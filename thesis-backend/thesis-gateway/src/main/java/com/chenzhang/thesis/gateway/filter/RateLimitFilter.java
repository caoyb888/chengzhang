package com.chenzhang.thesis.gateway.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
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

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流全局过滤器
 * <p>
 * 基于 Sentinel 核心库实现按客户端 IP 的 QPS 限流（默认 500 QPS/IP）。<br>
 * 首次请求时动态注册该 IP 对应的 FlowRule，后续请求复用已有规则。
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_QPS_PER_IP = 500;
    private static final String RESOURCE_PREFIX = "gateway_ip_limit:";
    private static final Object RULE_LOCK = new Object();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = getClientIp(exchange.getRequest());
        String resource = RESOURCE_PREFIX + ip;

        registerRuleIfAbsent(resource);

        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            final Entry finalEntry = entry;
            return chain.filter(exchange)
                .doFinally(signalType -> {
                    if (finalEntry != null) {
                        finalEntry.exit();
                    }
                });
        } catch (BlockException e) {
            log.warn("IP {} 触发 Sentinel 限流规则", ip);
            return writeBlockedResponse(exchange);
        }
    }

    /**
     * 若该 IP 对应的 Sentinel 规则尚未注册，则动态新增（线程安全，双检锁）。
     */
    private void registerRuleIfAbsent(String resource) {
        List<FlowRule> rules = FlowRuleManager.getRules();
        boolean exists = rules.stream().anyMatch(r -> resource.equals(r.getResource()));
        if (exists) {
            return;
        }
        synchronized (RULE_LOCK) {
            rules = FlowRuleManager.getRules();
            exists = rules.stream().anyMatch(r -> resource.equals(r.getResource()));
            if (exists) {
                return;
            }
            List<FlowRule> newRules = new ArrayList<>(rules);
            FlowRule rule = new FlowRule();
            rule.setResource(resource);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setCount(MAX_QPS_PER_IP);
            rule.setLimitApp("default");
            newRules.add(rule);
            FlowRuleManager.loadRules(newRules);
            log.info("注册 Sentinel IP 限流规则: resource={}, qps={}", resource, MAX_QPS_PER_IP);
        }
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

    private Mono<Void> writeBlockedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.fail(ResultCode.TOO_MANY_REQUESTS);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\"}".getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
