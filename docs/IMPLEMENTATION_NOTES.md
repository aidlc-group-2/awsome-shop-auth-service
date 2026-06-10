# 实现说明与注意事项（Implementation Notes）

> 本文件记录认证服务（Unit2）补全过程中的关键决策、临时方案（workaround）与待办联调项。
> 关联设计：`awsome-shop-plan/aidlc-docs/inception/`；差距分析：`team/gap-analysis-unit2-unit6.md`。

---

## ⚠️ Workaround：入职奖励积分发放（FR-A6）

**背景**：注册成功后需「同步调用积分服务发放入职奖励」（设计 services.md §2.1）。但**积分服务（Unit4 / awsome-shop-points-service）尚未实现**（构建顺序中排在 Unit2、Unit6 之后）。

**临时方案**：
- 已定义出站接口 `PointsClient`（`grantOnboardingBonus(userId)`），但**默认不真正调用**。
- 通过特性开关控制：
  ```yaml
  shop:
    points:
      onboarding:
        enabled: false        # 积分服务就绪后改为 true
        base-url: http://localhost:8003
  ```
- 失败处理遵循设计：**注册主事务不回滚**（用户照常创建），发积分失败仅记录日志/告警，靠重试或后续补偿保证最终发放。

**移除/启用该 workaround 的条件**：
1. `awsome-shop-points-service` 提供 `POST /internal/points/grant`（入职奖励）接口；
2. 将 `shop.points.onboarding.enabled` 置为 `true` 并配置 `base-url`；
3. 完成 Auth→Points 联调（注册→发积分→流水可查）。

> 搜索关键字：`TODO(FR-A6)` 可定位代码中所有相关占位。

---

## 已确认决策（2026-06-10）

| # | 主题 | 决策 |
|---|------|------|
| D-B1 | 企业邮箱白名单（FR-A2） | 可配置 `shop.auth.email.allowed-domains`，默认 `amazon.com,example.com` |
| D-B2 | 登录方式（FR-A3） | **仅用户名 + 密码**（不支持邮箱登录） |
| D-B3 | 注册后行为 | **自动返回 JWT**（注册即登录态） |
| D-B4 | 令牌校验接口（FR-A7） | 按设计返回 `{success, userId, role, message}`，供网关注入 `X-User-Id`/`X-User-Role` |

---

## 跨服务待联调项

- **网关对齐（Unit6）**：网关当前校验响应仅含 `operatorId`、注入 `X-Operator-Id`。按 D-B4 / 设计，应携带 `role` 并注入 `X-User-Id`/`X-User-Role`。本服务校验接口已先按设计输出，网关侧对齐待 Unit6 修复。
- **积分服务（Unit4）**：见上方 FR-A6 workaround。

---

## 端口约定

本服务统一对外端口为 **8001**（`application-local.yml` 已正确）。其余 profile（base/test/docker）历史上写过 8080/8081，补全时一并收敛为 8001。
