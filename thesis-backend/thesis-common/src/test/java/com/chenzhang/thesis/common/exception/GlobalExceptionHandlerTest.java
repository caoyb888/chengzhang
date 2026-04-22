package com.chenzhang.thesis.common.exception;

import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试计划 §4.4：GlobalExceptionHandler 所有异常分支覆盖
 * 不依赖 Spring 上下文，直接调用 handler 方法（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("TC-HANDLER-001: BusinessException → code 和 message 原样返回")
    void handleBusiness_returnsExceptionCodeAndMessage() {
        BusinessException ex = new BusinessException(2001, "前置节点尚未完成");

        Result<Void> result = handler.handleBusiness(ex);

        assertThat(result.getCode()).isEqualTo(2001);
        assertThat(result.getMessage()).isEqualTo("前置节点尚未完成");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("TC-HANDLER-002: BusinessException(ResultCode) → 枚举 code 透传")
    void handleBusiness_withResultCodeEnum_returnsEnumCode() {
        BusinessException ex = new BusinessException(ResultCode.PAPER_TOPIC_TAKEN);

        Result<Void> result = handler.handleBusiness(ex);

        assertThat(result.getCode()).isEqualTo(ResultCode.PAPER_TOPIC_TAKEN.getCode());
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("TC-HANDLER-003: 未知 Exception → code=500，不暴露内部信息")
    void handleUnknown_returnsGeneric500WithoutInternalDetails() {
        Exception ex = new RuntimeException("数据库连接失败: host=db-core, port=3306");

        Result<Void> result = handler.handleUnknown(ex);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo(ResultCode.INTERNAL_ERROR.getMessage());
        // 关键：不暴露内部异常信息给前端
        assertThat(result.getMessage()).doesNotContain("db-core");
        assertThat(result.getMessage()).doesNotContain("3306");
    }

    @Test
    @DisplayName("TC-HANDLER-004: Sa-Token NotLoginException → code=401")
    void handleNotLogin_returnsUnauthorized401() {
        cn.dev33.satoken.exception.NotLoginException ex =
                mock(cn.dev33.satoken.exception.NotLoginException.class);
        when(ex.getMessage()).thenReturn("token-timeout");

        Result<Void> result = handler.handleNotLogin(ex);

        assertThat(result.getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode());
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("TC-HANDLER-005: Sa-Token NotPermissionException → code=403")
    void handleNotPermission_returnsForbidden403() {
        cn.dev33.satoken.exception.NotPermissionException ex =
                mock(cn.dev33.satoken.exception.NotPermissionException.class);
        when(ex.getPermission()).thenReturn("paper:review");

        Result<Void> result = handler.handleNotPermission(ex);

        assertThat(result.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("TC-HANDLER-006: 默认 code=400 BusinessException 无需自定义枚举")
    void handleBusiness_defaultCode400_forGenericBusinessError() {
        BusinessException ex = new BusinessException("用户输入参数不合法");

        Result<Void> result = handler.handleBusiness(ex);

        assertThat(result.getCode()).isEqualTo(400);
    }
}
