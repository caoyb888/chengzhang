package com.chenzhang.thesis.common.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：CacheKey 命名规范验证（对应 CLAUDE.md §5.6）
 */
@DisplayName("CacheKeyConstant 缓存键命名规范测试")
class CacheKeyConstantTest {

    @Test
    @DisplayName("TC-CACHE-001: auth:token:{userId} 格式正确")
    void authToken_matchesPattern() {
        String key = String.format(CacheKeyConstant.AUTH_TOKEN, 123456L);
        assertThat(key).isEqualTo("auth:token:123456");
        assertThat(key).startsWith("auth:token:");
    }

    @Test
    @DisplayName("TC-CACHE-002: auth:perm:{userId} 格式正确")
    void authPerm_matchesPattern() {
        String key = String.format(CacheKeyConstant.AUTH_PERM, 789L);
        assertThat(key).isEqualTo("auth:perm:789");
        assertThat(key).startsWith("auth:perm:");
    }

    @Test
    @DisplayName("TC-CACHE-003: paper:status:{paperId} 格式正确")
    void paperStatus_matchesPattern() {
        String key = String.format(CacheKeyConstant.PAPER_STATUS, 100001L);
        assertThat(key).isEqualTo("paper:status:100001");
        assertThat(key).startsWith("paper:status:");
    }

    @Test
    @DisplayName("TC-CACHE-004: stat:batch:{batchId} 格式正确")
    void statBatch_matchesPattern() {
        String key = String.format(CacheKeyConstant.STAT_BATCH, 2001L);
        assertThat(key).isEqualTo("stat:batch:2001");
        assertThat(key).startsWith("stat:batch:");
    }

    @Test
    @DisplayName("TC-CACHE-005: sms:{phone} 格式正确")
    void smsCode_matchesPattern() {
        String key = String.format(CacheKeyConstant.SMS_CODE, "13800138000");
        assertThat(key).isEqualTo("sms:13800138000");
        assertThat(key).startsWith("sms:");
    }

    @Test
    @DisplayName("TC-CACHE-006: import:task:{taskId} 格式正确")
    void importTask_matchesPattern() {
        String key = String.format(CacheKeyConstant.IMPORT_TASK, "uuid-abc-123");
        assertThat(key).isEqualTo("import:task:uuid-abc-123");
        assertThat(key).startsWith("import:task:");
    }

    @Test
    @DisplayName("TC-CACHE-007: topic:lock:{batchId}:{hash} 双参数格式正确（分布式锁关键）")
    void topicLock_twoParams_matchesPattern() {
        String key = String.format(CacheKeyConstant.TOPIC_LOCK, 1001L, "abc123hash");
        assertThat(key).isEqualTo("topic:lock:1001:abc123hash");
        assertThat(key).startsWith("topic:lock:");
    }

    @Test
    @DisplayName("TC-CACHE-008: 不同 batchId 相同 hash — 选题锁 Key 不同（隔离验证）")
    void topicLock_differentBatchId_differentKey() {
        String key1 = String.format(CacheKeyConstant.TOPIC_LOCK, 1001L, "samehash");
        String key2 = String.format(CacheKeyConstant.TOPIC_LOCK, 1002L, "samehash");
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("TC-CACHE-009: 相同 batchId 不同 hash — 选题锁 Key 不同")
    void topicLock_differentHash_differentKey() {
        String key1 = String.format(CacheKeyConstant.TOPIC_LOCK, 1001L, "hash_aaa");
        String key2 = String.format(CacheKeyConstant.TOPIC_LOCK, 1001L, "hash_bbb");
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("TC-CACHE-010: 所有 Key 模板均含冒号命名空间分隔符")
    void allTemplates_containNamespaceSeparator() {
        String[] templates = {
            CacheKeyConstant.AUTH_TOKEN, CacheKeyConstant.AUTH_PERM,
            CacheKeyConstant.PAPER_STATUS, CacheKeyConstant.STAT_BATCH,
            CacheKeyConstant.SMS_CODE, CacheKeyConstant.IMPORT_TASK,
            CacheKeyConstant.TOPIC_LOCK
        };
        for (String t : templates) {
            assertThat(t).as("模板 '%s' 应包含冒号分隔符", t).contains(":");
        }
    }
}
