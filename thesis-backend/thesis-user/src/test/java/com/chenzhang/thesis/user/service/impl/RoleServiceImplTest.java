package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.domain.dto.CreateRoleDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateRolePermissionsDTO;
import com.chenzhang.thesis.user.domain.entity.Role;
import com.chenzhang.thesis.user.domain.entity.RolePermission;
import com.chenzhang.thesis.user.domain.vo.RoleVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.RoleMapper;
import com.chenzhang.thesis.user.mapper.RolePermissionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2 / §2.2.1：RBAC 角色权限 Service 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl 角色服务测试")
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("TC-ROLE-001: 创建角色正向 — 返回 roleId")
    void createRole_success_returnsRoleId() {
        mockSchoolAdminSession(1L);
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(roleMapper.insert(any(Role.class))).thenAnswer(i -> {
            Role r = i.getArgument(0);
            r.setId(10L);
            return 1;
        });

        CreateRoleDTO dto = new CreateRoleDTO();
        dto.setSchoolId(1L);
        dto.setRoleCode("CUSTOM_ROLE");
        dto.setRoleName("自定义角色");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            Long roleId = roleService.createRole(dto);
            assertThat(roleId).isEqualTo(10L);
        }
    }

    @Test
    @DisplayName("TC-ROLE-002: 创建角色 — 编码重复 → 抛异常")
    void createRole_duplicateCode_throwsException() {
        mockSchoolAdminSession(1L);
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CreateRoleDTO dto = new CreateRoleDTO();
        dto.setSchoolId(1L);
        dto.setRoleCode("EXIST");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            assertThatThrownBy(() -> roleService.createRole(dto))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("角色编码已存在");
        }
    }

    @Test
    @DisplayName("TC-ROLE-003: 更新预置角色 → 不允许修改")
    void updateRole_presetRole_throwsException() {
        Role role = new Role();
        role.setId(1L);
        role.setIsPreset(1);
        role.setIsDeleted(0);
        when(roleMapper.selectById(1L)).thenReturn(role);

        CreateRoleDTO dto = new CreateRoleDTO();
        assertThatThrownBy(() -> roleService.updateRole(1L, dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("预置角色不允许修改");
    }

    @Test
    @DisplayName("TC-ROLE-004: 删除预置角色 → 不允许删除")
    void removeRole_presetRole_throwsException() {
        Role role = new Role();
        role.setId(1L);
        role.setIsPreset(1);
        role.setIsDeleted(0);
        when(roleMapper.selectById(1L)).thenReturn(role);

        assertThatThrownBy(() -> roleService.removeRole(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("预置角色不允许删除");
    }

    @Test
    @DisplayName("TC-ROLE-005: 分配角色权限 — 删除旧权限 + 插入新权限")
    void updateRolePermissions_success_replacesPermissions() {
        Role role = new Role();
        role.setId(1L);
        role.setIsDeleted(0);
        when(roleMapper.selectById(1L)).thenReturn(role);

        UpdateRolePermissionsDTO dto = new UpdateRolePermissionsDTO();
        dto.setPermIds(Arrays.asList(101L, 102L, 103L));

        roleService.updateRolePermissions(1L, dto);

        verify(rolePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, times(3)).insert(any(RolePermission.class));
    }

    @Test
    @DisplayName("TC-ROLE-006: 应用角色套餐 — 复制预置角色权限到目标角色")
    void applyPresetRole_success_copiesPermissions() {
        Role preset = new Role();
        preset.setId(1L);
        preset.setRoleCode("SCHOOL_ADMIN");
        preset.setSchoolId(0L);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(preset);

        Role target = new Role();
        target.setId(2L);
        target.setIsDeleted(0);
        when(roleMapper.selectById(2L)).thenReturn(target);

        when(rolePermissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                Arrays.asList(
                        new RolePermission() {{ setPermId(10L); }},
                        new RolePermission() {{ setPermId(20L); }}
                )
        );

        roleService.applyPresetRole("SCHOOL_ADMIN", 2L);

        verify(rolePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, times(2)).insert(any(RolePermission.class));
    }

    @Test
    @DisplayName("TC-ROLE-007: 应用无效角色套餐 → 抛异常")
    void applyPresetRole_invalidCode_throwsException() {
        assertThatThrownBy(() -> roleService.applyPresetRole("INVALID_CODE", 2L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("无效的角色套餐编码");
    }

    @Test
    @DisplayName("TC-ROLE-008: 初始化预置角色 — 缺失则自动创建")
    void initPresetRoles_createsMissing() {
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        roleService.initPresetRoles();

        verify(roleMapper, times(5)).insert(any(Role.class));
    }

    @Test
    @DisplayName("TC-ROLE-009: 初始化预置角色 — 已存在则跳过")
    void initPresetRoles_existing_skips() {
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        roleService.initPresetRoles();

        verify(roleMapper, never()).insert(any(Role.class));
    }

    @Test
    @DisplayName("TC-ROLE-010: 查询活跃角色列表")
    void listActiveRoles_returnsActiveList() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                Arrays.asList(
                        new Role() {{ setId(1L); setRoleCode("SCHOOL_ADMIN"); }},
                        new Role() {{ setId(2L); setRoleCode("TEACHER"); }}
                )
        );
        when(rolePermissionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        List<RoleVO> list = roleService.listActiveRoles();
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("TC-ROLE-011: 分页查询角色")
    void pageRoles_withKeyword_returnsPage() {
        when(roleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Role>());

        PageResult<RoleVO> result = roleService.pageRoles(1, 10, "admin");
        assertThat(result).isNotNull();
    }

    private void mockSchoolAdminSession(Long schoolId) {
        // 占位
    }
}
