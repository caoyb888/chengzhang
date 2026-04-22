package com.chenzhang.thesis.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * 链路追踪全局过滤器（次高优先级）
 * <p>
 * 1. 生成或透传 X-Trace-Id<br>
 * 2. 将 traceId / userId / schoolId 写入 MDC 与 Reactor Context
 */
@Slf4j
@Component
public class TraceFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = generateTraceId();
        }

        final String finalTraceId = traceId;
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String schoolId = exchange.getRequest().getHeaders().getFirst("X-School-Id");

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header(TRACE_ID_HEADER, finalTraceId)
            .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange)
            .doOnSubscribe(subscription -> {
                MDC.put("traceId", finalTraceId);
                if (StringUtils.hasText(userId)) {
                    MDC.put("userId", userId);
                }
                if (StringUtils.hasText(schoolId)) {
                    MDC.put("schoolId", schoolId);
                }
            })
            .contextWrite(Context.of(TRACE_ID_HEADER, finalTraceId, "userId", userId, "schoolId", schoolId))
            .doFinally(signalType -> {
                MDC.remove("traceId");
                MDC.remove("userId");
                MDC.remove("schoolId");
            });
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
