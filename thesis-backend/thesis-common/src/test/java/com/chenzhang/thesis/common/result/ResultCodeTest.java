package com.chenzhang.thesis.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：ResultCode 枚举完整性验证
 */
@DisplayName("ResultCode 错误码枚举测试")
class ResultCodeTest {

    @ParameterizedTest
    @EnumSource(ResultCode.class)
    @DisplayName("TC-CODE-001: 所有枚举项 code 和 message 均不为空")
    void allEnumValues_haveNonNullCodeAndMessage(ResultCode code) {
        assertThat(code.getCode()).isNotNull();
        assertThat(code.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("TC-CODE-002: 所有错误码全局唯一，无重复")
    void allCodes_areUnique() {
        List<Integer> codes = Arrays.stream(ResultCode.values())
                .map(ResultCode::getCode)
                .toList();
        Set<Integer> uniqueCodes = Set.copyOf(codes);
        assertThat(uniqueCodes).hasSameSizeAs(codes);
    }

    @Test
    @DisplayName("TC-CODE-003: SUCCESS code=200")
    void successCode_is200() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
        assertThat(ResultCode.SUCCESS.getMessage()).isEqualTo("success");
    }

    @Test
    @DisplayName("TC-CODE-004: HTTP 标准码映射正确（401/403/500）")
    void standardHttpCodes_correctlyMapped() {
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ResultCode.INTERNAL_ERROR.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("TC-CODE-005: 业务错误码分段符合规范（1xxx用户/2xxx论文/3xxx权限）")
    void businessCodes_followNamingConvention() {
        Set<Integer> allCodes = Arrays.stream(ResultCode.values())
                .map(ResultCode::getCode)
                .collect(Collectors.toSet());

        // 验证分段规范：业务码在 1000-3999 范围内
        allCodes.stream()
                .filter(c -> c >= 1000)
                .forEach(c -> assertThat(c)
                        .as("业务码 %d 应在 1000-3999 范围内", c)
                        .isBetween(1000, 3999));
    }
}
