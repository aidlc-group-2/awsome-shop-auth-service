package com.awsome.shop.auth.repository.mysql.impl.user;

import com.awsome.shop.auth.common.dto.PageResult;
import com.awsome.shop.auth.domain.model.user.UserEntity;
import com.awsome.shop.auth.repository.mysql.mapper.user.UserMapper;
import com.awsome.shop.auth.repository.mysql.po.user.UserPO;
import com.awsome.shop.auth.repository.user.UserRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/**
 * User 仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public UserEntity findByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        UserPO po = userMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public UserEntity findByEmail(String email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email);
        UserPO po = userMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public UserEntity findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public void save(UserEntity entity) {
        UserPO po = toPO(entity);
        userMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void update(UserEntity entity) {
        UserPO po = toPO(entity);
        userMapper.updateById(po);
    }

    @Override
    public PageResult<UserEntity> page(int page, int size, String username, String role, String status) {
        IPage<UserPO> result = userMapper.selectPage(new Page<>(page, size), username, role, status);

        PageResult<UserEntity> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    private UserEntity toEntity(UserPO po) {
        UserEntity entity = new UserEntity();
        entity.setId(po.getId());
        entity.setUsername(po.getUsername());
        entity.setEmail(po.getEmail());
        entity.setPasswordHash(po.getPasswordHash());
        entity.setNickname(po.getNickname());
        entity.setRole(po.getRole());
        entity.setStatus(po.getStatus());
        entity.setFailedLoginAttempts(po.getFailedLoginAttempts());
        entity.setLockExpiredAt(po.getLockExpiredAt());
        entity.setLastLoginAt(po.getLastLoginAt());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private UserPO toPO(UserEntity entity) {
        UserPO po = new UserPO();
        po.setId(entity.getId());
        po.setUsername(entity.getUsername());
        po.setEmail(entity.getEmail());
        po.setPasswordHash(entity.getPasswordHash());
        po.setNickname(entity.getNickname());
        po.setRole(entity.getRole());
        po.setStatus(entity.getStatus());
        po.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        po.setLockExpiredAt(entity.getLockExpiredAt());
        po.setLastLoginAt(entity.getLastLoginAt());
        return po;
    }
}
