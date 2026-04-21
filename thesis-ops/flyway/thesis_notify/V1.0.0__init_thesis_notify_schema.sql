-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_notify
-- Description: 初始化 thesis_notify 通知库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_notify
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_notify;

-- ---------------------------------------------------------------------------
-- 表 notify_record — 通知记录表
-- 记录所有推送的通知，支持短信/站内信/微信三种渠道
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notify_record (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `user_id`       BIGINT UNSIGNED  NOT NULL COMMENT '接收通知的用户ID',
  `notify_type`   VARCHAR(32)      NOT NULL COMMENT '通知类型（PAPER_SUBMITTED/PAPER_APPROVED/PAPER_REJECTED/DEFENSE_REMIND/SYSTEM）',
  `title`         VARCHAR(128)     NOT NULL COMMENT '通知标题',
  `content`       TEXT             NOT NULL COMMENT '通知内容',
  `link_url`      VARCHAR(512)     NULL     COMMENT '通知关联跳转链接（前端路由路径）',
  `send_channel`  VARCHAR(32)      NOT NULL COMMENT '发送渠道（SMS-短信 INSITE-站内信 WECHAT-微信服务号）',
  `send_status`   VARCHAR(32)      NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（PENDING-待发 SUCCESS-成功 FAILED-失败）',
  `send_time`     DATETIME         NULL     COMMENT '实际发送时间',
  `read_status`   VARCHAR(32)      NOT NULL DEFAULT 'UNREAD' COMMENT '阅读状态（UNREAD-未读 READ-已读，站内信使用）',
  `read_at`       DATETIME         NULL     COMMENT '阅读时间',
  `retry_count`   TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '重试次数（失败后重试，最多3次）',
  `error_message` VARCHAR(256)     NULL     COMMENT '发送失败原因',
  `ref_id`        BIGINT UNSIGNED  NULL     COMMENT '关联业务ID（如paper_id/defense_group_id）',
  `ref_type`      VARCHAR(32)      NULL     COMMENT '关联业务类型（PAPER/DEFENSE/BATCH）',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（通知触发时间）',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `read_status`, `created_at` DESC) COMMENT '用户查询未读通知列表，高频',
  KEY `idx_user_school_time` (`school_id`, `user_id`, `notify_type`, `created_at`) COMMENT '按类型查通知历史',
  KEY `idx_send_pending` (`send_status`, `send_channel`, `retry_count`) COMMENT '失败重试扫描索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='通知记录表：记录所有推送的通知，支持短信/站内信/微信三种渠道，read_status仅站内信使用，失败自动重试最多3次';

-- End of V1.0.0 migration for thesis_notify
