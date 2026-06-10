package com.awsome.shop.auth.domain.model.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User 领域实体
 */
@Data
public class UserEntity {

    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String nickname;

    private String role;

    private String status;

    private Integer failedLoginAttempts;

    private LocalDateTime lockExpiredAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public boolean isLocked() {
        return "LOCKED".equals(status)
                && lockExpiredAt != null
                && lockExpiredAt.isAfter(LocalDateTime.now());
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public void recordLoginSuccess() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.lockExpiredAt = null;
    }

    public void recordLoginFailure(int maxAttempts, long lockDurationSeconds) {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.status = "LOCKED";
            this.lockExpiredAt = LocalDateTime.now().plusSeconds(lockDurationSeconds);
        }
    }
}
