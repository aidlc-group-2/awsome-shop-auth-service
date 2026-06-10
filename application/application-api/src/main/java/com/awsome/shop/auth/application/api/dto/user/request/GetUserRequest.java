package com.awsome.shop.auth.application.api.dto.user.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询用户详情请求（FR-A5）。
 */
@Data
public class GetUserRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
