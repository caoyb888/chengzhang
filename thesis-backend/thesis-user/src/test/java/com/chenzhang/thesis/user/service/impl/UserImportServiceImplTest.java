package com.chenzhang.thesis.user.service.impl;

import com.chenzhang.thesis.common.constant.CacheKeyConstant;
import com.chenzhang.thesis.common.exception.BusinessException;
import com.chenzhang.thesis.user.domain.entity.UserImportTask;
import com.chenzhang.thesis.user.mapper.UserImportTaskMapper;
import com.chenzhang.thesis.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §2.2.8 / §4.2：用户批量导入 Service 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserImportServiceImpl 用户批量导入服务测试")
class UserImportServiceImplTest {

    @Mock
    private UserImportTaskMapper importTaskMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private org.apache.rocketmq.spring.core.RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private UserImportServiceImpl userImportService;

    @Test
    @DisplayName("TC-IMPORT-001: 空文件上传 → 抛异常")
    void submitImportTask_emptyFile_throwsException() {
        MultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> userImportService.submitImportTask("STUDENT", file, 1L, 1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("上传文件不能为空");
    }

    @Test
    @DisplayName("TC-IMPORT-002: 查询进度 — Redis 缓存命中")
    void getProgress_cacheHit_returnsProgress() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String cached = "{taskId=abc123, status=SUCCESS, totalCount=100, successCount=100, failCount=0, progress=100, errorFileUrl=null, startedAt=null, finishedAt=null}";
        when(valueOperations.get(String.format(CacheKeyConstant.IMPORT_TASK, "abc123"))).thenReturn(cached);

        Map<String, Object> progress = userImportService.getProgress("abc123");

        assertThat(progress.get("taskId")).isEqualTo("abc123");
        assertThat(progress.get("status")).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("TC-IMPORT-004: 查询进度 — Redis 未命中，查 DB")
    void getProgress_cacheMiss_readsDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        UserImportTask task = new UserImportTask();
        task.setTaskId("def456");
        task.setStatus("PROCESSING");
        task.setTotalCount(200);
        task.setSuccessCount(50);
        task.setFailCount(0);
        when(importTaskMapper.selectOne(any())).thenReturn(task);

        Map<String, Object> progress = userImportService.getProgress("def456");

        assertThat(progress.get("taskId")).isEqualTo("def456");
        assertThat(progress.get("status")).isEqualTo("PROCESSING");
        assertThat(progress.get("progress")).isEqualTo(25);
    }

    @Test
    @DisplayName("TC-IMPORT-005: 查询进度 — 任务不存在 → 抛异常")
    void getProgress_notFound_throwsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(importTaskMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> userImportService.getProgress("notexist"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("TC-IMPORT-006: 进度计算 — 总数为 0 时不除零")
    void getProgress_zeroTotal_returnsZeroPercent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        UserImportTask task = new UserImportTask();
        task.setTaskId("zero");
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        when(importTaskMapper.selectOne(any())).thenReturn(task);

        Map<String, Object> progress = userImportService.getProgress("zero");

        assertThat(progress.get("progress")).isEqualTo(0);
    }
}
