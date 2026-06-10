package com.awsome.shop.auth.application.api.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 变更用户角色请求（FR-A5）。
 */
@Data
public class ChangeRoleRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "角色不能为空")
    private String role;
}
