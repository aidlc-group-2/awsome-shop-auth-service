package com.awsome.shop.auth.application.impl.service.auth;

import com.awsome.shop.auth.application.api.client.PointsClient;
import com.awsome.shop.auth.application.api.dto.auth.LoginRequest;
import com.awsome.shop.auth.application.api.dto.auth.LoginResponse;
import com.awsome.shop.auth.application.api.dto.auth.RegisterRequest;
import com.awsome.shop.auth.application.api.dto.auth.RegisterResponse;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenRequest;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenResponse;
import com.awsome.shop.auth.application.api.service.auth.AuthApplicationService;
import com.awsome.shop.auth.domain.model.auth.TokenValidationResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.service.auth.AuthDomainService;
import com.awsome.shop.auth.infrastructure.security.api.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证应用服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationServiceImpl implements AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final JwtService jwtService;
    private final PointsClient pointsClient;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // 1. 创建用户（领域层：邮箱白名单 + 唯一性 + bcrypt）
        UserEntity user = authDomainService.register(
                request.getUsername(), request.getPassword(),
                request.getEmail(), request.getNickname());

        // 2. FR-A6 同步触发入职奖励（失败不回滚注册，详见 PointsClientImpl / IMPLEMENTATION_NOTES.md）
        pointsClient.grantOnboardingBonus(user.getId());

        // 3. 注册成功自动签发 JWT（决策 D-B3）
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        RegisterResponse response = new RegisterResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = authDomainService.login(request.getUsername(), request.getPassword());

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public void logout(String token) {
        authDomainService.logout(token);
    }

    @Override
    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        TokenValidationResult result = authDomainService.validateToken(request.getToken());
        if (result.isValid()) {
            return new ValidateTokenResponse(true, result.getUserId(), result.getRole(), null);
        }
        return new ValidateTokenResponse(false, null, null, result.getMessage());
    }
}
