package com.chenzhang.thesis.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.user.service.UserImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户批量导入控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserImportController {

    private final UserImportService userImportService;

    @PostMapping("/import")
    @SaCheckPermission("perm:user:import")
    public Result<Map<String, Object>> importUsers(
            @RequestParam("importType") String importType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "batchId", required = false) Long batchId) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        Long schoolId = StpUtil.getSession().getModel("schoolId", Long.class, 0L);
        Map<String, Object> result = userImportService.submitImportTask(importType, file, operatorId, schoolId, batchId);
        return Result.ok(result);
    }

    @GetMapping("/import/{taskId}/progress")
    @SaCheckPermission("perm:user:import")
    public Result<Map<String, Object>> getImportProgress(@PathVariable String taskId) {
        Map<String, Object> progress = userImportService.getProgress(taskId);
        return Result.ok(progress);
    }
}
