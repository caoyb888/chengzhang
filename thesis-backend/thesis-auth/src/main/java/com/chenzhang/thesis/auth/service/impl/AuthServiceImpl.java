package com.chenzhang.thesis.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chenzhang.thesis.auth.domain.dto.LoginByPasswordDTO;
import com.chenzhang.thesis.auth.domain.dto.LoginBySmsDTO;
import com.chenzhang.thesis.auth.domain.dto.RefreshTokenDTO;
import com.chenzhang.thesis.auth.domain.entity.LoginLog;
import com.chenzhang.thesis.auth.domain.entity.RefreshToken;
import com.chenzhang.thesis.auth.domain.entity.User;
import com.chenzhang.thesis.auth.domain.vo.LoginVO;
import com.chenzhang.thesis.auth.domain.vo.UserInfoVO;
import com.chenzhang.thesis.auth.mapper.RefreshTokenMapper;
import com.chenzhang.thesis.auth.mapper.UserMapper;
import com.chenzhang.thesis.auth.service.AuthService;
import com.chenzhang.thesis.auth.service.LoginLogService;
import com.chenzhang.thesis.auth.service.SmsCodeService;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private SmsCodeService smsCodeService;

    @Value("${sa-token.timeout:86400}")
    private long accessTokenTimeout;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByPassword(LoginByPasswordDTO dto, String ip, String userAgent) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            saveLoginLog(null, null, dto.getUsername(), "PASSWORD", "FAIL_NOT_EXIST", ip, userAgent, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if ("LOCKED".equals(user.getStatus())) {
            if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
                saveLoginLog(user.getId(), user.getSchoolId(), dto.getUsername(), "PASSWORD", "FAIL_LOCKED", ip, userAgent, "账号已锁定");
                throw new BusinessException(ResultCode.USER_LOCKED);
            }
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            saveLoginLog(user.getId(), user.getSchoolId(), dto.getUsername(), "PASSWORD", "FAIL_LOCKED", ip, userAgent, "账号状态异常：" + user.getStatus());
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!BCrypt.checkpw(dto.getPassword(), user.getPasswordHash())) {
            saveLoginLog(user.getId(), user.getSchoolId(), dto.getUsername(), "PASSWORD", "FAIL_PWD", ip, userAgent, "密码错误");
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setSchoolId(user.getSchoolId());
        refreshToken.setTokenValue(refreshTokenValue);
        refreshToken.setClientType(dto.getClientType());
        refreshToken.setIpAddress(ip);
        refreshToken.setDeviceInfo(userAgent);
        refreshToken.setExpireAt(LocalDateTime.now().plusDays(7));
        refreshToken.setIsRevoked(0);
        refreshTokenMapper.insert(refreshToken);

        UserInfoVO userInfo = buildUserInfoVO(user);
        saveLoginLog(user.getId(), user.getSchoolId(), dto.getUsername(), "PASSWORD", "SUCCESS", ip, userAgent, null);

        log.info("[密码登录] userId={}, username={}, ip={}", user.getId(), dto.getUsername(), ip);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshTokenValue);
        vo.setExpiresIn(accessTokenTimeout);
        vo.setUserInfo(userInfo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginBySms(LoginBySmsDTO dto, String ip, String userAgent) {
        boolean valid = smsCodeService.verifySmsCode(dto.getPhone(), dto.getSmsCode());
        if (!valid) {
            saveLoginLog(null, null, dto.getPhone(), "SMS", "FAIL_PWD", ip, userAgent, "验证码错误");
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }

        String phoneHash = DigestUtil.sha256Hex(dto.getPhone());
        User user = userMapper.selectByPhoneHash(phoneHash);
        if (user == null) {
            saveLoginLog(null, null, dto.getPhone(), "SMS", "FAIL_NOT_EXIST", ip, userAgent, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            saveLoginLog(user.getId(), user.getSchoolId(), dto.getPhone(), "SMS", "FAIL_LOCKED", ip, userAgent, "账号状态异常：" + user.getStatus());
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setSchoolId(user.getSchoolId());
        refreshToken.setTokenValue(refreshTokenValue);
        refreshToken.setClientType(dto.getClientType());
        refreshToken.setIpAddress(ip);
        refreshToken.setDeviceInfo(userAgent);
        refreshToken.setExpireAt(LocalDateTime.now().plusDays(7));
        refreshToken.setIsRevoked(0);
        refreshTokenMapper.insert(refreshToken);

        UserInfoVO userInfo = buildUserInfoVO(user);
        saveLoginLog(user.getId(), user.getSchoolId(), dto.getPhone(), "SMS", "SUCCESS", ip, userAgent, null);

        log.info("[短信登录] userId={}, phone={}, ip={}", user.getId(), maskPhone(dto.getPhone()), ip);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshTokenValue);
        vo.setExpiresIn(accessTokenTimeout);
        vo.setUserInfo(userInfo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO refreshToken(RefreshTokenDTO dto) {
        RefreshToken rt = refreshTokenMapper.selectByTokenValue(dto.getRefreshToken());
        if (rt == null || rt.getIsRevoked() == 1 || rt.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已吊销");
        }
        if (rt.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌已过期");
        }

        User user = userMapper.selectById(rt.getUserId());
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        StpUtil.login(user.getId());
        String newAccessToken = StpUtil.getTokenValue();

        rt.setExpireAt(LocalDateTime.now().plusDays(7));
        refreshTokenMapper.updateById(rt);

        UserInfoVO userInfo = buildUserInfoVO(user);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(newAccessToken);
        vo.setRefreshToken(rt.getTokenValue());
        vo.setExpiresIn(accessTokenTimeout);
        vo.setUserInfo(userInfo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout() {
        long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();

        refreshTokenMapper.update(null, new UpdateWrapper<RefreshToken>()
                .eq("user_id", userId)
                .eq("is_revoked", 0)
                .set("is_revoked", 1));

        log.info("[登出] userId={}", userId);
    }

    private UserInfoVO buildUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setSchoolId(user.getSchoolId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setUserType(user.getUserType());
        vo.setTeachingPointId(user.getTeachingPointId());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPermissions(userMapper.selectPermissionsByUserId(user.getId()));
        return vo;
    }

    private void saveLoginLog(Long userId, Long schoolId, String account, String type,
                              String result, String ip, String ua, String failReason) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setSchoolId(schoolId);
        log.setLoginAccount(account);
        log.setLoginType(type);
        log.setLoginResult(result);
        log.setIpAddress(ip);
        log.setUserAgent(ua);
        log.setFailReason(failReason);
        loginLogService.saveLoginLog(log);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
