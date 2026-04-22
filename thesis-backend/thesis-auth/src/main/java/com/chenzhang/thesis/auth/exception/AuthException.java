package com.chenzhang.thesis.auth.exception;

import com.chenzhang.thesis.common.exception.BusinessException;

/**
 * 认证授权模块业务异常，code 范围 10xx
 */
public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(1000, message);
    }

    public AuthException(int code, String message) {
        super(code, message);
    }
}
