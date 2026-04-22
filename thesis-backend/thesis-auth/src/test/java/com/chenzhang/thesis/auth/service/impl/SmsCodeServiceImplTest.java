package com.chenzhang.thesis.auth.service.impl;

import com.chenzhang.thesis.auth.domain.dto.SmsCodeSendDTO;
import com.chenzhang.thesis.common.constant.CacheKeyConstant;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §2.2.1：短信验证码服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SmsCodeServiceImpl 短信验证码服务测试")
class SmsCodeServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SmsCodeServiceImpl smsCodeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("TC-SMS-001: 发送验证码正向 — 设置 5min TTL + 60s 冷却")
    void sendSmsCode_success_setsRedisKeys() {
        when(valueOperations.get("sms:cooldown:13800138000")).thenReturn(null);

        SmsCodeSendDTO dto = new SmsCodeSendDTO();
        dto.setPhone("13800138000");
        dto.setPurpose("LOGIN");

        smsCodeService.sendSmsCode(dto);

        verify(valueOperations).set(contains("sms:"), anyString(), eq(300L), eq(java.util.concurrent.TimeUnit.SECONDS));
        verify(valueOperations).set(eq("sms:cooldown:13800138000"), eq("1"), eq(60L), eq(java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("TC-SMS-002: 60s 内重复发送 → SMS_CODE_SEND_TOO_FREQUENT")
    void sendSmsCode_cooldownActive_throwsException() {
        when(valueOperations.get("sms:cooldown:13800138000")).thenReturn("1");

        SmsCodeSendDTO dto = new SmsCodeSendDTO();
        dto.setPhone("13800138000");

        assertThatThrownBy(() -> smsCodeService.sendSmsCode(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(ResultCode.SMS_CODE_SEND_TOO_FREQUENT.getCode()));
    }

    @Test
    @DisplayName("TC-SMS-003: 验证码校验正向 — 正确码返回 true 并删除缓存")
    void verifySmsCode_correct_returnsTrueAndDeletes() {
        String key = String.format(CacheKeyConstant.SMS_CODE, "13800138000");
        when(valueOperations.get(key)).thenReturn("123456");

        boolean result = smsCodeService.verifySmsCode("13800138000", "123456");

        assertThat(result).isTrue();
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("TC-SMS-004: 验证码校验 — 错误码返回 false")
    void verifySmsCode_wrong_returnsFalse() {
        String key = String.format(CacheKeyConstant.SMS_CODE, "13800138000");
        when(valueOperations.get(key)).thenReturn("123456");

        boolean result = smsCodeService.verifySmsCode("13800138000", "000000");

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(key);
    }

    @Test
    @DisplayName("TC-SMS-005: 验证码校验 — 缓存已过期返回 false")
    void verifySmsCode_expired_returnsFalse() {
        String key = String.format(CacheKeyConstant.SMS_CODE, "13800138000");
        when(valueOperations.get(key)).thenReturn(null);

        boolean result = smsCodeService.verifySmsCode("13800138000", "123456");

        assertThat(result).isFalse();
    }
}
