package com.chenzhang.thesis.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.user.domain.vo.PermissionVO;
import com.chenzhang.thesis.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/role/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @SaCheckPermission("permission:read")
    public Result<List<PermissionVO>> getPermissionTree() {
        List<PermissionVO> tree = permissionService.getPermissionTree();
        return Result.ok(tree);
    }
}
