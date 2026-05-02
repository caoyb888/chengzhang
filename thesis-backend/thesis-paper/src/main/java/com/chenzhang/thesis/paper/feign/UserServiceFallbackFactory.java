package com.chenzhang.thesis.paper.feign;

import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.paper.domain.vo.FeignUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 降级工厂
 */
@Slf4j
@Component
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return userId -> {
            log.warn("[Feign 降级] UserService.getUserById, userId={}, cause={}", userId, cause.getMessage());
            return Result.fail(503, "用户服务暂时不可用");
        };
    }
}
