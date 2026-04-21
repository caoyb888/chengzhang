package com.chenzhang.thesis.common.config;

import cn.dev33.satoken.stp.StpUtil;
import com.chenzhang.thesis.common.constant.UserTypeConstant;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

/**
 * 数据权限处理器
 * 自动注入 school_id / teaching_point_id 过滤条件（对应 CLAUDE.md §5.4）
 * 业务代码禁止手写 school_id 过滤，统一由此处理器注入
 *
 * 使用：在 Mapper 查询方法上加 @DataPermission 注解
 */
@Component
public class DataPermissionHandler {

    /**
     * 根据当前登录用户角色，生成数据权限 WHERE 条件
     *
     * @param where     原始 WHERE 条件
     * @param mappedStatementId 当前执行的 MapperId
     * @return 追加了数据权限条件的 Expression
     */
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        if (!StpUtil.isLogin()) {
            return where;
        }

        Object loginId = StpUtil.getLoginId();
        // 从 Session 中获取用户信息（登录时注入）
        Long schoolId   = (Long) StpUtil.getSession().get("schoolId");
        String userType = (String) StpUtil.getSession().get("userType");

        if (schoolId == null) {
            return where;
        }

        // 所有角色都追加 school_id 过滤
        Expression schoolFilter = new EqualsTo(new Column("school_id"), new LongValue(schoolId));

        Expression result = (where == null) ? schoolFilter : new AndExpression(where, schoolFilter);

        // 教学点管理员额外追加 teaching_point_id 过滤
        if (UserTypeConstant.POINT_ADMIN.equals(userType)) {
            Long teachingPointId = (Long) StpUtil.getSession().get("teachingPointId");
            if (teachingPointId != null) {
                Expression pointFilter = new EqualsTo(new Column("teaching_point_id"), new LongValue(teachingPointId));
                result = new AndExpression(result, pointFilter);
            }
        }

        return result;
    }
}
