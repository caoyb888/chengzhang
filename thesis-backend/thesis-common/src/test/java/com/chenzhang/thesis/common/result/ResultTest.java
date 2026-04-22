package com.chenzhang.thesis.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：Result<T> 构造方法覆盖测试
 * 验收标准（S0-05）：Result 构造单元测试通过
 */
@DisplayName("Result 统一响应体测试")
class ResultTest {

    @Test
    @DisplayName("TC-RESULT-001: ok() 无数据 — code=200, data=null")
    void ok_noData_returns200WithNullData() {
        Result<Void> result = Result.ok();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isNull();
        assertThat(result.getTimestamp()).isPositive();
    }

    @Test
    @DisplayName("TC-RESULT-002: ok(data) — code=200, data 正确回填")
    void ok_withData_returnsDataCorrectly() {
        String payload = "论文ID:12345";
        Result<String> result = Result.ok(payload);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(payload);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("TC-RESULT-003: ok(data, message) — 自定义 message 覆盖默认值")
    void ok_withDataAndMessage_usesCustomMessage() {
        Result<Integer> result = Result.ok(42, "导入完成，共处理42条");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("导入完成，共处理42条");
        assertThat(result.getData()).isEqualTo(42);
    }

    @Test
    @DisplayName("TC-RESULT-004: fail(code, message) — 失败响应 data=null")
    void fail_withCodeAndMessage_returnsFailureWithNullData() {
        Result<Void> result = Result.fail(400, "请求参数错误");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("请求参数错误");
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("TC-RESULT-005: fail(ResultCode) — 使用枚举构造失败响应")
    void fail_withResultCode_usesEnumCodeAndMessage() {
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);

        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMessage()).isEqualTo("未登录或登录已过期");
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("TC-RESULT-006: timestamp 在合理范围内（当前时间前后1分钟）")
    void result_timestamp_isCurrentTime() {
        long before = System.currentTimeMillis() - 60_000;
        Result<Void> result = Result.ok();
        long after = System.currentTimeMillis() + 60_000;

        assertThat(result.getTimestamp()).isBetween(before, after);
    }

    @Test
    @DisplayName("TC-RESULT-007: 失败响应 isSuccess() 始终为 false")
    void fail_allErrorCodes_isSuccessFalse() {
        int[] errorCodes = {400, 401, 403, 404, 500, 503, 1001, 2001, 3001};
        for (int code : errorCodes) {
            Result<Void> result = Result.fail(code, "error");
            assertThat(result.isSuccess())
                    .as("code=%d 应返回 isSuccess=false", code)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("TC-RESULT-008: Result 泛型支持复杂对象")
    void ok_withComplexGenericType_doesNotLoseTypeInfo() {
        PageResult<String> page = new PageResult<>();
        page.setTotal(100L);
        page.setCurrent(1);
        page.setSize(20);

        Result<PageResult<String>> result = Result.ok(page);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getTotal()).isEqualTo(100L);
    }
}
