package com.awsome.shop.auth.domain.impl.service.auth;

import com.awsome.shop.auth.common.exception.BusinessException;
import com.awsome.shop.auth.domain.model.auth.TokenValidationResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.infrastructure.cache.api.service.TokenCacheService;
import com.awsome.shop.auth.infrastructure.security.api.service.JwtService;
import com.awsome.shop.auth.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthDomainServiceImpl 单元测试
 *
 * <p>passwordEncoder 在被测类中内联创建（BCryptPasswordEncoder），
 * 测试中使用真实 BCrypt 生成密码哈希；@Value 字段通过反射注入。</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthDomainServiceImplTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenCacheService tokenCacheService;

    @Mock
    private EmailDomainValidator emailDomainValidator;

    private AuthDomainServiceImpl authDomainService;

    @BeforeEach
    void setUp() throws Exception {
        authDomainService = new AuthDomainServiceImpl(
                userRepository, jwtService, tokenCacheService, emailDomainValidator);
        setField("maxFailedAttempts", 3);
        setField("lockDurationSeconds", 1800L);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AuthDomainServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(authDomainService, value);
    }

    // ==================== register ====================

    @Test
    @DisplayName("register 成功：bcrypt 加密、默认 EMPLOYEE/ACTIVE")
    void registerShouldCreateUserWithDefaults() {
        when(emailDomainValidator.isAllowed("alice@corp.com")).thenReturn(true);
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.findByEmail("alice@corp.com")).thenReturn(null);

        UserEntity user = authDomainService.register("alice", "P@ssw0rd", "alice@corp.com", "Alice");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(user).isSameAs(saved);
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@corp.com");
        assertThat(saved.getNickname()).isEqualTo("Alice");
        assertThat(saved.getRole()).isEqualTo("EMPLOYEE");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getFailedLoginAttempts()).isZero();
        assertThat(saved.getPasswordHash()).isNotEqualTo("P@ssw0rd");
        assertThat(ENCODER.matches("P@ssw0rd", saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("register 邮箱域名不允许时抛 PARAM_101")
    void registerShouldRejectDisallowedEmailDomain() {
        when(emailDomainValidator.isAllowed("alice@evil.com")).thenReturn(false);

        assertThatThrownBy(() ->
                authDomainService.register("alice", "pwd", "alice@evil.com", "Alice"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("PARAM_101");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register 用户名已存在时抛 CONFLICT_001")
    void registerShouldRejectDuplicateUsername() {
        when(emailDomainValidator.isAllowed(anyString())).thenReturn(true);
        when(userRepository.findByUsername("alice")).thenReturn(new UserEntity());

        assertThatThrownBy(() ->
                authDomainService.register("alice", "pwd", "alice@corp.com", "Alice"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("CONFLICT_001");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register 邮箱已注册时抛 CONFLICT_002")
    void registerShouldRejectDuplicateEmail() {
        when(emailDomainValidator.isAllowed(anyString())).thenReturn(true);
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.findByEmail("alice@corp.com")).thenReturn(new UserEntity());

        assertThatThrownBy(() ->
                authDomainService.register("alice", "pwd", "alice@corp.com", "Alice"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("CONFLICT_002");

        verify(userRepository, never()).save(any());
    }

    // ==================== login ====================

    private UserEntity activeUser(String rawPassword) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash(ENCODER.encode(rawPassword));
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);
        return user;
    }

    @Test
    @DisplayName("login 成功：重置失败计数并更新登录时间")
    void loginShouldSucceedWithCorrectPassword() {
        UserEntity user = activeUser("P@ssw0rd");
        user.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("alice")).thenReturn(user);

        UserEntity result = authDomainService.login("alice", "P@ssw0rd");

        assertThat(result.getFailedLoginAttempts()).isZero();
        assertThat(result.getLastLoginAt()).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("login 用户不存在时抛 AUTH_001")
    void loginShouldRejectUnknownUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        assertThatThrownBy(() -> authDomainService.login("ghost", "pwd"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("AUTH_001");
    }

    @Test
    @DisplayName("login 账户禁用时抛 AUTH_003")
    void loginShouldRejectDisabledAccount() {
        UserEntity user = activeUser("pwd");
        user.setStatus("DISABLED");
        when(userRepository.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> authDomainService.login("alice", "pwd"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("AUTH_003");
    }

    @Test
    @DisplayName("login 账户锁定中抛 AUTH_002")
    void loginShouldRejectLockedAccount() {
        UserEntity user = activeUser("pwd");
        user.setStatus("LOCKED");
        user.setLockExpiredAt(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> authDomainService.login("alice", "pwd"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("AUTH_002");
    }

    @Test
    @DisplayName("login 锁定已过期时应重置状态并允许登录")
    void loginShouldResetExpiredLock() {
        UserEntity user = activeUser("P@ssw0rd");
        user.setStatus("LOCKED");
        user.setFailedLoginAttempts(3);
        user.setLockExpiredAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByUsername("alice")).thenReturn(user);

        UserEntity result = authDomainService.login("alice", "P@ssw0rd");

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getFailedLoginAttempts()).isZero();
        assertThat(result.getLockExpiredAt()).isNull();
    }

    @Test
    @DisplayName("login 密码错误时累计失败计数并抛 AUTH_001")
    void loginShouldRecordFailureOnWrongPassword() {
        UserEntity user = activeUser("P@ssw0rd");
        when(userRepository.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> authDomainService.login("alice", "wrong"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("AUTH_001");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("login 失败次数达到上限时账户被锁定")
    void loginShouldLockAccountAfterMaxFailures() {
        UserEntity user = activeUser("P@ssw0rd");
        user.setFailedLoginAttempts(2); // maxFailedAttempts = 3
        when(userRepository.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> authDomainService.login("alice", "wrong"))
                .isInstanceOf(BusinessException.class);

        assertThat(user.getStatus()).isEqualTo("LOCKED");
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getLockExpiredAt()).isAfter(LocalDateTime.now());
    }

    // ==================== logout ====================

    @Test
    @DisplayName("logout 有效 token 应加入黑名单")
    void logoutShouldBlacklistValidToken() {
        when(jwtService.validateToken("token-1")).thenReturn(true);
        when(jwtService.getExpirationSeconds()).thenReturn(7200L);

        authDomainService.logout("token-1");

        verify(tokenCacheService).addToBlacklist("token-1", 7200L);
    }

    @Test
    @DisplayName("logout 无效或空 token 不加黑名单")
    void logoutShouldIgnoreInvalidToken() {
        when(jwtService.validateToken("bad-token")).thenReturn(false);

        authDomainService.logout("bad-token");
        authDomainService.logout(null);

        verify(tokenCacheService, never()).addToBlacklist(anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    // ==================== validateToken ====================

    @Test
    @DisplayName("validateToken 有效且未拉黑时返回成功")
    void validateTokenShouldSucceedForValidToken() {
        when(jwtService.validateToken("good")).thenReturn(true);
        when(tokenCacheService.isBlacklisted("good")).thenReturn(false);
        when(jwtService.getUserIdFromToken("good")).thenReturn(7L);
        when(jwtService.getRoleFromToken("good")).thenReturn("ADMIN");

        TokenValidationResult result = authDomainService.validateToken("good");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("validateToken 空 token 返回失败")
    void validateTokenShouldFailForBlankToken() {
        assertThat(authDomainService.validateToken(null).isValid()).isFalse();
        assertThat(authDomainService.validateToken("  ").isValid()).isFalse();
    }

    @Test
    @DisplayName("validateToken JWT 校验失败时返回失败")
    void validateTokenShouldFailForInvalidJwt() {
        when(jwtService.validateToken("bad")).thenReturn(false);

        TokenValidationResult result = authDomainService.validateToken("bad");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("validateToken 已拉黑的 token 返回失败")
    void validateTokenShouldFailForBlacklistedToken() {
        when(jwtService.validateToken("revoked")).thenReturn(true);
        when(tokenCacheService.isBlacklisted("revoked")).thenReturn(true);

        TokenValidationResult result = authDomainService.validateToken("revoked");

        assertThat(result.isValid()).isFalse();
    }
}
