package com.chenzhang.thesis.common.exception;

import com.chenzhang.thesis.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常（对应 CLAUDE.md §5.3）
 * 禁止在业务代码中 throw new RuntimeException()，统一使用 BusinessException
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String appendMessage) {
        super(resultCode.getMessage() + "：" + appendMessage);
        this.code = resultCode.getCode();
    }
}
