package com.chenzhang.thesis.paper.feign;

import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.paper.domain.vo.FeignUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "thesis-user", fallbackFactory = UserServiceFallbackFactory.class)
public interface UserServiceClient {

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/api/v1/user/{userId}")
    Result<FeignUserVO> getUserById(@PathVariable Long userId);
}
