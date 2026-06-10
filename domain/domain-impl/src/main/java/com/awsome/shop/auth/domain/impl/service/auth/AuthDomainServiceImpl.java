package com.awsome.shop.auth.domain.impl.service.auth;

import com.awsome.shop.auth.common.enums.AuthErrorCode;
import com.awsome.shop.auth.common.exception.BusinessException;
import com.awsome.shop.auth.domain.model.auth.TokenValidationResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.service.auth.AuthDomainService;
import com.awsome.shop.auth.infrastructure.cache.api.service.TokenCacheService;
import com.awsome.shop.auth.infrastructure.security.api.service.JwtService;
import com.awsome.shop.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证领域服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenCacheService tokenCacheService;
    private final EmailDomainValidator emailDomainValidator;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lock-duration:1800}")
    private long lockDurationSeconds;

    @Override
    @Transactional
    public UserEntity register(String username, String rawPassword, String email, String nickname) {
        // FR-A2 企业邮箱域名白名单
        if (!emailDomainValidator.isAllowed(email)) {
            throw new BusinessException(AuthErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);
        }
        // 唯一性校验
        if (userRepository.findByUsername(username) != null) {
            throw new BusinessException(AuthErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.findByEmail(email) != null) {
            throw new BusinessException(AuthErrorCode.EMAIL_EXISTS);
        }

        // FR-A4 bcrypt 加密 + FR-A5 默认角色 EMPLOYEE
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole("EMPLOYEE");
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
        return user;
    }

    @Override
    public UserEntity login(String username, String password) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 检查账户是否被禁用
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }

        // 检查账户是否被锁定
        if (user.isLocked()) {
            long remainingMinutes = java.time.Duration.between(
                    java.time.LocalDateTime.now(), user.getLockExpiredAt()).toMinutes() + 1;
            throw new BusinessException(AuthErrorCode.ACCOUNT_LOCKED, String.valueOf(remainingMinutes));
        }

        // 如果锁定已过期，重置状态
        if ("LOCKED".equals(user.getStatus()) && !user.isLocked()) {
            user.setStatus("ACTIVE");
            user.setFailedLoginAttempts(0);
            user.setLockExpiredAt(null);
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordLoginFailure(maxFailedAttempts, lockDurationSeconds);
            userRepository.update(user);
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 登录成功
        user.recordLoginSuccess();
        userRepository.update(user);

        return user;
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtService.validateToken(token)) {
            tokenCacheService.addToBlacklist(token, jwtService.getExpirationSeconds());
        }
    }

    @Override
    public TokenValidationResult validateToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenValidationResult.failure(AuthErrorCode.INVALID_TOKEN.getMessage());
        }
        if (!jwtService.validateToken(token)) {
            return TokenValidationResult.failure(AuthErrorCode.INVALID_TOKEN.getMessage());
        }
        // 已登出（黑名单）的令牌视为无效
        if (tokenCacheService.isBlacklisted(token)) {
            return TokenValidationResult.failure(AuthErrorCode.INVALID_TOKEN.getMessage());
        }
        Long userId = jwtService.getUserIdFromToken(token);
        String role = jwtService.getRoleFromToken(token);
        return TokenValidationResult.success(userId, role);
    }
}
