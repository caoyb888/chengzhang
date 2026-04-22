package com.chenzhang.thesis.paper.exception;

import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.common.result.ResultCode;

/**
 * 论文模块业务异常
 */
public class PaperException extends BusinessException {

    public PaperException(String message) {
        super(message);
    }

    public PaperException(int code, String message) {
        super(code, message);
    }

    public PaperException(ResultCode resultCode) {
        super(resultCode);
    }

    public PaperException(ResultCode resultCode, String appendMessage) {
        super(resultCode, appendMessage);
    }
}
