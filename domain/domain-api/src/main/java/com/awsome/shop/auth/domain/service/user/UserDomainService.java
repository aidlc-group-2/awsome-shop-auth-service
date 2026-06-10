package com.awsome.shop.auth.domain.service.user;

import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;

/**
 * 用户领域服务接口
 */
public interface UserDomainService {

    PageResult<UserEntity> page(int page, int size, String username, String role, String status);

    /**
     * 查询用户详情（FR-A5）。
     */
    UserEntity getById(Long userId);

    /**
     * 变更用户角色（FR-A5）。
     *
     * @param userId 用户ID
     * @param role   目标角色（EMPLOYEE/ADMIN）
     * @return 变更后的用户实体
     */
    UserEntity changeRole(Long userId, String role);
}
