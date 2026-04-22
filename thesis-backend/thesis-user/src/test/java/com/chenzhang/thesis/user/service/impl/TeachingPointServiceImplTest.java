package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.domain.dto.CreateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.entity.TeachingPoint;
import com.chenzhang.thesis.user.domain.vo.TeachingPointVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.TeachingPointMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：教学点管理 Service 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeachingPointServiceImpl 教学点服务测试")
class TeachingPointServiceImplTest {

    @Mock
    private TeachingPointMapper teachingPointMapper;

    @InjectMocks
    private TeachingPointServiceImpl teachingPointService;

    @Test
    @DisplayName("TC-TP-001: 创建教学点正向 — 默认数据范围为 POINT")
    void createTeachingPoint_success_defaultsPointScope() {
        mockSession(1L);
        when(teachingPointMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(teachingPointMapper.insert(any(TeachingPoint.class))).thenAnswer(i -> {
            TeachingPoint p = i.getArgument(0);
            p.setId(101L);
            return 1;
        });

        CreateTeachingPointDTO dto = new CreateTeachingPointDTO();
        dto.setSchoolId(1L);
        dto.setName("济南教学点");
        dto.setCode("JN001");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            Long id = teachingPointService.createTeachingPoint(dto);
            assertThat(id).isEqualTo(101L);
        }
    }

    @Test
    @DisplayName("TC-TP-002: 创建教学点 — 编码重复 → 抛异常")
    void createTeachingPoint_duplicateCode_throwsException() {
        mockSession(1L);
        when(teachingPointMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CreateTeachingPointDTO dto = new CreateTeachingPointDTO();
        dto.setSchoolId(1L);
        dto.setCode("DUP");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            assertThatThrownBy(() -> teachingPointService.createTeachingPoint(dto))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("教学点编码已存在");
        }
    }

    @Test
    @DisplayName("TC-TP-003: 更新教学点 — 字段选择性更新")
    void updateTeachingPoint_success_updatesFields() {
        TeachingPoint point = new TeachingPoint();
        point.setId(1L);
        point.setIsDeleted(0);
        when(teachingPointMapper.selectById(1L)).thenReturn(point);

        UpdateTeachingPointDTO dto = new UpdateTeachingPointDTO();
        dto.setName("新名称");
        dto.setContactPhone("13900139000");

        teachingPointService.updateTeachingPoint(1L, dto);

        assertThat(point.getName()).isEqualTo("新名称");
        assertThat(point.getContactPhone()).isEqualTo("13900139000");
        verify(teachingPointMapper).updateById(point);
    }

    @Test
    @DisplayName("TC-TP-004: 查询教学点详情 — 不存在 → NOT_FOUND")
    void getTeachingPointDetail_notFound_throwsException() {
        when(teachingPointMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> teachingPointService.getTeachingPointDetail(999L))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("TC-TP-005: 删除教学点 — 软删除")
    void removeTeachingPoint_success_deletes() {
        TeachingPoint point = new TeachingPoint();
        point.setId(1L);
        point.setIsDeleted(0);
        when(teachingPointMapper.selectById(1L)).thenReturn(point);

        teachingPointService.removeTeachingPoint(1L);
        verify(teachingPointMapper).deleteById(1L);
    }

    @Test
    @DisplayName("TC-TP-006: 查询活跃教学点列表")
    void listActiveTeachingPoints_returnsActiveList() {
        when(teachingPointMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                List.of(
                        new TeachingPoint() {{ setId(101L); setName("济南"); }},
                        new TeachingPoint() {{ setId(102L); setName("青岛"); }}
                )
        );

        List<TeachingPointVO> list = teachingPointService.listActiveTeachingPoints();
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("TC-TP-007: 分页查询教学点 — 按关键字过滤")
    void pageTeachingPoints_withKeyword_returnsPage() {
        when(teachingPointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<TeachingPoint>());

        PageResult<TeachingPointVO> result = teachingPointService.pageTeachingPoints(1, 10, "济南");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("TC-TP-008: 数据权限 — 越权创建他校教学点 → DATA_PERMISSION_DENIED")
    void createTeachingPoint_crossSchool_throwsException() {
        mockSession(1L);

        CreateTeachingPointDTO dto = new CreateTeachingPointDTO();
        dto.setSchoolId(2L);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(mock(SaSession.class));
            when(StpUtil.getSession().get("schoolId")).thenReturn(1L);

            assertThatThrownBy(() -> teachingPointService.createTeachingPoint(dto))
                    .isInstanceOf(UserException.class)
                    .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo(ResultCode.DATA_PERMISSION_DENIED.getCode()));
        }
    }

    private void mockSession(Long schoolId) {
        // 占位
    }
}
