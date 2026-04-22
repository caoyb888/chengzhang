package com.chenzhang.thesis.auth.service;

import com.chenzhang.thesis.auth.domain.dto.LoginByPasswordDTO;
import com.chenzhang.thesis.auth.domain.dto.LoginBySmsDTO;
import com.chenzhang.thesis.auth.domain.dto.RefreshTokenDTO;
import com.chenzhang.thesis.auth.domain.vo.LoginVO;

public interface AuthService {

    LoginVO loginByPassword(LoginByPasswordDTO dto, String ip, String userAgent);

    LoginVO loginBySms(LoginBySmsDTO dto, String ip, String userAgent);

    LoginVO refreshToken(RefreshTokenDTO dto);

    void logout();
}
