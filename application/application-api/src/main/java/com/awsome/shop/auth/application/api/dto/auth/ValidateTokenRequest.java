package com.awsome.shop.auth.application.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 令牌校验请求（FR-A7）。
 */
@Data
public class ValidateTokenRequest {

    @NotBlank(message = "token 不能为空")
    private String token;
}
