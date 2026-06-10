package com.awsome.shop.auth.facade.http.controller;

import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenRequest;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenResponse;
import com.awsome.shop.auth.application.api.service.auth.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部认证 Controller（FR-A7）。
 *
 * <p>供 API 网关进行 JWT 令牌校验。响应直接返回 {@link ValidateTokenResponse}
 * （不包裹统一 Result），以匹配网关的反序列化契约：{@code {success, userId, role, message}}。</p>
 *
 * <p>该路径位于 {@code /api/v1/internal/**}，应仅在内网/网关侧可达
 * （由网关 InternalAuthFilter / 网络策略保证，详见 gap 分析）。</p>
 */
@Tag(name = "Internal Auth", description = "内部令牌校验（供网关调用）")
@RestController
@RequestMapping("/api/v1/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthApplicationService authApplicationService;

    @Operation(summary = "校验 JWT 令牌，返回用户身份与角色")
    @PostMapping("/validate")
    public ValidateTokenResponse validate(@RequestBody @Valid ValidateTokenRequest request) {
        return authApplicationService.validateToken(request);
    }
}
