package com.chenzhang.thesis.user.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.chenzhang.thesis.common.config.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Set;

/**
 * MyBatis-Plus 配置
 * - 分页插件
 * - 防全表更新/删除插件
 * - 数据权限拦截器（自动注入 school_id / teaching_point_id）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisPlusConfig {

    private final DataPermissionHandler dataPermissionHandler;

    private static final Set<String> IGNORE_TABLES = Set.of("permission");

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件：MySQL 方言，单页最大 100 条
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(100L);
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);

        // 防止全表 UPDATE/DELETE
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 数据权限拦截器
        interceptor.addInnerInterceptor(new DataPermissionInnerInterceptor());

        return interceptor;
    }

    /**
     * 数据权限内部拦截器
     */
    public class DataPermissionInnerInterceptor implements InnerInterceptor {

        @Override
        public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                                RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
            if (!StpUtil.isLogin()) {
                return;
            }
            PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
            String originalSql = mpBs.sql();
            try {
                Select select = (Select) CCJSqlParserUtil.parse(originalSql);
                PlainSelect plainSelect = select.getPlainSelect();
                if (plainSelect != null) {
                    if (shouldIgnore(plainSelect)) {
                        return;
                    }
                    Expression where = plainSelect.getWhere();
                    Expression newWhere = dataPermissionHandler.getSqlSegment(where, ms.getId());
                    plainSelect.setWhere(newWhere);
                    mpBs.sql(select.toString());
                }
            } catch (Exception e) {
                log.warn("数据权限 SQL 解析失败，跳过拦截: {}", originalSql, e);
            }
        }

        private boolean shouldIgnore(PlainSelect plainSelect) {
            if (plainSelect.getFromItem() instanceof net.sf.jsqlparser.schema.Table table) {
                return IGNORE_TABLES.contains(table.getName());
            }
            return false;
        }
    }
}
