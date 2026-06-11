package com.awsome.shop.auth.application.impl.service.auth;

import com.awsome.shop.auth.application.api.client.PointsClient;
import com.awsome.shop.auth.application.api.dto.auth.LoginRequest;
import com.awsome.shop.auth.application.api.dto.auth.LoginResponse;
import com.awsome.shop.auth.application.api.dto.auth.RegisterRequest;
import com.awsome.shop.auth.application.api.dto.auth.RegisterResponse;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenRequest;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenResponse;
import com.awsome.shop.auth.domain.model.auth.TokenValidationResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.service.auth.AuthDomainService;
import com.awsome.shop.auth.infrastructure.security.api.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthApplicationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PointsClient pointsClient;

    @InjectMocks
    private AuthApplicationServiceImpl authApplicationService;

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@corp.com");
        user.setNickname("Alice");
        user.setRole("EMPLOYEE");
        return user;
    }

    @Test
    @DisplayName("register 应编排 领域注册 -> 积分发放 -> 签发 JWT，并完整映射响应")
    void registerShouldOrchestrateAndMapResponse() {
        UserEntity user = user();
        when(authDomainService.register("alice", "P@ssw0rd", "alice@corp.com", "Alice"))
                .thenReturn(user);
        when(jwtService.generateToken(1L, "alice", "EMPLOYEE")).thenReturn("jwt-token");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("P@ssw0rd");
        request.setEmail("alice@corp.com");
        request.setNickname("Alice");

        RegisterResponse response = authApplicationService.register(request);

        InOrder inOrder = Mockito.inOrder(authDomainService, pointsClient, jwtService);
        inOrder.verify(authDomainService).register("alice", "P@ssw0rd", "alice@corp.com", "Alice");
        inOrder.verify(pointsClient).grantOnboardingBonus(1L);
        inOrder.verify(jwtService).generateToken(1L, "alice", "EMPLOYEE");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@corp.com");
        assertThat(response.getNickname()).isEqualTo("Alice");
        assertThat(response.getRole()).isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("login 应签发 JWT 并映射响应")
    void loginShouldGenerateTokenAndMapResponse() {
        UserEntity user = user();
        when(authDomainService.login("alice", "P@ssw0rd")).thenReturn(user);
        when(jwtService.generateToken(1L, "alice", "EMPLOYEE")).thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("P@ssw0rd");

        LoginResponse response = authApplicationService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getNickname()).isEqualTo("Alice");
        assertThat(response.getRole()).isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("logout 应委托领域服务")
    void logoutShouldDelegate() {
        authApplicationService.logout("token-1");

        verify(authDomainService).logout("token-1");
    }

    @Test
    @DisplayName("validateToken 有效时返回用户信息")
    void validateTokenShouldMapSuccessResult() {
        when(authDomainService.validateToken("good"))
                .thenReturn(TokenValidationResult.success(7L, "ADMIN"));

        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("good");

        ValidateTokenResponse response = authApplicationService.validateToken(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("validateToken 无效时返回失败消息")
    void validateTokenShouldMapFailureResult() {
        when(authDomainService.validateToken("bad"))
                .thenReturn(TokenValidationResult.failure("Token 无效或已过期"));

        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("bad");

        ValidateTokenResponse response = authApplicationService.validateToken(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getMessage()).isEqualTo("Token 无效或已过期");
    }
}
