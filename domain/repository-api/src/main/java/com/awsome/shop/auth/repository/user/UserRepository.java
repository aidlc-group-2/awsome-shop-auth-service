package com.awsome.shop.auth.repository.user;

import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;

/**
 * User 仓储接口
 */
public interface UserRepository {

    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);

    UserEntity findById(Long id);

    void save(UserEntity entity);

    void update(UserEntity entity);

    PageResult<UserEntity> page(int page, int size, String username, String role, String status);
}
