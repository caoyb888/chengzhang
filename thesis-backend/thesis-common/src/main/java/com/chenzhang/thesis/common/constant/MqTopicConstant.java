package com.chenzhang.thesis.common.constant;

/**
 * RocketMQ Topic 命名规范（对应 CLAUDE.md §5.7）
 */
public final class MqTopicConstant {

    private MqTopicConstant() {}

    public static final String PAPER_SUBMIT        = "thesis-paper-submit";
    public static final String PAPER_REVIEW        = "thesis-paper-review";
    public static final String USER_IMPORT         = "thesis-user-import";
    public static final String NOTIFY_SEND         = "thesis-notify-send";
    public static final String AI_EVALUATE         = "thesis-ai-evaluate";
    public static final String AI_IDEOLOGY_SCAN    = "thesis-ai-ideology-scan";
    public static final String STAT_REFRESH        = "thesis-stat-refresh";

    // 消费者组命名规范：<服务名>-<topic简称>-group
    public static final String NOTIFY_SEND_GROUP        = "thesis-notify-send-group";
    public static final String AI_EVALUATE_GROUP        = "thesis-ai-evaluate-group";
    public static final String AI_IDEOLOGY_SCAN_GROUP   = "thesis-ai-ideology-scan-group";
    public static final String STAT_REFRESH_GROUP       = "thesis-stat-refresh-group";
    public static final String USER_IMPORT_GROUP        = "thesis-user-import-group";
}
