package com.awsome.shop.auth.application.api.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User 数据传输对象
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private String nickname;

    private String role;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
