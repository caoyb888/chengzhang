-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.1.0
-- Database   : thesis_paper
-- Description: paper_operation_log 按年 RANGE COLUMNS 分区，满足3年审计保留要求
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

USE thesis_paper;

-- ---------------------------------------------------------------------------
-- Step 1: 调整主键，使分区列 created_at 包含在主键中（MySQL 8.0 分区表硬性要求）
-- ---------------------------------------------------------------------------
ALTER TABLE paper_operation_log
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`, `created_at`);

-- ---------------------------------------------------------------------------
-- Step 2: 按年 RANGE COLUMNS 分区（审计要求保留3年，年分区粒度足够）
-- ---------------------------------------------------------------------------
ALTER TABLE paper_operation_log
  PARTITION BY RANGE COLUMNS (`created_at`) (
    PARTITION p2024 VALUES LESS THAN ('2025-01-01 00:00:00'),
    PARTITION p2025 VALUES LESS THAN ('2026-01-01 00:00:00'),
    PARTITION p2026 VALUES LESS THAN ('2027-01-01 00:00:00'),
    PARTITION p2027 VALUES LESS THAN ('2028-01-01 00:00:00'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
  );

-- ---------------------------------------------------------------------------
-- Step 3: 重建原索引确认（分区操作保留原有二级索引）
-- idx_paper_time、idx_operator_batch、idx_batch_time 已由上一步保留
-- ---------------------------------------------------------------------------

-- End of V1.1.0 migration for thesis_paper
