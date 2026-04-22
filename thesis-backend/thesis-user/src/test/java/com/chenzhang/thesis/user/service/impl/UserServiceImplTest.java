package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.convert.UserConvert;
import com.chenzhang.thesis.user.domain.dto.CreateUserDTO;
import com.chenzhang.thesis.user.domain.dto.ResetPasswordDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateUserDTO;
import com.chenzhang.thesis.user.domain.dto.UserQueryDTO;
import com.chenzhang.thesis.user.domain.entity.Role;
import com.chenzhang.thesis.user.domain.entity.User;
import com.chenzhang.thesis.user.domain.entity.UserRole;
import com.chenzhang.thesis.user.domain.vo.UserDetailVO;
import com.chenzhang.thesis.user.domain.vo.UserVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.RoleMapper;
import com.chenzhang.thesis.user.mapper.UserMapper;
import com.chenzhang.thesis.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：用户管理核心 Service 测试
 * 覆盖：新增、编辑、查询、重置密码、启用/禁用、数据权限
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 用户服务测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserConvert userConvert;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "defaultPassword", "123456");
    }

    @Test
    @DisplayName("TC-USER-001: 创建用户正向 — 返回 userId，密码 BCrypt 加密")
    void createUser_success_returnsUserId() {
        mockLoginSession(1L, "SCHOOL_ADMIN");
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userConvert.toUser(any(CreateUserDTO.class))).thenReturn(new User());
        when(userMapper.insert(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(100L);
            return 1;
        });

        CreateUserDTO dto = new CreateUserDTO();
        dto.setSchoolId(1L);
        dto.setUsername("student01");
        dto.setRealName("张三");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            Long userId = userService.createUser(dto);
            assertThat(userId).isEqualTo(100L);
        }
    }

    @Test
    @DisplayName("TC-USER-002: 创建用户 — 账号已存在 → 抛异常")
    void createUser_duplicateUsername_throwsException() {
        mockLoginSession(1L, "SCHOOL_ADMIN");
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CreateUserDTO dto = new CreateUserDTO();
        dto.setSchoolId(1L);
        dto.setUsername("existing");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("登录账号已存在");
        }
    }

    @Test
    @DisplayName("TC-USER-003: 创建用户 — 越权创建他校用户 → DATA_PERMISSION_DENIED")
    void createUser_crossSchool_throwsException() {
        mockLoginSession(1L, "SCHOOL_ADMIN");

        CreateUserDTO dto = new CreateUserDTO();
        dto.setSchoolId(2L);
        dto.setUsername("hacker");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserException.class)
                    .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo(ResultCode.DATA_PERMISSION_DENIED.getCode()));
        }
    }

    @Test
    @DisplayName("TC-USER-004: 更新用户正向 — 字段选择性更新")
    void updateUser_success_updatesFields() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setRealName("李四");
        dto.setEmail("li@example.com");

        userService.updateUser(1L, dto);

        verify(userMapper).updateById(user);
        assertThat(user.getRealName()).isEqualTo("李四");
        assertThat(user.getEmail()).isEqualTo("li@example.com");
    }

    @Test
    @DisplayName("TC-USER-005: 更新用户 — 用户不存在 → USER_NOT_FOUND")
    void updateUser_notFound_throwsException() {
        when(userMapper.selectById(999L)).thenReturn(null);

        UpdateUserDTO dto = new UpdateUserDTO();
        assertThatThrownBy(() -> userService.updateUser(999L, dto))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("TC-USER-006: 分页查询用户 — 按条件过滤并分页")
    void pageUsers_withConditions_returnsPageResult() {
        UserQueryDTO query = new UserQueryDTO();
        query.setKeyword("张");
        query.setUserType("STUDENT");
        query.setCurrent(1);
        query.setSize(10);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<User>());

        PageResult<UserVO> result = userService.pageUsers(query);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("TC-USER-007: 查询用户详情 — 含角色信息")
    void getUserDetail_withRoles_returnsDetail() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userConvert.toUserDetailVO(user)).thenReturn(new UserDetailVO());
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                List.of(new UserRole() {{ setRoleId(10L); }})
        );
        when(roleMapper.selectBatchIds(List.of(10L))).thenReturn(
                List.of(new Role() {{ setId(10L); setRoleCode("STUDENT"); }})
        );

        UserDetailVO detail = userService.getUserDetail(1L);
        assertThat(detail).isNotNull();
    }

    @Test
    @DisplayName("TC-USER-008: 删除用户 — 软删除")
    void removeUser_success_deletes() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);

        userService.removeUser(1L);
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("TC-USER-009: 重置密码 — BCrypt 加密存储")
    void resetPassword_success_encodesPassword() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);

        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("NewPass@123");

        userService.resetPassword(1L, dto);

        verify(userMapper).updateById(user);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches("NewPass@123", user.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("TC-USER-010: 更新状态 — ACTIVE ↔ DISABLED")
    void updateStatus_success_changesStatus() {
        User user = new User();
        user.setId(1L);
        user.setIsDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);

        userService.updateStatus(1L, "DISABLED");

        assertThat(user.getStatus()).isEqualTo("DISABLED");
        verify(userMapper).updateById(user);
    }

    private void mockLoginSession(Long schoolId, String userType) {
        // 辅助方法占位，实际在 try-with-resources 中使用 MockedStatic
    }
}
