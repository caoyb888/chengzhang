package com.chenzhang.thesis.auth.service;

import com.chenzhang.thesis.auth.domain.dto.SmsCodeSendDTO;

public interface SmsCodeService {

    void sendSmsCode(SmsCodeSendDTO dto);

    boolean verifySmsCode(String phone, String smsCode);
}
