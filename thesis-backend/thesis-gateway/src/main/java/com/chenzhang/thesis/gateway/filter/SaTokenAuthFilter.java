package com.chenzhang.thesis.gateway.filter;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.common.result.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 认证全局过滤器（最高优先级）
 * <p>
 * 1. 白名单路径直接放行<br>
 * 2. 其他路径校验 Authorization Header 中的 Token<br>
 * 3. 校验通过后，将 userId / schoolId / teachingPointId 注入下游请求头
 */
@Slf4j
@Component
public class SaTokenAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = Arrays.asList(
        "/api/v1/auth/login/",
        "/api/v1/auth/sms/send",
        "/api/v1/auth/token/refresh",
        "/doc.html",
        "/webjars/",
        "/v3/api-docs/"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!SaFoxUtil.isNotEmpty(token)) {
            return unauthorized(exchange, ResultCode.UNAUTHORIZED.getMessage());
        }

        return Mono.fromCallable(() -> doAuth(token))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(authInfo -> {
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", authInfo.userId())
                    .header("X-School-Id", authInfo.schoolId() != null ? authInfo.schoolId() : "")
                    .header("X-Teaching-Point-Id", authInfo.teachingPointId() != null ? authInfo.teachingPointId() : "")
                    .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            })
            .onErrorResume(e -> {
                log.warn("认证失败 [{}]: {}", path, e.getMessage());
                String msg = e instanceof SaTokenException ? e.getMessage() : ResultCode.UNAUTHORIZED.getMessage();
                return unauthorized(exchange, msg);
            });
    }

    /**
     * 使用 StpLogic 直接根据 Token 获取登录信息（不依赖 ThreadLocal 上下文，兼容 WebFlux）
     */
    private AuthInfo doAuth(String token) {
        StpLogic stpLogic = StpUtil.stpLogic;

        // 根据 Token 值获取 loginId
        Object loginId = stpLogic.getLoginIdByToken(token);
        if (loginId == null) {
            throw new SaTokenException("登录已过期或Token无效");
        }

        // 根据 loginId 获取 Session，提取扩展字段
        SaSession session = stpLogic.getSessionByLoginId(loginId, false);
        String schoolId = session != null ? session.getString("schoolId") : null;
        String teachingPointId = session != null ? session.getString("teachingPointId") : null;

        return new AuthInfo(loginId.toString(), schoolId, teachingPointId);
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED.getCode(), message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":401,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private record AuthInfo(String userId, String schoolId, String teachingPointId) {
    }
}
