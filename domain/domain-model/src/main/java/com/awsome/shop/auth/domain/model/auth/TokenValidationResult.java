package com.awsome.shop.auth.domain.model.auth;

import lombok.Getter;

/**
 * 令牌校验结果（FR-A7）。供网关/内部调用校验 JWT 后获取用户身份与角色。
 */
@Getter
public class TokenValidationResult {

    private final boolean valid;

    private final Long userId;

    private final String role;

    private final String message;

    private TokenValidationResult(boolean valid, Long userId, String role, String message) {
        this.valid = valid;
        this.userId = userId;
        this.role = role;
        this.message = message;
    }

    public static TokenValidationResult success(Long userId, String role) {
        return new TokenValidationResult(true, userId, role, null);
    }

    public static TokenValidationResult failure(String message) {
        return new TokenValidationResult(false, null, null, message);
    }
}
