package com.chenzhang.thesis.auth.config;

import cn.dev33.satoken.jwt.StpLogicJwtForMixin;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置：启用 JWT 模式（mix 风格，支持登录/踢人/注销）
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForMixin();
    }
}
