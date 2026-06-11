package com.awsome.shop.auth.application.impl.service.user;

import com.awsome.shop.auth.application.api.dto.user.UserDTO;
import com.awsome.shop.auth.application.api.dto.user.request.ChangeRoleRequest;
import com.awsome.shop.auth.application.api.dto.user.request.GetUserRequest;
import com.awsome.shop.auth.application.api.dto.user.request.ListUserRequest;
import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.domain.service.user.UserDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserApplicationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceImplTest {

    @Mock
    private UserDomainService userDomainService;

    @InjectMocks
    private UserApplicationServiceImpl userApplicationService;

    private UserEntity user() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("alice");
        entity.setEmail("alice@corp.com");
        entity.setNickname("Alice");
        entity.setRole("EMPLOYEE");
        entity.setStatus("ACTIVE");
        entity.setLastLoginAt(LocalDateTime.of(2026, 6, 11, 9, 0));
        entity.setCreatedAt(LocalDateTime.of(2026, 6, 1, 0, 0));
        entity.setUpdatedAt(LocalDateTime.of(2026, 6, 10, 0, 0));
        return entity;
    }

    private void assertDtoMatchesUser(UserDTO dto) {
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("alice");
        assertThat(dto.getEmail()).isEqualTo("alice@corp.com");
        assertThat(dto.getNickname()).isEqualTo("Alice");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getLastLoginAt()).isEqualTo(LocalDateTime.of(2026, 6, 11, 9, 0));
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 10, 0, 0));
    }

    @Test
    @DisplayName("list 应转换分页结果为 UserDTO")
    void listShouldConvertPageResult() {
        PageResult<UserEntity> page = new PageResult<>();
        page.setCurrent(1L);
        page.setSize(20L);
        page.setTotal(1L);
        page.setPages(1L);
        page.setRecords(List.of(user()));
        when(userDomainService.page(1, 20, "alice", "EMPLOYEE", "ACTIVE")).thenReturn(page);

        ListUserRequest request = new ListUserRequest();
        request.setPage(1);
        request.setSize(20);
        request.setUsername("alice");
        request.setRole("EMPLOYEE");
        request.setStatus("ACTIVE");

        PageResult<UserDTO> result = userApplicationService.list(request);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertDtoMatchesUser(result.getRecords().get(0));
        assertThat(result.getRecords().get(0).getRole()).isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("getUser 应返回映射后的 DTO")
    void getUserShouldMapEntity() {
        when(userDomainService.getById(1L)).thenReturn(user());

        GetUserRequest request = new GetUserRequest();
        request.setUserId(1L);

        UserDTO dto = userApplicationService.getUser(request);

        assertDtoMatchesUser(dto);
    }

    @Test
    @DisplayName("changeRole 应委托领域服务并返回更新后的 DTO")
    void changeRoleShouldDelegateAndMap() {
        UserEntity updated = user();
        updated.setRole("ADMIN");
        when(userDomainService.changeRole(1L, "ADMIN")).thenReturn(updated);

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(1L);
        request.setRole("ADMIN");

        UserDTO dto = userApplicationService.changeRole(request);

        verify(userDomainService).changeRole(1L, "ADMIN");
        assertThat(dto.getRole()).isEqualTo("ADMIN");
    }
}
