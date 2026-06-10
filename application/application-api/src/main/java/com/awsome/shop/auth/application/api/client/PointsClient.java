package com.awsome.shop.auth.application.api.client;

/**
 * 积分服务出站客户端（FR-A6）。
 *
 * <p>注册成功后同步触发"入职奖励"积分发放。</p>
 */
public interface PointsClient {

    /**
     * 为新注册用户发放入职奖励积分。
     *
     * @param userId 新用户ID
     */
    void grantOnboardingBonus(Long userId);
}
