package com.awsome.shop.auth.domain.impl.service.user;

import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.common.enums.AuthErrorCode;
import com.awsome.shop.auth.common.exception.BusinessException;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.service.user.UserDomainService;
import com.awsome.shop.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 用户领域服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

    private static final Set<String> VALID_ROLES = Set.of("EMPLOYEE", "ADMIN");

    private final UserRepository userRepository;

    @Override
    public PageResult<UserEntity> page(int page, int size, String username, String role, String status) {
        return userRepository.page(page, size, username, role, status);
    }

    @Override
    public UserEntity getById(Long userId) {
        UserEntity user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserEntity changeRole(Long userId, String role) {
        if (role == null || !VALID_ROLES.contains(role)) {
            throw new BusinessException(AuthErrorCode.INVALID_ROLE);
        }
        UserEntity user = getById(userId);
        user.setRole(role);
        userRepository.update(user);
        return user;
    }
}
