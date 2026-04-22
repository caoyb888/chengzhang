package com.chenzhang.thesis.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.chenzhang.thesis.auth.domain.dto.LoginByPasswordDTO;
import com.chenzhang.thesis.auth.domain.dto.LoginBySmsDTO;
import com.chenzhang.thesis.auth.domain.dto.RefreshTokenDTO;
import com.chenzhang.thesis.auth.domain.entity.RefreshToken;
import com.chenzhang.thesis.auth.domain.entity.User;
import com.chenzhang.thesis.auth.domain.vo.LoginVO;
import com.chenzhang.thesis.auth.mapper.RefreshTokenMapper;
import com.chenzhang.thesis.auth.mapper.UserMapper;
import com.chenzhang.thesis.auth.service.LoginLogService;
import com.chenzhang.thesis.auth.service.SmsCodeService;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §2.2.1 / §4.2：认证鉴权核心 Service 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 认证服务测试")
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private LoginLogService loginLogService;
    @Mock
    private SmsCodeService smsCodeService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenTimeout", 86400L);
    }

    @Test
    @DisplayName("TC-AUTH-001: 密码登录正向 — 返回有效 Token 和用户信息")
    void loginByPassword_success_returnsLoginVO() {
        User user = activeUser();
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.login(user.getId())).thenAnswer(i -> null);
            stp.when(StpUtil::getTokenValue).thenReturn("mock_access_token");

            LoginByPasswordDTO dto = new LoginByPasswordDTO();
            dto.setUsername("admin");
            dto.setPassword("Admin@123456");

            LoginVO vo = authService.loginByPassword(dto, "127.0.0.1", "Mozilla/5.0");

            assertThat(vo.getAccessToken()).isEqualTo("mock_access_token");
            assertThat(vo.getRefreshToken()).isNotNull();
            assertThat(vo.getExpiresIn()).isEqualTo(86400L);
            assertThat(vo.getUserInfo().getUserId()).isEqualTo(user.getId());
            verify(refreshTokenMapper).insert(any(RefreshToken.class));
            verify(loginLogService).saveLoginLog(any());
        }
    }

    @Test
    @DisplayName("TC-AUTH-002: 密码登录 — 用户名不存在 → USER_NOT_FOUND")
    void loginByPassword_userNotFound_throwsException() {
        when(userMapper.selectByUsername("notexist")).thenReturn(null);

        LoginByPasswordDTO dto = new LoginByPasswordDTO();
        dto.setUsername("notexist");
        dto.setPassword("any");

        assertThatThrownBy(() -> authService.loginByPassword(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode()));

        verify(loginLogService).saveLoginLog(any());
    }

    @Test
    @DisplayName("TC-AUTH-003: 密码登录 — 账号已锁定 → USER_LOCKED")
    void loginByPassword_lockedUser_throwsException() {
        User user = activeUser();
        user.setStatus("LOCKED");
        user.setLockUntil(LocalDateTime.now().plusMinutes(30));
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        LoginByPasswordDTO dto = new LoginByPasswordDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin@123456");

        assertThatThrownBy(() -> authService.loginByPassword(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_LOCKED.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-004: 密码登录 — 账号已禁用 → USER_DISABLED")
    void loginByPassword_disabledUser_throwsException() {
        User user = activeUser();
        user.setStatus("DISABLED");
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        LoginByPasswordDTO dto = new LoginByPasswordDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin@123456");

        assertThatThrownBy(() -> authService.loginByPassword(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_DISABLED.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-005: 密码登录 — 密码错误 → USER_PASSWORD_ERROR")
    void loginByPassword_wrongPassword_throwsException() {
        User user = activeUser();
        user.setPasswordHash(BCrypt.hashpw("correct", BCrypt.gensalt()));
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        LoginByPasswordDTO dto = new LoginByPasswordDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.loginByPassword(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_PASSWORD_ERROR.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-006: 短信登录正向 — 验证码正确且用户存在")
    void loginBySms_success_returnsLoginVO() {
        when(smsCodeService.verifySmsCode("13800138000", "123456")).thenReturn(true);
        User user = activeUser();
        when(userMapper.selectByPhoneHash(anyString())).thenReturn(user);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.login(user.getId())).thenAnswer(i -> null);
            stp.when(StpUtil::getTokenValue).thenReturn("mock_access_token");

            LoginBySmsDTO dto = new LoginBySmsDTO();
            dto.setPhone("13800138000");
            dto.setSmsCode("123456");

            LoginVO vo = authService.loginBySms(dto, "127.0.0.1", "ua");

            assertThat(vo.getAccessToken()).isEqualTo("mock_access_token");
            verify(refreshTokenMapper).insert(any(RefreshToken.class));
        }
    }

    @Test
    @DisplayName("TC-AUTH-007: 短信登录 — 验证码错误 → SMS_CODE_ERROR")
    void loginBySms_wrongCode_throwsException() {
        when(smsCodeService.verifySmsCode("13800138000", "000000")).thenReturn(false);

        LoginBySmsDTO dto = new LoginBySmsDTO();
        dto.setPhone("13800138000");
        dto.setSmsCode("000000");

        assertThatThrownBy(() -> authService.loginBySms(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.SMS_CODE_ERROR.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-008: Token 刷新正向 — 有效 RefreshToken 返回新 AccessToken")
    void refreshToken_valid_returnsNewLoginVO() {
        RefreshToken rt = validRefreshToken();
        when(refreshTokenMapper.selectByTokenValue("valid_rt")).thenReturn(rt);
        User user = activeUser();
        when(userMapper.selectById(rt.getUserId())).thenReturn(user);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.login(user.getId())).thenAnswer(i -> null);
            stp.when(StpUtil::getTokenValue).thenReturn("new_access_token");

            RefreshTokenDTO dto = new RefreshTokenDTO();
            dto.setRefreshToken("valid_rt");

            LoginVO vo = authService.refreshToken(dto);

            assertThat(vo.getAccessToken()).isEqualTo("new_access_token");
            assertThat(vo.getRefreshToken()).isEqualTo("valid_rt");
            verify(refreshTokenMapper).updateById(rt);
        }
    }

    @Test
    @DisplayName("TC-AUTH-009: Token 刷新 — RefreshToken 已吊销 → UNAUTHORIZED")
    void refreshToken_revoked_throwsException() {
        RefreshToken rt = validRefreshToken();
        rt.setIsRevoked(1);
        when(refreshTokenMapper.selectByTokenValue("revoked_rt")).thenReturn(rt);

        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("revoked_rt");

        assertThatThrownBy(() -> authService.refreshToken(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-010: Token 刷新 — RefreshToken 已过期 → UNAUTHORIZED")
    void refreshToken_expired_throwsException() {
        RefreshToken rt = validRefreshToken();
        rt.setExpireAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenMapper.selectByTokenValue("expired_rt")).thenReturn(rt);

        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("expired_rt");

        assertThatThrownBy(() -> authService.refreshToken(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-011: Token 刷新 — 用户不存在或已禁用 → USER_NOT_FOUND")
    void refreshToken_userInactive_throwsException() {
        RefreshToken rt = validRefreshToken();
        when(refreshTokenMapper.selectByTokenValue("valid_rt")).thenReturn(rt);
        when(userMapper.selectById(rt.getUserId())).thenReturn(null);

        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("valid_rt");

        assertThatThrownBy(() -> authService.refreshToken(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("TC-AUTH-012: 登出 — 吊销当前用户全部 RefreshToken")
    void logout_success_revokesTokens() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            stp.when(StpUtil::logout).thenAnswer(i -> null);
            when(refreshTokenMapper.update(any(), any())).thenReturn(1);

            authService.logout();

            verify(refreshTokenMapper).update(any(), any());
        }
    }

    @Test
    @DisplayName("TC-AUTH-013: 密码登录 — 账号锁定已过期但状态未恢复 → USER_DISABLED")
    void loginByPassword_lockExpiredButNotRestored_throwsDisabled() {
        User user = activeUser();
        user.setStatus("LOCKED");
        user.setLockUntil(LocalDateTime.now().minusMinutes(1));
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        LoginByPasswordDTO dto = new LoginByPasswordDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin@123456");

        assertThatThrownBy(() -> authService.loginByPassword(dto, "127.0.0.1", "ua"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.USER_DISABLED.getCode()));
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setSchoolId(1L);
        user.setUsername("admin");
        user.setRealName("管理员");
        user.setPasswordHash(BCrypt.hashpw("Admin@123456", BCrypt.gensalt()));
        user.setStatus("ACTIVE");
        user.setUserType("SCHOOL_ADMIN");
        return user;
    }

    private RefreshToken validRefreshToken() {
        RefreshToken rt = new RefreshToken();
        rt.setId(1L);
        rt.setUserId(1L);
        rt.setSchoolId(1L);
        rt.setTokenValue("valid_rt");
        rt.setIsRevoked(0);
        rt.setIsDeleted(0);
        rt.setExpireAt(LocalDateTime.now().plusDays(7));
        return rt;
    }
}
