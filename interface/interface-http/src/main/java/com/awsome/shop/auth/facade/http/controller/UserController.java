package com.awsome.shop.auth.facade.http.controller;

import com.awsome.shop.auth.application.api.dto.user.UserDTO;
import com.awsome.shop.auth.application.api.dto.user.request.ChangeRoleRequest;
import com.awsome.shop.auth.application.api.dto.user.request.GetUserRequest;
import com.awsome.shop.auth.application.api.dto.user.request.ListUserRequest;
import com.awsome.shop.auth.application.api.service.user.UserApplicationService;
import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.common.enums.AuthErrorCode;
import com.awsome.shop.auth.common.exception.BusinessException;
import com.awsome.shop.auth.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理 Controller
 *
 * <p>所有接口均为管理端，挂在 {@code /api/v1/auth/**}（网关要求认证）。
 * 服务内再校验网关注入的 {@code X-User-Role} 必须为 ADMIN，作为越权兜底。</p>
 */
@Tag(name = "User", description = "用户管理")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private final UserApplicationService userApplicationService;

    @Operation(summary = "用户列表分页查询（管理员）")
    @PostMapping("/auth/user/list")
    public Result<PageResult<UserDTO>> list(@RequestBody @Valid ListUserRequest request,
                                            @RequestHeader(value = HEADER_USER_ROLE, required = false) String role) {
        requireAdmin(role);
        return Result.success(userApplicationService.list(request));
    }

    @Operation(summary = "用户详情查询（管理员）")
    @PostMapping("/auth/user/detail")
    public Result<UserDTO> getUser(@RequestBody @Valid GetUserRequest request,
                                   @RequestHeader(value = HEADER_USER_ROLE, required = false) String role) {
        requireAdmin(role);
        return Result.success(userApplicationService.getUser(request));
    }

    @Operation(summary = "变更用户角色（管理员）")
    @PostMapping("/auth/user/role")
    public Result<UserDTO> changeRole(@RequestBody @Valid ChangeRoleRequest request,
                                      @RequestHeader(value = HEADER_USER_ROLE, required = false) String role) {
        requireAdmin(role);
        return Result.success(userApplicationService.changeRole(request));
    }

    /**
     * 校验网关注入的角色头必须为 ADMIN，否则拒绝（防止普通员工自我提权）。
     */
    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new BusinessException(AuthErrorCode.ADMIN_REQUIRED);
        }
    }
}
