package com.awsome.shop.auth.application.impl.client;

import com.awsome.shop.auth.application.api.client.PointsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * 积分服务客户端实现（FR-A6）。
 *
 * <p><b>WORKAROUND（临时方案）</b>：积分服务（Unit4 / awsome-shop-points-service）尚未实现，
 * 默认通过特性开关 {@code shop.points.onboarding.enabled=false} 关闭真实调用。</p>
 *
 * <p>失败处理遵循设计（services.md §2.1）：注册主事务不回滚，发积分失败仅记录日志，
 * 由后续重试/补偿保证最终一致。</p>
 *
 * <p>启用条件见 {@code docs/IMPLEMENTATION_NOTES.md}。搜索 {@code TODO(FR-A6)} 可定位相关占位。</p>
 */
@Slf4j
@Component
public class PointsClientImpl implements PointsClient {

    private final boolean onboardingEnabled;
    private final String baseUrl;
    private final WebClient webClient;
    private final Duration timeout;

    public PointsClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${shop.points.onboarding.enabled:false}") boolean onboardingEnabled,
            @Value("${shop.points.onboarding.base-url:http://localhost:8003}") String baseUrl,
            @Value("${shop.points.onboarding.timeout:5s}") Duration timeout) {
        this.onboardingEnabled = onboardingEnabled;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.build();
        this.timeout = timeout;
    }

    @Override
    public void grantOnboardingBonus(Long userId) {
        // TODO(FR-A6): 积分服务（Unit4）就绪后，将 shop.points.onboarding.enabled 置为 true 并完成联调。
        if (!onboardingEnabled) {
            log.info("[FR-A6][WORKAROUND] 入职积分发放已禁用(shop.points.onboarding.enabled=false)，"
                    + "跳过 userId={} 的入职奖励发放。积分服务就绪后开启。", userId);
            return;
        }
        try {
            webClient.post()
                    .uri(baseUrl + "/api/v1/internal/points/grant")
                    .bodyValue(Map.of(
                            "userId", userId,
                            "type", "ONBOARDING",
                            "reason", "新用户注册入职奖励"))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(timeout)
                    .block();
            log.info("[FR-A6] 已为 userId={} 触发入职奖励积分发放", userId);
        } catch (Exception e) {
            // 注册不回滚：仅记录，待重试/补偿
            log.error("[FR-A6] 入职奖励积分发放失败 userId={}，注册不回滚，待重试/补偿。原因: {}",
                    userId, e.getMessage(), e);
        }
    }
}
