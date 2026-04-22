package com.chenzhang.thesis.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.chenzhang.thesis.auth.domain.dto.SmsCodeSendDTO;
import com.chenzhang.thesis.auth.service.SmsCodeService;
import com.chenzhang.thesis.common.constant.CacheKeyConstant;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsCodeServiceImpl implements SmsCodeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long SMS_TTL_SECONDS = 300;
    private static final long SMS_COOLDOWN_SECONDS = 60;

    @Override
    public void sendSmsCode(SmsCodeSendDTO dto) {
        String phone = dto.getPhone();
        String cooldownKey = "sms:cooldown:" + phone;
        String codeKey = String.format(CacheKeyConstant.SMS_CODE, phone);

        String cooldown = redisTemplate.opsForValue().get(cooldownKey);
        if (cooldown != null) {
            throw new BusinessException(ResultCode.SMS_CODE_SEND_TOO_FREQUENT);
        }

        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(codeKey, code, SMS_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(cooldownKey, "1", SMS_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        log.info("[短信验证码] phone={}, purpose={}, code={}", maskPhone(phone), dto.getPurpose(), code);
        // TODO: 接入短信网关发送实际短信
    }

    @Override
    public boolean verifySmsCode(String phone, String smsCode) {
        String codeKey = String.format(CacheKeyConstant.SMS_CODE, phone);
        String cached = redisTemplate.opsForValue().get(codeKey);
        if (cached == null || !cached.equals(smsCode)) {
            return false;
        }
        redisTemplate.delete(codeKey);
        return true;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
