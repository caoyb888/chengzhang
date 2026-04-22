package com.chenzhang.thesis.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginBySmsDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    @NotBlank(message = "客户端类型不能为空")
    private String clientType;
}
