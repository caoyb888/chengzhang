package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenzhang.thesis.user.domain.dto.AssignUserRoleDTO;
import com.chenzhang.thesis.user.domain.entity.UserRole;
import com.chenzhang.thesis.user.mapper.UserRoleMapper;
import com.chenzhang.thesis.user.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(AssignUserRoleDTO dto) {
        Long userId = dto.getUserId();
        List<Long> roleIds = dto.getRoleIds();

        // 删除现有角色
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        // 插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            Long schoolId = getCurrentSchoolId();
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setSchoolId(schoolId);
                userRole.setGrantedBy(currentUserId);
                userRole.setGrantedAt(LocalDateTime.now());
                userRole.setCreatedAt(LocalDateTime.now());
                userRole.setUpdatedAt(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
    }

    private Long getCurrentSchoolId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object schoolId = StpUtil.getSession().get("schoolId");
        return schoolId == null ? null : Long.valueOf(schoolId.toString());
    }
}
