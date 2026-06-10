package com.awsome.shop.auth.application.api.service.user;

import com.awsome.shop.auth.application.api.dto.user.UserDTO;
import com.awsome.shop.auth.application.api.dto.user.request.ChangeRoleRequest;
import com.awsome.shop.auth.application.api.dto.user.request.GetUserRequest;
import com.awsome.shop.auth.application.api.dto.user.request.ListUserRequest;
import com.awsome.shop.auth.common.dto.PageResult;

/**
 * 用户管理应用服务接口
 */
public interface UserApplicationService {

    PageResult<UserDTO> list(ListUserRequest request);

    /**
     * 查询用户详情（FR-A5）。
     */
    UserDTO getUser(GetUserRequest request);

    /**
     * 变更用户角色（FR-A5）。
     */
    UserDTO changeRole(ChangeRoleRequest request);
}
