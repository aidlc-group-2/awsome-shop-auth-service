package com.awsome.shop.auth.application.api.dto.auth;

import lombok.Data;

/**
 * 注册响应（FR-A1）。注册成功后自动返回 JWT（登录态）。
 */
@Data
public class RegisterResponse {

    private String token;

    private Long userId;

    private String username;

    private String email;

    private String nickname;

    private String role;
}
