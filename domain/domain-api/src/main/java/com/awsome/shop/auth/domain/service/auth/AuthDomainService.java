package com.awsome.shop.auth.domain.service.auth;

import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.model.auth.TokenValidationResult;

/**
 * 认证领域服务接口
 */
public interface AuthDomainService {

    /**
     * 用户注册（FR-A1/A2/A4）：校验企业邮箱域名与唯一性，bcrypt 加密存储，创建用户。
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @param email       企业邮箱
     * @param nickname    昵称（可空）
     * @return 创建成功的用户实体
     */
    UserEntity register(String username, String rawPassword, String email, String nickname);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户实体
     */
    UserEntity login(String username, String password);

    /**
     * 用户登出
     *
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * 令牌校验（FR-A7）：解析并校验 JWT（含黑名单检查），返回用户身份与角色。
     *
     * @param token JWT Token
     * @return 校验结果
     */
    TokenValidationResult validateToken(String token);
}
