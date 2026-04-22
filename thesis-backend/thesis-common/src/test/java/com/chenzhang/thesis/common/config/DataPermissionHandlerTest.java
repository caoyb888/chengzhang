package com.chenzhang.thesis.common.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.chenzhang.thesis.common.constant.UserTypeConstant;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.4：DataPermissionHandler 各角色注入逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataPermissionHandler 数据权限处理器测试")
class DataPermissionHandlerTest {

    private DataPermissionHandler handler;

    @Mock
    private SaSession session;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandler();
    }

    @Test
    @DisplayName("TC-DP-001: 未登录状态 — 直接返回原始 WHERE")
    void notLogin_returnsOriginalWhere() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);

            Expression original = new EqualsTo(new net.sf.jsqlparser.schema.Column("id"), new LongValue(1));
            Expression result = handler.getSqlSegment(original, "com.mapper.UserMapper.selectList");

            assertThat(result).isSameAs(original);
        }
    }

    @Test
    @DisplayName("TC-DP-002: SCHOOL_ADMIN — 仅注入 school_id 过滤")
    void schoolAdmin_injectsOnlySchoolId() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(100L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(1L);
            when(session.get("userType")).thenReturn(UserTypeConstant.SCHOOL_ADMIN);

            Expression original = null;
            Expression result = handler.getSqlSegment(original, "com.mapper.UserMapper.selectList");

            assertThat(result).isInstanceOf(EqualsTo.class);
            assertThat(result.toString()).contains("school_id = 1");
        }
    }

    @Test
    @DisplayName("TC-DP-003: POINT_ADMIN — 注入 school_id + teaching_point_id")
    void pointAdmin_injectsSchoolIdAndTeachingPointId() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(200L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(1L);
            when(session.get("userType")).thenReturn(UserTypeConstant.POINT_ADMIN);
            when(session.get("teachingPointId")).thenReturn(101L);

            Expression original = null;
            Expression result = handler.getSqlSegment(original, "com.mapper.UserMapper.selectList");

            assertThat(result).isInstanceOf(AndExpression.class);
            assertThat(result.toString()).contains("school_id = 1");
            assertThat(result.toString()).contains("teaching_point_id = 101");
        }
    }

    @Test
    @DisplayName("TC-DP-004: TEACHER — 仅注入 school_id")
    void teacher_injectsOnlySchoolId() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(300L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(1L);
            when(session.get("userType")).thenReturn(UserTypeConstant.TEACHER);

            Expression result = handler.getSqlSegment(null, "com.mapper.UserMapper.selectList");

            assertThat(result.toString()).contains("school_id = 1");
            assertThat(result.toString()).doesNotContain("teaching_point_id");
        }
    }

    @Test
    @DisplayName("TC-DP-005: STUDENT — 仅注入 school_id（业务层再做学生ID校验）")
    void student_injectsOnlySchoolId() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(400L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(1L);
            when(session.get("userType")).thenReturn(UserTypeConstant.STUDENT);

            Expression result = handler.getSqlSegment(null, "com.mapper.UserMapper.selectList");

            assertThat(result.toString()).contains("school_id = 1");
            assertThat(result.toString()).doesNotContain("teaching_point_id");
        }
    }

    @Test
    @DisplayName("TC-DP-006: 已有 WHERE 条件 — 追加 AND school_id")
    void existingWhere_appendsAndCondition() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(100L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(1L);
            when(session.get("userType")).thenReturn(UserTypeConstant.SCHOOL_ADMIN);

            Expression original = new EqualsTo(new net.sf.jsqlparser.schema.Column("status"), new net.sf.jsqlparser.expression.StringValue("ACTIVE"));
            Expression result = handler.getSqlSegment(original, "com.mapper.UserMapper.selectList");

            assertThat(result).isInstanceOf(AndExpression.class);
            assertThat(result.toString()).contains("status = 'ACTIVE'");
            assertThat(result.toString()).contains("school_id = 1");
        }
    }

    @Test
    @DisplayName("TC-DP-007: schoolId 为空 — 不注入过滤")
    void nullSchoolId_returnsOriginal() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginId).thenReturn(100L);
            stp.when(StpUtil::getSession).thenReturn(session);

            when(session.get("schoolId")).thenReturn(null);

            Expression original = new EqualsTo(new net.sf.jsqlparser.schema.Column("id"), new LongValue(1));
            Expression result = handler.getSqlSegment(original, "com.mapper.UserMapper.selectList");

            assertThat(result).isSameAs(original);
        }
    }
}
