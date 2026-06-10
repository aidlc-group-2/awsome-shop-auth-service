package com.awsome.shop.auth.infrastructure.security.api.service;

/**
 * JWT 令牌服务接口
 */
public interface JwtService {

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT Token 字符串
     */
    String generateToken(Long userId, String username, String role);

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从 Token 中提取角色
     *
     * @param token JWT Token
     * @return 角色
     */
    String getRoleFromToken(String token);

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效 false-无效
     */
    boolean validateToken(String token);

    /**
     * 获取 Token 过期时间（秒）
     *
     * @return 过期时间秒数
     */
    long getExpirationSeconds();
}
