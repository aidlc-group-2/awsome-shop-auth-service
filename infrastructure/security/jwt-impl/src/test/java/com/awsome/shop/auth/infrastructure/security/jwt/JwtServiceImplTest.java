package com.awsome.shop.auth.infrastructure.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtServiceImpl 单元测试（使用真实 JJWT，不依赖 Spring 上下文）
 */
class JwtServiceImplTest {

    private static final String SECRET = "test-secret-key-for-unit-test-32bytes-minimum!!";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(SECRET, 7200L, "awsome-shop-auth-service");
    }

    @Test
    @DisplayName("生成的 token 应能解析出 userId/username/role")
    void generatedTokenShouldCarryClaims() {
        String token = jwtService.generateToken(42L, "alice", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("validateToken 对有效 token 返回 true")
    void validateTokenShouldAcceptValidToken() {
        String token = jwtService.generateToken(1L, "alice", "EMPLOYEE");

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken 对篡改的 token 返回 false")
    void validateTokenShouldRejectTamperedToken() {
        String token = jwtService.generateToken(1L, "alice", "EMPLOYEE");
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken 对其他密钥签发的 token 返回 false")
    void validateTokenShouldRejectTokenSignedWithDifferentKey() {
        JwtServiceImpl other = new JwtServiceImpl(
                "another-secret-key-with-32-bytes-or-more!!", 7200L, "other-issuer");
        String token = other.generateToken(1L, "alice", "EMPLOYEE");

        assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken 对已过期 token 返回 false")
    void validateTokenShouldRejectExpiredToken() {
        JwtServiceImpl shortLived = new JwtServiceImpl(SECRET, -60L, "awsome-shop-auth-service");
        String token = shortLived.generateToken(1L, "alice", "EMPLOYEE");

        assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken 对垃圾输入返回 false 而不抛异常")
    void validateTokenShouldRejectGarbageInput() {
        assertThat(jwtService.validateToken("not-a-jwt")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
        assertThat(jwtService.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("getExpirationSeconds 返回配置值")
    void getExpirationSecondsShouldReturnConfiguredValue() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(7200L);
    }
}
