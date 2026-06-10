package com.awsome.shop.auth.application.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 令牌校验响应（FR-A7）。
 *
 * <p>契约按设计：返回用户身份与角色，供网关注入 {@code X-User-Id}/{@code X-User-Role}。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private boolean success;

    private Long userId;

    private String role;

    private String message;
}
