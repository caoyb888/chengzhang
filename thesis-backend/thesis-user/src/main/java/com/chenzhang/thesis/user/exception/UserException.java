package com.chenzhang.thesis.user.exception;

import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;

/**
 * 用户模块业务异常
 */
public class UserException extends BusinessException {

    public UserException(String message) {
        super(message);
    }

    public UserException(ResultCode resultCode) {
        super(resultCode);
    }

    public UserException(ResultCode resultCode, String appendMessage) {
        super(resultCode, appendMessage);
    }
}
