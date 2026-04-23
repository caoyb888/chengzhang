-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.2.0
-- Database   : thesis_im
-- Description: im_message 按月 RANGE COLUMNS 分区，支持消息保留≥1年及归档
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

USE thesis_im;

-- ---------------------------------------------------------------------------
-- Step 1: 调整主键，使分区列 created_at 包含在主键中（MySQL 8.0 分区表硬性要求）
-- ---------------------------------------------------------------------------
ALTER TABLE im_message
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`, `created_at`);

-- ---------------------------------------------------------------------------
-- Step 2: 按月 RANGE COLUMNS 分区（保留≥1年，超过1年分区可交换至归档表后 DROP）
-- 覆盖 2026-01 至 2027-12，后续由运维脚本每月自动添加新分区
-- ---------------------------------------------------------------------------
ALTER TABLE im_message
  PARTITION BY RANGE COLUMNS (`created_at`) (
    PARTITION p2026_01 VALUES LESS THAN ('2026-02-01 00:00:00'),
    PARTITION p2026_02 VALUES LESS THAN ('2026-03-01 00:00:00'),
    PARTITION p2026_03 VALUES LESS THAN ('2026-04-01 00:00:00'),
    PARTITION p2026_04 VALUES LESS THAN ('2026-05-01 00:00:00'),
    PARTITION p2026_05 VALUES LESS THAN ('2026-06-01 00:00:00'),
    PARTITION p2026_06 VALUES LESS THAN ('2026-07-01 00:00:00'),
    PARTITION p2026_07 VALUES LESS THAN ('2026-08-01 00:00:00'),
    PARTITION p2026_08 VALUES LESS THAN ('2026-09-01 00:00:00'),
    PARTITION p2026_09 VALUES LESS THAN ('2026-10-01 00:00:00'),
    PARTITION p2026_10 VALUES LESS THAN ('2026-11-01 00:00:00'),
    PARTITION p2026_11 VALUES LESS THAN ('2026-12-01 00:00:00'),
    PARTITION p2026_12 VALUES LESS THAN ('2027-01-01 00:00:00'),
    PARTITION p2027_01 VALUES LESS THAN ('2027-02-01 00:00:00'),
    PARTITION p2027_02 VALUES LESS THAN ('2027-03-01 00:00:00'),
    PARTITION p2027_03 VALUES LESS THAN ('2027-04-01 00:00:00'),
    PARTITION p2027_04 VALUES LESS THAN ('2027-05-01 00:00:00'),
    PARTITION p2027_05 VALUES LESS THAN ('2027-06-01 00:00:00'),
    PARTITION p2027_06 VALUES LESS THAN ('2027-07-01 00:00:00'),
    PARTITION p2027_07 VALUES LESS THAN ('2027-08-01 00:00:00'),
    PARTITION p2027_08 VALUES LESS THAN ('2027-09-01 00:00:00'),
    PARTITION p2027_09 VALUES LESS THAN ('2027-10-01 00:00:00'),
    PARTITION p2027_10 VALUES LESS THAN ('2027-11-01 00:00:00'),
    PARTITION p2027_11 VALUES LESS THAN ('2027-12-01 00:00:00'),
    PARTITION p2027_12 VALUES LESS THAN ('2028-01-01 00:00:00'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
  );

-- ---------------------------------------------------------------------------
-- Step 3: 重建原索引确认（分区操作保留原有二级索引）
-- idx_group_time、idx_sender_time、idx_school_filtered 已由上一步保留
-- ---------------------------------------------------------------------------

-- End of V1.2.0 migration for thesis_im
