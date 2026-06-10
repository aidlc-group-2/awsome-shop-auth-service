package com.awsome.shop.auth.application.api.service.auth;

import com.awsome.shop.auth.application.api.dto.auth.LoginRequest;
import com.awsome.shop.auth.application.api.dto.auth.LoginResponse;
import com.awsome.shop.auth.application.api.dto.auth.RegisterRequest;
import com.awsome.shop.auth.application.api.dto.auth.RegisterResponse;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenRequest;
import com.awsome.shop.auth.application.api.dto.auth.ValidateTokenResponse;

/**
 * 认证应用服务接口
 */
public interface AuthApplicationService {

    /**
     * 注册（FR-A1）：创建用户、触发入职积分，并自动返回 JWT。
     */
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);

    /**
     * 令牌校验（FR-A7），供网关/内部调用。
     */
    ValidateTokenResponse validateToken(ValidateTokenRequest request);
}
