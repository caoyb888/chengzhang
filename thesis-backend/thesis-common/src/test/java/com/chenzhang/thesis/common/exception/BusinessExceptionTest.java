package com.chenzhang.thesis.common.exception;

import com.chenzhang.thesis.common.result.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：BusinessException 所有构造器分支覆盖
 */
@DisplayName("BusinessException 业务异常测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("TC-BEXC-001: String 构造器 — 默认 code=400")
    void stringConstructor_defaultCode400() {
        BusinessException ex = new BusinessException("参数不合法");

        assertThat(ex.getCode()).isEqualTo(400);
        assertThat(ex.getMessage()).isEqualTo("参数不合法");
    }

    @Test
    @DisplayName("TC-BEXC-002: (int code, String message) 构造器 — 自定义错误码")
    void codeMessageConstructor_customCode() {
        BusinessException ex = new BusinessException(2001, "前置节点尚未完成");

        assertThat(ex.getCode()).isEqualTo(2001);
        assertThat(ex.getMessage()).isEqualTo("前置节点尚未完成");
    }

    @Test
    @DisplayName("TC-BEXC-003: ResultCode 构造器 — 使用枚举 code 和 message")
    void resultCodeConstructor_usesEnumValues() {
        BusinessException ex = new BusinessException(ResultCode.PAPER_TOPIC_TAKEN);

        assertThat(ex.getCode()).isEqualTo(ResultCode.PAPER_TOPIC_TAKEN.getCode());
        assertThat(ex.getMessage()).isEqualTo(ResultCode.PAPER_TOPIC_TAKEN.getMessage());
    }

    @Test
    @DisplayName("TC-BEXC-004: (ResultCode, appendMessage) 构造器 — message 拼接正确")
    void resultCodeWithAppend_concatenatesMessage() {
        BusinessException ex = new BusinessException(ResultCode.PAPER_COMMENT_TOO_SHORT, "最少需要50字");

        assertThat(ex.getCode()).isEqualTo(ResultCode.PAPER_COMMENT_TOO_SHORT.getCode());
        assertThat(ex.getMessage()).contains(ResultCode.PAPER_COMMENT_TOO_SHORT.getMessage());
        assertThat(ex.getMessage()).contains("最少需要50字");
    }

    @Test
    @DisplayName("TC-BEXC-005: BusinessException 是 RuntimeException 子类（不强制 try-catch）")
    void businessException_isRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("TC-BEXC-006: 各论文业务错误码均可通过 ResultCode 枚举构造")
    void allPaperErrorCodes_canBeConstructedViaEnum() {
        ResultCode[] paperCodes = {
            ResultCode.PAPER_NODE_NOT_READY,
            ResultCode.PAPER_DEADLINE_EXPIRED,
            ResultCode.PAPER_TOPIC_TAKEN,
            ResultCode.PAPER_COMMENT_TOO_SHORT
        };
        for (ResultCode rc : paperCodes) {
            BusinessException ex = new BusinessException(rc);
            assertThat(ex.getCode()).isEqualTo(rc.getCode());
            assertThat(ex.getMessage()).isNotBlank();
        }
    }
}
