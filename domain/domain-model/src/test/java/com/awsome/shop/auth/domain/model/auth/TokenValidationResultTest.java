package com.awsome.shop.auth.domain.model.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenValidationResult 单元测试
 */
class TokenValidationResultTest {

    @Test
    @DisplayName("success 工厂方法应携带 userId 和 role")
    void successShouldCarryUserIdAndRole() {
        TokenValidationResult result = TokenValidationResult.success(42L, "ADMIN");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getMessage()).isNull();
    }

    @Test
    @DisplayName("failure 工厂方法应携带消息且无用户信息")
    void failureShouldCarryMessageOnly() {
        TokenValidationResult result = TokenValidationResult.failure("Token 无效或已过期");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getUserId()).isNull();
        assertThat(result.getRole()).isNull();
        assertThat(result.getMessage()).isEqualTo("Token 无效或已过期");
    }
}
