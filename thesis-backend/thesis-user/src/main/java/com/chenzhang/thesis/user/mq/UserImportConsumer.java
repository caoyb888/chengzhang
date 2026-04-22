package com.chenzhang.thesis.user.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户批量导入 MQ 消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "thesis-user-import",
        consumerGroup = "thesis-user-import-group"
)
public class UserImportConsumer implements RocketMQListener<Map<String, Object>> {

    @Override
    public void onMessage(Map<String, Object> message) {
        String taskId = (String) message.get("taskId");
        log.info("[MQ] 收到用户导入任务: taskId={}", taskId);
        // Sprint 1 简化：异步导入逻辑由前端轮询进度触发，Consumer 仅做日志记录
        // 实际生产环境可在此触发 Excel 解析和批量写入
    }
}
