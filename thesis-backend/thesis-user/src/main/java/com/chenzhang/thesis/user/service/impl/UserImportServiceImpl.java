package com.chenzhang.thesis.user.service.impl;

import com.alibaba.excel.EasyExcel;
import com.chenzhang.thesis.common.constant.CacheKeyConstant;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.user.domain.entity.User;
import com.chenzhang.thesis.user.domain.entity.UserImportTask;
import com.chenzhang.thesis.user.mapper.UserImportTaskMapper;
import com.chenzhang.thesis.user.mapper.UserMapper;
import com.chenzhang.thesis.user.service.UserImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户批量导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportServiceImpl implements UserImportService {

    private final UserImportTaskMapper importTaskMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public Map<String, Object> submitImportTask(String importType, MultipartFile file, Long operatorId, Long schoolId, Long batchId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 解析总行数（简易实现：读取 Excel 获取行数）
        int totalCount = 0;
        try {
            List<Map<Integer, String>> list = EasyExcel.read(file.getInputStream()).sheet().doReadSync();
            totalCount = list.size() - 1; // 减去表头
        } catch (IOException e) {
            throw new BusinessException("文件解析失败");
        }

        // 保存任务记录
        UserImportTask task = new UserImportTask();
        task.setTaskId(taskId);
        task.setSchoolId(schoolId);
        task.setOperatorId(operatorId);
        task.setImportType(importType);
        task.setFileUrl("pending");
        task.setTotalCount(totalCount);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setIsDeleted(0);
        importTaskMapper.insert(task);

        if (totalCount <= 50) {
            // 同步处理
            task.setStatus("PROCESSING");
            task.setStartedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);

            int success = processImport(file, importType, schoolId, batchId);
            task.setSuccessCount(success);
            task.setFailCount(totalCount - success);
            task.setStatus(success == totalCount ? "SUCCESS" : "PARTIAL");
            task.setFinishedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);
        } else {
            // 异步处理：发送 MQ
            task.setStatus("PENDING");
            importTaskMapper.updateById(task);
            rocketMQTemplate.convertAndSend("thesis-user-import", Map.of(
                    "taskId", taskId,
                    "importType", importType,
                    "schoolId", schoolId,
                    "batchId", batchId
            ));
        }

        // 写入 Redis 进度缓存
        updateRedisProgress(taskId, task);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("totalCount", totalCount);
        result.put("description", totalCount <= 50 ? "同步处理中..." : "已提交异步任务");
        return result;
    }

    @Override
    public Map<String, Object> getProgress(String taskId) {
        String key = String.format(CacheKeyConstant.IMPORT_TASK, taskId);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return parseProgress(cached);
        }

        UserImportTask task = importTaskMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserImportTask>()
                        .eq(UserImportTask::getTaskId, taskId)
        );
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        Map<String, Object> progress = new HashMap<>();
        progress.put("taskId", task.getTaskId());
        progress.put("status", task.getStatus());
        progress.put("totalCount", task.getTotalCount());
        progress.put("successCount", task.getSuccessCount());
        progress.put("failCount", task.getFailCount());
        progress.put("progress", task.getTotalCount() != null && task.getTotalCount() > 0
                ? (int) ((task.getSuccessCount() + task.getFailCount()) * 100.0 / task.getTotalCount())
                : 0);
        progress.put("errorFileUrl", task.getErrorFileUrl());
        progress.put("startedAt", task.getStartedAt() != null ? task.getStartedAt().toString() : null);
        progress.put("finishedAt", task.getFinishedAt() != null ? task.getFinishedAt().toString() : null);
        return progress;
    }

    private int processImport(MultipartFile file, String importType, Long schoolId, Long batchId) {
        // Sprint 1 简化实现：逐行解析 Excel 并写入数据库
        int success = 0;
        try {
            List<Map<Integer, String>> rows = EasyExcel.read(file.getInputStream()).sheet().doReadSync();
            if (rows.size() <= 1) return 0;

            List<User> users = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                Map<Integer, String> row = rows.get(i);
                User user = new User();
                user.setSchoolId(schoolId);
                user.setUsername(row.getOrDefault(0, ""));
                user.setRealName(row.getOrDefault(1, ""));
                user.setPhone(row.getOrDefault(2, ""));
                user.setUserType(importType.equals("STUDENT") ? "STUDENT" : importType.equals("TEACHER") ? "TEACHER" : "ASSISTANT");
                user.setStudentNo(row.getOrDefault(3, ""));
                user.setTeacherNo(row.getOrDefault(3, ""));
                user.setMajor(row.getOrDefault(4, ""));
                user.setStatus("ACTIVE");
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setIsDeleted(0);
                users.add(user);

                if (users.size() >= 500) {
                    userMapper.insert(users);
                    success += users.size();
                    users.clear();
                }
            }
            if (!users.isEmpty()) {
                userMapper.insert(users);
                success += users.size();
            }
        } catch (IOException e) {
            log.error("导入处理失败", e);
        }
        return success;
    }

    private void updateRedisProgress(String taskId, UserImportTask task) {
        String key = String.format(CacheKeyConstant.IMPORT_TASK, taskId);
        Map<String, Object> progress = new HashMap<>();
        progress.put("taskId", task.getTaskId());
        progress.put("status", task.getStatus());
        progress.put("totalCount", task.getTotalCount());
        progress.put("successCount", task.getSuccessCount());
        progress.put("failCount", task.getFailCount());
        progress.put("progress", task.getTotalCount() != null && task.getTotalCount() > 0
                ? (int) ((task.getSuccessCount() + task.getFailCount()) * 100.0 / task.getTotalCount())
                : 0);
        progress.put("errorFileUrl", task.getErrorFileUrl());
        progress.put("startedAt", task.getStartedAt() != null ? task.getStartedAt().toString() : null);
        progress.put("finishedAt", task.getFinishedAt() != null ? task.getFinishedAt().toString() : null);
        redisTemplate.opsForValue().set(key, progress.toString(), 24, TimeUnit.HOURS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseProgress(String cached) {
        Map<String, Object> map = new HashMap<>();
        try {
            cached = cached.replace("{", "").replace("}", "");
            String[] pairs = cached.split(", ");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
        } catch (Exception e) {
            log.warn("解析进度缓存失败: {}", cached);
        }
        return map;
    }
}
