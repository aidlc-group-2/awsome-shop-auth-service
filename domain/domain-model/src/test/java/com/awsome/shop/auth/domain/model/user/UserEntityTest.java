package com.awsome.shop.auth.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserEntity 单元测试
 */
class UserEntityTest {

    @Test
    @DisplayName("LOCKED 且锁未过期时 isLocked 为 true")
    void isLockedShouldBeTrueWhenLockedAndNotExpired() {
        UserEntity user = new UserEntity();
        user.setStatus("LOCKED");
        user.setLockExpiredAt(LocalDateTime.now().plusMinutes(10));

        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("LOCKED 但锁已过期时 isLocked 为 false")
    void isLockedShouldBeFalseWhenLockExpired() {
        UserEntity user = new UserEntity();
        user.setStatus("LOCKED");
        user.setLockExpiredAt(LocalDateTime.now().minusMinutes(1));

        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("非 LOCKED 状态或缺少过期时间时 isLocked 为 false")
    void isLockedShouldBeFalseForOtherStatusOrMissingExpiry() {
        UserEntity active = new UserEntity();
        active.setStatus("ACTIVE");
        active.setLockExpiredAt(LocalDateTime.now().plusMinutes(10));
        assertThat(active.isLocked()).isFalse();

        UserEntity noExpiry = new UserEntity();
        noExpiry.setStatus("LOCKED");
        noExpiry.setLockExpiredAt(null);
        assertThat(noExpiry.isLocked()).isFalse();
    }

    @Test
    @DisplayName("isActive 仅在 ACTIVE 状态为 true")
    void isActiveShouldReflectStatus() {
        UserEntity user = new UserEntity();
        user.setStatus("ACTIVE");
        assertThat(user.isActive()).isTrue();

        user.setStatus("DISABLED");
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("recordLoginSuccess 应重置失败计数并解除锁定")
    void recordLoginSuccessShouldResetState() {
        UserEntity user = new UserEntity();
        user.setStatus("LOCKED");
        user.setFailedLoginAttempts(4);
        user.setLockExpiredAt(LocalDateTime.now().plusMinutes(5));

        user.recordLoginSuccess();

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        assertThat(user.getLockExpiredAt()).isNull();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("recordLoginFailure 未达上限时仅累计次数")
    void recordLoginFailureShouldIncrementBelowThreshold() {
        UserEntity user = new UserEntity();
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(1);

        user.recordLoginFailure(5, 1800);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        assertThat(user.getLockExpiredAt()).isNull();
    }

    @Test
    @DisplayName("recordLoginFailure 达到上限时锁定账户并设置过期时间")
    void recordLoginFailureShouldLockAtThreshold() {
        UserEntity user = new UserEntity();
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(4);

        LocalDateTime before = LocalDateTime.now();
        user.recordLoginFailure(5, 1800);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getStatus()).isEqualTo("LOCKED");
        assertThat(user.getLockExpiredAt()).isAfter(before.plusSeconds(1700));
    }

    @Test
    @DisplayName("recordLoginFailure 对 null 计数按 0 处理")
    void recordLoginFailureShouldHandleNullCounter() {
        UserEntity user = new UserEntity();
        user.setFailedLoginAttempts(null);

        user.recordLoginFailure(5, 1800);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }
}
