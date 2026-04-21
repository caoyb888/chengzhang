-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_statistics
-- Description: 初始化 thesis_statistics 统计库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_statistics
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_statistics;

-- ---------------------------------------------------------------------------
-- 表 stat_paper_summary — 论文统计宽表（预计算）
-- 通过定时任务（每5分钟）预计算各维度统计，禁止前端直接聚合大表
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stat_paper_summary (
  `id`                    BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`             BIGINT UNSIGNED  NOT NULL COMMENT '学校ID',
  `batch_id`              BIGINT UNSIGNED  NOT NULL COMMENT '批次ID',
  `teaching_point_id`     BIGINT UNSIGNED  NULL     COMMENT '教学点ID（NULL=汇总全校数据）',
  `teacher_id`            BIGINT UNSIGNED  NULL     COMMENT '教师ID（NULL=汇总所有教师数据）',
  `major`                 VARCHAR(128)     NULL     COMMENT '专业名称（NULL=汇总所有专业数据）',
  `dim_type`              VARCHAR(32)      NOT NULL COMMENT '统计维度类型（SCHOOL/POINT/TEACHER/MAJOR/POINT_TEACHER/POINT_MAJOR）',
  `total_students`        INT              NOT NULL DEFAULT 0 COMMENT '该维度下学生总数',
  -- 电子签名统计
  `sign_pending`          INT              NOT NULL DEFAULT 0 COMMENT '签名-待提交人数',
  `sign_submitted`        INT              NOT NULL DEFAULT 0 COMMENT '签名-审核中人数',
  `sign_approved`         INT              NOT NULL DEFAULT 0 COMMENT '签名-已通过人数',
  `sign_rejected`         INT              NOT NULL DEFAULT 0 COMMENT '签名-已驳回人数',
  -- 选题统计
  `topic_pending`         INT              NOT NULL DEFAULT 0 COMMENT '选题-待提交人数',
  `topic_submitted`       INT              NOT NULL DEFAULT 0 COMMENT '选题-审核中人数',
  `topic_approved`        INT              NOT NULL DEFAULT 0 COMMENT '选题-已通过人数',
  `topic_rejected`        INT              NOT NULL DEFAULT 0 COMMENT '选题-已驳回人数',
  -- 大纲统计
  `outline_pending`       INT              NOT NULL DEFAULT 0 COMMENT '大纲-待提交人数',
  `outline_submitted`     INT              NOT NULL DEFAULT 0 COMMENT '大纲-审核中人数',
  `outline_approved`      INT              NOT NULL DEFAULT 0 COMMENT '大纲-已通过人数',
  `outline_rejected`      INT              NOT NULL DEFAULT 0 COMMENT '大纲-已驳回人数',
  -- 初稿统计
  `draft_pending`         INT              NOT NULL DEFAULT 0 COMMENT '初稿-待提交人数',
  `draft_submitted`       INT              NOT NULL DEFAULT 0 COMMENT '初稿-审核中人数',
  `draft_approved`        INT              NOT NULL DEFAULT 0 COMMENT '初稿-已通过人数',
  `draft_rejected`        INT              NOT NULL DEFAULT 0 COMMENT '初稿-已驳回人数',
  -- 定稿统计
  `final_draft_pending`   INT              NOT NULL DEFAULT 0 COMMENT '定稿-待提交人数',
  `final_draft_submitted` INT              NOT NULL DEFAULT 0 COMMENT '定稿-审核中人数',
  `final_draft_approved`  INT              NOT NULL DEFAULT 0 COMMENT '定稿-已通过人数',
  `final_draft_rejected`  INT              NOT NULL DEFAULT 0 COMMENT '定稿-已驳回人数',
  -- 终稿统计
  `final_pending`         INT              NOT NULL DEFAULT 0 COMMENT '终稿-待提交人数',
  `final_submitted`       INT              NOT NULL DEFAULT 0 COMMENT '终稿-审核中人数',
  `final_approved`        INT              NOT NULL DEFAULT 0 COMMENT '终稿-已通过人数',
  `final_rejected`        INT              NOT NULL DEFAULT 0 COMMENT '终稿-已驳回人数',
  -- 教师指导统计
  `guide_total`           INT              NOT NULL DEFAULT 0 COMMENT '教师-总指导学生数',
  `guide_completed`       INT              NOT NULL DEFAULT 0 COMMENT '教师-已指导完成数',
  `guide_in_progress`     INT              NOT NULL DEFAULT 0 COMMENT '教师-指导中数',
  `guide_pending`         INT              NOT NULL DEFAULT 0 COMMENT '教师-待指导数',
  -- 查重统计
  `check_total`           INT              NOT NULL DEFAULT 0 COMMENT '查重-已查重总数',
  `check_pass_rate`       DECIMAL(5,2)     NOT NULL DEFAULT 0 COMMENT '查重-平均相似率（%）',
  `stat_time`             DATETIME         NOT NULL COMMENT '统计时间（最后一次计算时间）',
  `created_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`            TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_dim` (`batch_id`, `dim_type`, `teaching_point_id`, `teacher_id`, `major`) COMMENT 'UPSERT唯一键（更新时按此键匹配）',
  KEY `idx_school_batch_dim` (`school_id`, `batch_id`, `dim_type`) COMMENT '统计报表查询核心索引，高频'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文统计宽表：通过定时任务（每5分钟）预计算各维度统计，禁止前端直接聚合paper/paper_node_status大表。dim_type区分统计维度，UPSERT更新';

-- End of V1.0.0 migration for thesis_statistics
