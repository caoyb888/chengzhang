package com.chenzhang.thesis.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：PageResult<T> 分页响应体测试
 */
@DisplayName("PageResult 分页响应体测试")
class PageResultTest {

    @Test
    @DisplayName("TC-PAGE-001: 空构造器 — 默认字段可写入")
    void defaultConstructor_fieldsCanBeSet() {
        PageResult<String> page = new PageResult<>();
        page.setTotal(100L);
        page.setPages(5);
        page.setCurrent(1);
        page.setSize(20);
        page.setRecords(List.of("a", "b", "c"));

        assertThat(page.getTotal()).isEqualTo(100L);
        assertThat(page.getPages()).isEqualTo(5);
        assertThat(page.getCurrent()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getRecords()).hasSize(3);
    }

    @Test
    @DisplayName("TC-PAGE-002: records 为空列表时不为 null")
    void emptyRecords_notNull() {
        PageResult<String> page = new PageResult<>();
        page.setRecords(List.of());
        assertThat(page.getRecords()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("TC-PAGE-003: total=0 时 pages 应为 0（逻辑合理性）")
    void zeroTotal_pagesIsZero() {
        PageResult<Object> page = new PageResult<>();
        page.setTotal(0L);
        page.setPages(0);
        page.setRecords(List.of());

        assertThat(page.getTotal()).isZero();
        assertThat(page.getPages()).isZero();
        assertThat(page.getRecords()).isEmpty();
    }
}
