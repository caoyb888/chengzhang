-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.1.0
-- Database   : thesis_im
-- Description: 补充答辩录像记录表和答辩评分表（Sprint 4 P0 在线答辩功能）
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

USE thesis_im;

-- ---------------------------------------------------------------------------
-- 表 defense_recording — 答辩录像记录表
-- 记录在线答辩的云端录制信息，支持多组并行答辩的录像管理
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS defense_recording (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `defense_group_id` BIGINT UNSIGNED  NOT NULL COMMENT '所属答辩组ID，关联 defense_group.id',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `recording_type`  VARCHAR(32)      NOT NULL DEFAULT 'CLOUD' COMMENT '录制类型（CLOUD-云端录制 LOCAL-本地录制）',
  `recording_url`   VARCHAR(512)     NULL     COMMENT '录像文件URL（MinIO存储）',
  `start_time`      DATETIME         NOT NULL COMMENT '录制开始时间',
  `end_time`        DATETIME         NULL     COMMENT '录制结束时间',
  `duration_seconds` INT             NULL     COMMENT '录制时长（秒）',
  `file_size`       BIGINT           NULL     COMMENT '文件大小（字节）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'RECORDING' COMMENT '状态（RECORDING-录制中 COMPLETED-已完成 FAILED-失败）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_group_status` (`defense_group_id`, `status`, `created_at`) COMMENT '按答辩组查询录像列表',
  KEY `idx_batch_status` (`batch_id`, `status`) COMMENT '按批次查询录像进度'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='答辩录像记录表：记录在线答辩的云端录制信息，支持答辩结束后回放（招标要求▲在线答辩录像）';

-- ---------------------------------------------------------------------------
-- 表 defense_score — 答辩评分表
-- 记录答辩评委对每位学生的逐项评分和综合结论
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS defense_score (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `defense_group_id` BIGINT UNSIGNED  NOT NULL COMMENT '所属答辩组ID，关联 defense_group.id',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID（冗余）',
  `student_id`      BIGINT UNSIGNED  NOT NULL COMMENT '答辩学生用户ID',
  `judge_id`        BIGINT UNSIGNED  NOT NULL COMMENT '评委教师用户ID',
  `score_items_json` JSON            NOT NULL COMMENT '各维度得分详情（[{"dimension":"内容质量","score":85,"max_score":100}]）',
  `total_score`     DECIMAL(6,2)     NOT NULL COMMENT '汇总总分',
  `comment`         TEXT             NULL     COMMENT '评委评语',
  `conclusion`      VARCHAR(32)      NULL     COMMENT '答辩结论（PASS-通过 FAIL-未通过）',
  `scored_at`       DATETIME         NOT NULL COMMENT '评分提交时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_student_judge` (`defense_group_id`, `student_id`, `judge_id`) COMMENT '同一评委对同一学生只评一次分',
  KEY `idx_batch_score` (`batch_id`, `total_score`, `conclusion`) COMMENT '按批次统计评分分布',
  KEY `idx_student_group` (`student_id`, `defense_group_id`) COMMENT '查询学生的答辩评分'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='答辩评分表：记录答辩评委对每位学生的逐项评分和综合结论，score_items_json存储各维度明细，系统自动汇总总分（Sprint 4 S4-BE-02）';

-- End of V1.1.0 migration for thesis_im
