package com.awsome.shop.auth.domain.impl.service.user;

import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.common.exception.BusinessException;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserDomainServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDomainServiceImpl userDomainService;

    @Test
    @DisplayName("page 应透传查询条件")
    void pageShouldDelegateToRepository() {
        PageResult<UserEntity> pageResult = new PageResult<>();
        when(userRepository.page(1, 20, "alice", "ADMIN", "ACTIVE")).thenReturn(pageResult);

        PageResult<UserEntity> result = userDomainService.page(1, 20, "alice", "ADMIN", "ACTIVE");

        assertThat(result).isSameAs(pageResult);
    }

    @Test
    @DisplayName("getById 存在时返回用户")
    void getByIdShouldReturnUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(user);

        assertThat(userDomainService.getById(1L)).isSameAs(user);
    }

    @Test
    @DisplayName("getById 不存在时抛 AUTH_005")
    void getByIdShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userDomainService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("AUTH_005");
    }

    @Test
    @DisplayName("changeRole 合法角色应更新并返回")
    void changeRoleShouldUpdateForValidRole() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole("EMPLOYEE");
        when(userRepository.findById(1L)).thenReturn(user);

        UserEntity result = userDomainService.changeRole(1L, "ADMIN");

        assertThat(result.getRole()).isEqualTo("ADMIN");
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("changeRole 非法角色应抛 PARAM_102 且不更新")
    void changeRoleShouldRejectInvalidRole() {
        assertThatThrownBy(() -> userDomainService.changeRole(1L, "SUPERMAN"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("PARAM_102");

        assertThatThrownBy(() -> userDomainService.changeRole(1L, null))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).update(any());
    }
}
