package com.chenzhang.thesis.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 请求日志全局过滤器
 * <p>
 * 记录所有请求的 Method、Path、耗时、状态码、客户端 IP、TraceId。<br>
 * 耗时超过 500ms 的接口打印 WARN 慢接口日志。
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final long SLOW_THRESHOLD_MS = 500;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String traceId = request.getHeaders().getFirst(TraceFilter.TRACE_ID_HEADER);
        String clientIp = getClientIp(request);

        return chain.filter(exchange)
            .doFinally(signalType -> {
                long duration = System.currentTimeMillis() - startTime;
                Integer statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : null;

                if (duration > SLOW_THRESHOLD_MS) {
                    log.warn("[SLOW-API] {} {} {}ms status={} clientIp={} traceId={}",
                        method, path, duration, statusCode, clientIp, traceId);
                } else {
                    log.info("[REQUEST] {} {} {}ms status={} clientIp={} traceId={}",
                        method, path, duration, statusCode, clientIp, traceId);
                }
            });
    }

    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeaders().getFirst("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
