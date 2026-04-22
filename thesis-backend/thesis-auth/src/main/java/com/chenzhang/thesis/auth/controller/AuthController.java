package com.chenzhang.thesis.auth.controller;

import com.chenzhang.thesis.auth.domain.dto.LoginByPasswordDTO;
import com.chenzhang.thesis.auth.domain.dto.LoginBySmsDTO;
import com.chenzhang.thesis.auth.domain.dto.RefreshTokenDTO;
import com.chenzhang.thesis.auth.domain.dto.SmsCodeSendDTO;
import com.chenzhang.thesis.auth.domain.vo.LoginVO;
import com.chenzhang.thesis.auth.service.AuthService;
import com.chenzhang.thesis.auth.service.SmsCodeService;
import com.chenzhang.thesis.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
@Tag(name = "认证授权接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SmsCodeService smsCodeService;

    @PostMapping("/login/password")
    @Operation(summary = "密码登录")
    public Result<LoginVO> loginByPassword(@RequestBody @Valid LoginByPasswordDTO dto,
                                           HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return Result.ok(authService.loginByPassword(dto, ip, ua));
    }

    @PostMapping("/login/sms")
    @Operation(summary = "短信验证码登录")
    public Result<LoginVO> loginBySms(@RequestBody @Valid LoginBySmsDTO dto,
                                      HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return Result.ok(authService.loginBySms(dto, ip, ua));
    }

    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码")
    public Result<Void> sendSmsCode(@RequestBody @Valid SmsCodeSendDTO dto) {
        smsCodeService.sendSmsCode(dto);
        return Result.ok();
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "刷新访问令牌")
    public Result<LoginVO> refreshToken(@RequestBody @Valid RefreshTokenDTO dto) {
        return Result.ok(authService.refreshToken(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
