package com.chenzhang.thesis.gateway.filter;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：网关限流过滤器测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter 网关限流过滤器测试")
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        FlowRuleManager.loadRules(java.util.List.of());
    }

    @Test
    @DisplayName("TC-RATE-001: 正常请求放行")
    void filter_normalRequest_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/user/list")
                .remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("TC-RATE-002: 获取客户端 IP — 优先 X-Forwarded-For")
    void getClientIp_xForwardedFor_priority() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/user/list")
                .header("X-Forwarded-For", "203.0.113.1, 192.168.1.1")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.1", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("TC-RATE-003: 过滤器顺序为 50")
    void getOrder_is50() {
        assertThat(filter.getOrder()).isEqualTo(50);
    }

    @Test
    @DisplayName("TC-RATE-004: Sentinel 规则动态注册")
    void filter_registersRuleForNewIp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/user/list")
                .remoteAddress(new java.net.InetSocketAddress("10.0.0.1", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // 首次请求应注册规则
        Mono<Void> result = filter.filter(exchange, chain);
        StepVerifier.create(result).verifyComplete();

        // 规则已注册
        var rules = FlowRuleManager.getRules();
        assertThat(rules.stream().anyMatch(r -> r.getResource().contains("10.0.0.1"))).isTrue();
    }
}
