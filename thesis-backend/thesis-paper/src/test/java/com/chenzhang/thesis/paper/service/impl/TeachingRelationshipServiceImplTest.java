package com.chenzhang.thesis.paper.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipDTO;
import com.chenzhang.thesis.paper.domain.entity.TeachingRelationship;
import com.chenzhang.thesis.paper.exception.PaperException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2 / Sprint 1 S1-BE-07：师生指导关系 Service 测试
 * 覆盖：重复检测、正向创建、批量创建、状态更新、逻辑删除
 *
 * 实现说明：TeachingRelationshipServiceImpl 继承 MyBatis-Plus ServiceImpl，
 * 使用 Mockito spy + doReturn 拦截继承的 DB 操作方法，无需启动 Spring 上下文。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeachingRelationshipServiceImpl 指导关系服务测试")
class TeachingRelationshipServiceImplTest {

    private TeachingRelationshipServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new TeachingRelationshipServiceImpl());
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-TR-001: 创建指导关系 — 已存在相同关系 → 抛 PaperException")
    void create_duplicate_throwsException() {
        doReturn(new TeachingRelationship()).when(service).getOne(any());

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            assertThatThrownBy(() -> service.create(buildDto()))
                    .isInstanceOf(PaperException.class)
                    .hasMessageContaining("已存在");
        }
    }

    @Test
    @DisplayName("TC-TR-002: 创建指导关系 — 正向成功返回生成的 ID")
    void create_success_returnsId() {
        doReturn(null).when(service).getOne(any());
        doAnswer(inv -> {
            TeachingRelationship entity = inv.getArgument(0);
            entity.setId(100L);
            return true;
        }).when(service).save(any(TeachingRelationship.class));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            Long id = service.create(buildDto());
            assertThat(id).isEqualTo(100L);
        }
    }

    @Test
    @DisplayName("TC-TR-003: 创建指导关系 — save 失败 → 抛 PaperException")
    void create_saveFails_throwsException() {
        doReturn(null).when(service).getOne(any());
        doReturn(false).when(service).save(any(TeachingRelationship.class));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            assertThatThrownBy(() -> service.create(buildDto()))
                    .isInstanceOf(PaperException.class)
                    .hasMessageContaining("创建指导关系失败");
        }
    }

    @Test
    @DisplayName("TC-TR-004: 创建指导关系 — assignedBy 设置为当前登录用户 ID")
    void create_setsAssignedBy() {
        doReturn(null).when(service).getOne(any());

        List<TeachingRelationship> captured = new java.util.ArrayList<>();
        doAnswer(inv -> {
            TeachingRelationship e = inv.getArgument(0);
            e.setId(200L);
            captured.add(e);
            return true;
        }).when(service).save(any(TeachingRelationship.class));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(55L);

            service.create(buildDto());
        }

        assertThat(captured.get(0).getAssignedBy()).isEqualTo(55L);
        assertThat(captured.get(0).getIsActive()).isEqualTo(1);
    }

    // ─── batchCreate ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-TR-005: 批量创建 — 空列表 → 返回 0，不执行任何 DB 操作")
    void batchCreate_emptyList_returnsZero() {
        int result = service.batchCreate(Collections.emptyList());

        assertThat(result).isEqualTo(0);
        verify(service, never()).saveBatch(anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-TR-006: 批量创建 — null 列表 → 返回 0")
    void batchCreate_nullList_returnsZero() {
        int result = service.batchCreate(null);

        assertThat(result).isEqualTo(0);
        verify(service, never()).saveBatch(anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-TR-007: 批量创建 — 正向成功，返回实际创建数量")
    void batchCreate_success_returnsCount() {
        doReturn(true).when(service).saveBatch(anyList(), anyInt());

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            List<TeachingRelationshipDTO> dtos = Arrays.asList(buildDto(), buildDto(), buildDto());
            int count = service.batchCreate(dtos);

            assertThat(count).isEqualTo(3);
            verify(service).saveBatch(argThat(list -> list.size() == 3), eq(500));
        }
    }

    @Test
    @DisplayName("TC-TR-008: 批量创建 — saveBatch 失败 → 抛 PaperException")
    void batchCreate_saveFails_throwsException() {
        doReturn(false).when(service).saveBatch(anyList(), anyInt());

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            assertThatThrownBy(() -> service.batchCreate(Arrays.asList(buildDto())))
                    .isInstanceOf(PaperException.class)
                    .hasMessageContaining("批量创建指导关系失败");
        }
    }

    // ─── updateStatus ────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-TR-009: 更新状态 — null 状态值 → 抛 PaperException（状态值非法）")
    void updateStatus_nullStatus_throwsException() {
        assertThatThrownBy(() -> service.updateStatus(1L, null))
                .isInstanceOf(PaperException.class)
                .hasMessageContaining("状态值非法");
    }

    @Test
    @DisplayName("TC-TR-010: 更新状态 — 状态值为 2（非 0/1）→ 抛 PaperException")
    void updateStatus_invalidStatus2_throwsException() {
        assertThatThrownBy(() -> service.updateStatus(1L, 2))
                .isInstanceOf(PaperException.class)
                .hasMessageContaining("状态值非法");
    }

    @Test
    @DisplayName("TC-TR-011: 更新状态 — 关系不存在或已删除 → 抛 NOT_FOUND PaperException")
    void updateStatus_notFound_throwsNotFound() {
        doReturn(false).when(service).update(any(LambdaUpdateWrapper.class));

        assertThatThrownBy(() -> service.updateStatus(99L, 0))
                .isInstanceOf(PaperException.class)
                .satisfies(ex -> assertThat(((PaperException) ex).getCode())
                        .isEqualTo(ResultCode.NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("TC-TR-012: 更新状态 — 启用（isActive=1）正向成功，无异常")
    void updateStatus_enable_success() {
        doReturn(true).when(service).update(any(LambdaUpdateWrapper.class));

        assertThatCode(() -> service.updateStatus(1L, 1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TC-TR-013: 更新状态 — 禁用（isActive=0）正向成功，无异常")
    void updateStatus_disable_success() {
        doReturn(true).when(service).update(any(LambdaUpdateWrapper.class));

        assertThatCode(() -> service.updateStatus(1L, 0)).doesNotThrowAnyException();
    }

    // ─── remove ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-TR-014: 删除指导关系 — 不存在或已删除 → 抛 NOT_FOUND PaperException")
    void remove_notFound_throwsNotFound() {
        doReturn(false).when(service).removeById(anyLong());

        assertThatThrownBy(() -> service.remove(99L))
                .isInstanceOf(PaperException.class)
                .satisfies(ex -> assertThat(((PaperException) ex).getCode())
                        .isEqualTo(ResultCode.NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("TC-TR-015: 删除指导关系 — 正向成功，无异常")
    void remove_success() {
        doReturn(true).when(service).removeById(anyLong());

        assertThatCode(() -> service.remove(1L)).doesNotThrowAnyException();
        verify(service).removeById(1L);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private TeachingRelationshipDTO buildDto() {
        TeachingRelationshipDTO dto = new TeachingRelationshipDTO();
        dto.setSchoolId(1L);
        dto.setBatchId(1001L);
        dto.setStudentId(101L);
        dto.setTeacherId(201L);
        dto.setTeacherType("MAIN");
        dto.setLevel(1);
        dto.setAssignType("MANUAL");
        return dto;
    }
}
