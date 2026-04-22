package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenzhang.thesis.user.domain.dto.AssignUserRoleDTO;
import com.chenzhang.thesis.user.domain.entity.UserRole;
import com.chenzhang.thesis.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：用户角色分配 Service 测试
 * 覆盖：批量分配、空列表、null 列表、字段正确性、未登录降级
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleServiceImpl 用户角色服务测试")
class UserRoleServiceImplTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    @Test
    @DisplayName("TC-USERROLE-001: 分配 3 个角色 — 先删除旧角色，再插入 3 条新记录")
    void assignUserRoles_with3Roles_deletesOldAndInserts3New() {
        AssignUserRoleDTO dto = new AssignUserRoleDTO();
        dto.setUserId(100L);
        dto.setRoleIds(Arrays.asList(10L, 20L, 30L));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            SaSession mockSession = mock(SaSession.class);
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            stp.when(StpUtil::getSession).thenReturn(mockSession);
            when(mockSession.get("schoolId")).thenReturn(1L);

            userRoleService.assignUserRoles(dto);

            verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
            verify(userRoleMapper, times(3)).insert(any(UserRole.class));
        }
    }

    @Test
    @DisplayName("TC-USERROLE-002: 分配空角色列表 — 只删除旧角色，不插入任何记录")
    void assignUserRoles_withEmptyRoles_onlyDeletes() {
        AssignUserRoleDTO dto = new AssignUserRoleDTO();
        dto.setUserId(100L);
        dto.setRoleIds(Collections.emptyList());

        userRoleService.assignUserRoles(dto);

        verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, never()).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("TC-USERROLE-003: 分配 null 角色列表 — 只删除旧角色，不插入任何记录")
    void assignUserRoles_withNullRoles_onlyDeletes() {
        AssignUserRoleDTO dto = new AssignUserRoleDTO();
        dto.setUserId(100L);
        dto.setRoleIds(null);

        userRoleService.assignUserRoles(dto);

        verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, never()).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("TC-USERROLE-004: 分配角色 — userId / roleId / schoolId / grantedBy 均正确写入")
    void assignUserRoles_setsCorrectFields() {
        List<UserRole> captured = new ArrayList<>();
        doAnswer(inv -> {
            captured.add(inv.getArgument(0));
            return 1;
        }).when(userRoleMapper).insert(any(UserRole.class));

        AssignUserRoleDTO dto = new AssignUserRoleDTO();
        dto.setUserId(200L);
        dto.setRoleIds(List.of(50L));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            SaSession mockSession = mock(SaSession.class);
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(99L);
            stp.when(StpUtil::getSession).thenReturn(mockSession);
            when(mockSession.get("schoolId")).thenReturn(2L);

            userRoleService.assignUserRoles(dto);
        }

        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getUserId()).isEqualTo(200L);
        assertThat(captured.get(0).getRoleId()).isEqualTo(50L);
        assertThat(captured.get(0).getSchoolId()).isEqualTo(2L);
        assertThat(captured.get(0).getGrantedBy()).isEqualTo(99L);
    }

    @Test
    @DisplayName("TC-USERROLE-005: 未登录时分配角色 — grantedBy 和 schoolId 均为 null")
    void assignUserRoles_notLoggedIn_nullGrantedByAndSchoolId() {
        List<UserRole> captured = new ArrayList<>();
        doAnswer(inv -> {
            captured.add(inv.getArgument(0));
            return 1;
        }).when(userRoleMapper).insert(any(UserRole.class));

        AssignUserRoleDTO dto = new AssignUserRoleDTO();
        dto.setUserId(300L);
        dto.setRoleIds(List.of(70L));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);

            userRoleService.assignUserRoles(dto);
        }

        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getGrantedBy()).isNull();
        assertThat(captured.get(0).getSchoolId()).isNull();
    }
}
