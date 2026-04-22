package com.chenzhang.thesis.auth.domain.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfoVO userInfo;
}
