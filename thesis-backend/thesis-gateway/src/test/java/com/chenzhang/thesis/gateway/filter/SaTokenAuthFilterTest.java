package com.chenzhang.thesis.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：网关认证过滤器测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaTokenAuthFilter 网关认证过滤器测试")
class SaTokenAuthFilterTest {

    private SaTokenAuthFilter filter;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SaTokenAuthFilter();
    }

    @Test
    @DisplayName("TC-GATEWAY-001: 白名单路径直接放行")
    void filter_whiteList_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("TC-GATEWAY-002: 无 Token → 返回 401")
    void filter_noToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/user/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("TC-GATEWAY-004: 登录接口在白名单内直接放行")
    void filter_loginPath_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login/password").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("TC-GATEWAY-005: Token 刷新接口在白名单内直接放行")
    void filter_refreshPath_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/token/refresh").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("TC-GATEWAY-006: 过滤器顺序为最高优先级")
    void getOrder_isHighestPriority() {
        assertThat(filter.getOrder()).isEqualTo(-100);
    }

    @Test
    @DisplayName("TC-GATEWAY-007: API docs 路径在白名单内直接放行")
    void filter_apiDocs_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/v3/api-docs/auth").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("TC-GATEWAY-008: webjars 路径在白名单内直接放行")
    void filter_webjars_passThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/webjars/jquery/jquery.min.js").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }
}
