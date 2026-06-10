package com.awsome.shop.auth.facade.http.controller;

import com.awsome.shop.auth.application.api.dto.user.UserDTO;
import com.awsome.shop.auth.application.api.dto.user.request.ChangeRoleRequest;
import com.awsome.shop.auth.application.api.dto.user.request.GetUserRequest;
import com.awsome.shop.auth.application.api.dto.user.request.ListUserRequest;
import com.awsome.shop.auth.application.api.service.user.UserApplicationService;
import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理 Controller
 */
@Tag(name = "User", description = "用户管理")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;

    @Operation(summary = "用户列表分页查询")
    @PostMapping("/public/auth/user/list")
    public Result<PageResult<UserDTO>> list(@RequestBody @Valid ListUserRequest request) {
        return Result.success(userApplicationService.list(request));
    }

    @Operation(summary = "用户详情查询（管理员）")
    @PostMapping("/auth/user/detail")
    public Result<UserDTO> getUser(@RequestBody @Valid GetUserRequest request) {
        return Result.success(userApplicationService.getUser(request));
    }

    @Operation(summary = "变更用户角色（管理员）")
    @PostMapping("/auth/user/role")
    public Result<UserDTO> changeRole(@RequestBody @Valid ChangeRoleRequest request) {
        return Result.success(userApplicationService.changeRole(request));
    }
}
