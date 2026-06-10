package com.awsome.shop.auth.facade.http.controller;

import com.awsome.shop.auth.application.api.dto.auth.LoginRequest;
import com.awsome.shop.auth.application.api.dto.auth.LoginResponse;
import com.awsome.shop.auth.application.api.dto.auth.RegisterRequest;
import com.awsome.shop.auth.application.api.dto.auth.RegisterResponse;
import com.awsome.shop.auth.application.api.service.auth.AuthApplicationService;
import com.awsome.shop.auth.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 */
@Tag(name = "Auth", description = "用户认证（登录/登出）")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @Operation(summary = "用户注册")
    @PostMapping("/public/auth/register")
    public Result<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return Result.success(authApplicationService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/public/auth/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authApplicationService.login(request));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/public/auth/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token != null) {
            authApplicationService.logout(token);
        }
        return Result.success();
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
