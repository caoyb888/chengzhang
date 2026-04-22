package com.chenzhang.thesis.common.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试计划 §4.4：MQ Topic 命名规范验证（对应 CLAUDE.md §5.7）
 */
@DisplayName("MqTopicConstant 消息队列 Topic 命名测试")
class MqTopicConstantTest {

    @Test
    @DisplayName("TC-MQ-001: 核心 Topic 常量存在且非空")
    void coreTopics_existAndNotBlank() {
        assertThat(MqTopicConstant.PAPER_SUBMIT).isNotBlank();
        assertThat(MqTopicConstant.PAPER_REVIEW).isNotBlank();
        assertThat(MqTopicConstant.USER_IMPORT).isNotBlank();
        assertThat(MqTopicConstant.NOTIFY_SEND).isNotBlank();
        assertThat(MqTopicConstant.AI_EVALUATE).isNotBlank();
        assertThat(MqTopicConstant.AI_IDEOLOGY_SCAN).isNotBlank();
        assertThat(MqTopicConstant.STAT_REFRESH).isNotBlank();
    }

    @Test
    @DisplayName("TC-MQ-002: 所有 Topic 命名以 thesis- 开头（规范前缀）")
    void allTopics_startWithThesisPrefix() throws Exception {
        for (String topic : getAllTopicValues()) {
            if (!topic.endsWith("_GROUP")) { // 排除消费者组常量
                assertThat(topic)
                        .as("Topic '%s' 应以 'thesis-' 开头", topic)
                        .startsWith("thesis-");
            }
        }
    }

    @Test
    @DisplayName("TC-MQ-003: 消费者组命名以 -group 结尾")
    void consumerGroups_endWithGroupSuffix() {
        assertThat(MqTopicConstant.NOTIFY_SEND_GROUP).endsWith("-group");
        assertThat(MqTopicConstant.AI_EVALUATE_GROUP).endsWith("-group");
    }

    @Test
    @DisplayName("TC-MQ-004: 所有 Topic 值全局唯一（无重复）")
    void allTopicValues_areUnique() throws Exception {
        List<String> topics = getAllTopicValues();
        Set<String> unique = new HashSet<>(topics);
        assertThat(unique).hasSameSizeAs(topics);
    }

    @Test
    @DisplayName("TC-MQ-005: 论文提交 Topic 符合规范 thesis-paper-submit")
    void paperSubmitTopic_matchesSpec() {
        assertThat(MqTopicConstant.PAPER_SUBMIT).isEqualTo("thesis-paper-submit");
    }

    @Test
    @DisplayName("TC-MQ-006: AI 评议 Topic 符合规范 thesis-ai-evaluate")
    void aiEvaluateTopic_matchesSpec() {
        assertThat(MqTopicConstant.AI_EVALUATE).isEqualTo("thesis-ai-evaluate");
    }

    /** 通过反射收集所有 static final String 常量值 */
    private List<String> getAllTopicValues() throws Exception {
        List<String> values = new ArrayList<>();
        for (Field field : MqTopicConstant.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())
                    && field.getType() == String.class) {
                field.setAccessible(true);
                values.add((String) field.get(null));
            }
        }
        return values;
    }
}
