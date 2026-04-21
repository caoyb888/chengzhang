-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_im
-- Description: 初始化 thesis_im 即时通讯库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_im
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_im;

-- ---------------------------------------------------------------------------
-- 表 im_group — 消息群组表
-- 支持三种群组类型：批次大群、师生指导群、自定义群
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS im_group (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（多租户隔离）',
  `batch_id`        BIGINT UNSIGNED  NULL     COMMENT '关联批次ID（BATCH/GUIDE类型必填，CUSTOM为NULL）',
  `group_type`      VARCHAR(32)      NOT NULL COMMENT '群组类型（BATCH-批次大群 GUIDE-师生指导群 CUSTOM-自定义群）',
  `name`            VARCHAR(128)     NOT NULL COMMENT '群组名称（自动生成或管理员自定义）',
  `avatar_url`      VARCHAR(512)     NULL     COMMENT '群头像URL',
  `owner_id`        BIGINT UNSIGNED  NOT NULL COMMENT '群主用户ID（管理员或教师）',
  `member_count`    INT              NOT NULL DEFAULT 0 COMMENT '群成员数量（冗余计数，实时维护）',
  `last_message_id` BIGINT UNSIGNED  NULL     COMMENT '最后一条消息ID（用于群列表预览）',
  `last_message_at` DATETIME         NULL     COMMENT '最后消息时间（用于群列表排序）',
  `is_muted`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否全群禁言（主持人/管理员操作）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'ACTIVE' COMMENT '群组状态（ACTIVE-正常 DISSOLVED-已解散）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_batch_type` (`school_id`, `batch_id`, `group_type`, `status`) COMMENT '按学校批次查询群组',
  KEY `idx_school_last_msg` (`school_id`, `last_message_at` DESC) COMMENT '群组列表按最新消息排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='消息群组表：支持三种群组类型，批次大群和师生指导群自动创建，last_message_*字段用于群列表实时预览';

-- ---------------------------------------------------------------------------
-- 表 im_group_member — 群组成员表
-- 记录群组成员列表和权限
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS im_group_member (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id`        BIGINT UNSIGNED  NOT NULL COMMENT '群组ID，关联 im_group.id',
  `user_id`         BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `member_role`     VARCHAR(32)      NOT NULL DEFAULT 'MEMBER' COMMENT '成员角色（OWNER-群主 ADMIN-管理员 MEMBER-普通成员）',
  `is_muted`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否被单独禁言（答辩中主持人对特定成员操作）',
  `unread_count`    INT              NOT NULL DEFAULT 0 COMMENT '未读消息数（Redis中维护，此处定期同步）',
  `last_read_msg_id` BIGINT UNSIGNED NULL     COMMENT '最后已读消息ID（用于计算未读数）',
  `join_type`       VARCHAR(32)      NOT NULL DEFAULT 'AUTO' COMMENT '加入方式（AUTO-自动加入 INVITE-邀请加入）',
  `joined_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入群组时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-在群 1-已退群）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`) COMMENT '同一用户在同一群组只有一条记录',
  KEY `idx_user_school` (`user_id`, `school_id`, `is_deleted`) COMMENT '用户查看自己的所有群组列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='群组成员表：记录群组成员列表和权限，is_muted支持答辩场景的单独禁言功能';

-- ---------------------------------------------------------------------------
-- 表 im_message — 即时消息表
-- 所有消息保留≥1年（招标要求），按月分区管理
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS im_message (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID（消息全局唯一ID）',
  `group_id`        BIGINT UNSIGNED  NOT NULL COMMENT '所属群组ID，关联 im_group.id',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `sender_id`       BIGINT UNSIGNED  NOT NULL COMMENT '发送人用户ID',
  `msg_type`        VARCHAR(32)      NOT NULL COMMENT '消息类型（TEXT-文字 IMAGE-图片 FILE-文件 SYSTEM-系统通知）',
  `content`         TEXT             NOT NULL COMMENT '消息内容（TEXT类型为文本；IMAGE/FILE类型为JSON含URL/文件名/大小）',
  `original_content` TEXT            NULL     COMMENT '过滤前的原始内容（含敏感词时记录，用于审计）',
  `is_filtered`     TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否经过敏感词过滤（1-已过滤替换 0-正常）',
  `is_blocked`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否被拦截（1-未发出 0-正常发出）',
  `file_url`        VARCHAR(512)     NULL     COMMENT '文件URL（IMAGE/FILE类型使用，MinIO存储）',
  `file_name`       VARCHAR(256)     NULL     COMMENT '文件原始名称（FILE类型）',
  `file_size`       BIGINT           NULL     COMMENT '文件大小（字节，FILE/IMAGE类型）',
  `reply_msg_id`    BIGINT UNSIGNED  NULL     COMMENT '回复的消息ID（引用回复功能）',
  `is_recalled`     TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否已撤回（发送后2分钟内可撤回）',
  `recalled_at`     DATETIME         NULL     COMMENT '撤回时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（管理员删违规消息使用）',
  PRIMARY KEY (`id`),
  KEY `idx_group_time` (`group_id`, `created_at`, `is_deleted`) COMMENT '按群组加载历史消息（时序分页），高频',
  KEY `idx_sender_time` (`sender_id`, `group_id`, `created_at`) COMMENT '查询某用户在群组的发言记录',
  KEY `idx_school_filtered` (`school_id`, `is_filtered`, `created_at`) COMMENT '管理端审查过滤消息记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='即时消息表：所有消息保留≥1年（招标要求），is_filtered记录敏感词过滤情况，管理员可查看任意群组消息。按月分区见§5章';

-- ---------------------------------------------------------------------------
-- 表 sensitive_word — 敏感词库表
-- 内置词库(school_id=0)和学校自定义词库
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sensitive_word (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '所属学校ID（0=系统内置词库，>0=学校自定义词库）',
  `word`        VARCHAR(128)     NOT NULL COMMENT '敏感词内容',
  `category`    VARCHAR(32)      NOT NULL DEFAULT 'GENERAL' COMMENT '词库分类（GENERAL-通用 POLITICAL-政治 RELIGION-宗教 CUSTOM-自定义）',
  `level`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '敏感等级（1-替换星号 2-拦截不发送 3-记录告警）',
  `is_active`   TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否启用（0-停用 1-启用）',
  `created_by`  BIGINT UNSIGNED  NULL     COMMENT '添加人用户ID（NULL=系统预置）',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`  TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_word` (`school_id`, `word`) COMMENT '同一学校内敏感词不重复',
  KEY `idx_school_active` (`school_id`, `is_active`, `category`) COMMENT '加载学校有效敏感词库（AC自动机初始化时）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='敏感词库表：内置词库(school_id=0)和学校自定义词库，level决定处理方式，每次变更触发AC自动机热更新';

-- ---------------------------------------------------------------------------
-- 表 defense_group — 答辩组表
-- 支持同步在线答辩（SYNC）和异步答辩（ASYNC）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS defense_group (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `name`            VARCHAR(128)     NOT NULL COMMENT '答辩组名称（如：计算机专业第一答辩组）',
  `defense_type`    VARCHAR(32)      NOT NULL COMMENT '答辩形式（SYNC-在线同步答辩 ASYNC-异步答辩）',
  `defense_time`    DATETIME         NULL     COMMENT '答辩时间（SYNC类型必填）',
  `duration_minutes` INT             NULL     COMMENT '预计答辩时长（分钟）',
  `host_user_id`    BIGINT UNSIGNED  NULL     COMMENT '主持人用户ID（拥有禁言/踢出/邀请上台权限）',
  `room_id`         VARCHAR(128)     NULL     COMMENT 'RTC房间ID（云服务商提供，SYNC类型使用）',
  `room_token`      VARCHAR(512)     NULL     COMMENT 'RTC房间Token（加密存储）',
  `view_mode`       VARCHAR(32)      NOT NULL DEFAULT 'GRID' COMMENT '视频布局模式（GRID-一屏多等分 RIGHT_LIST-右侧成员列表 TOP_LIST-顶部成员列表）',
  `is_recording`    TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否自动录像（1-全程录像 0-不录像）',
  `recording_url`   VARCHAR(512)     NULL     COMMENT '录像文件URL（答辩结束后由云端回调填充）',
  `async_deadline`  DATETIME         NULL     COMMENT '异步答辩截止时间（ASYNC类型：学生上传视频的截止时间）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'PENDING' COMMENT '答辩状态（PENDING-待开始 IN_PROGRESS-进行中 PAUSED-暂停 FINISHED-已结束）',
  `started_at`      DATETIME         NULL     COMMENT '实际开始时间',
  `finished_at`     DATETIME         NULL     COMMENT '实际结束时间',
  `created_by`      BIGINT UNSIGNED  NOT NULL COMMENT '创建管理员ID',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_batch_status` (`batch_id`, `status`, `defense_time`) COMMENT '按批次查询答辩组列表和进行中的答辩',
  KEY `idx_school_batch` (`school_id`, `batch_id`, `defense_type`) COMMENT '管理端查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='答辩组表：支持同步在线答辩（SYNC）和异步答辩（ASYNC），SYNC类型关联云端RTC房间，支持多组并行（每组独立room_id）。招标要求▲';

-- ---------------------------------------------------------------------------
-- 表 defense_group_member — 答辩组成员表
-- 记录评委和学生的参与关系
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS defense_group_member (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `defense_group_id` BIGINT UNSIGNED NOT NULL COMMENT '答辩组ID，关联 defense_group.id',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `user_id`         BIGINT UNSIGNED  NOT NULL COMMENT '用户ID（评委教师或答辩学生）',
  `member_role`     VARCHAR(32)      NOT NULL COMMENT '成员角色（HOST-主持人 JUDGE-评委教师 STUDENT-答辩学生）',
  `paper_id`        BIGINT UNSIGNED  NULL     COMMENT '学生的论文ID（STUDENT角色填写，用于导航至论文）',
  `defense_order`   TINYINT UNSIGNED NULL     COMMENT '答辩顺序（学生答辩时的出场顺序，NULL=不限顺序）',
  `async_video_url` VARCHAR(512)     NULL     COMMENT '异步答辩视频URL（ASYNC类型，学生上传的答辩视频）',
  `async_video_status` VARCHAR(32)   NULL     COMMENT '异步视频状态（UPLOADED-已上传 TRANSCODING-转码中 READY-可播放）',
  `score`           DECIMAL(6,2)     NULL     COMMENT '答辩成绩（评委给出）',
  `comment`         TEXT             NULL     COMMENT '评委评语',
  `score_submitted_at` DATETIME      NULL     COMMENT '评委提交成绩时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已移除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`defense_group_id`, `user_id`) COMMENT '同一用户在同一答辩组只有一条记录',
  KEY `idx_user_batch` (`user_id`, `batch_id`) COMMENT '用户查看自己参与的答辩组'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='答辩组成员表：记录评委和学生的参与关系，score字段记录评委打分，async_video_url支持异步答辩视频上传';

-- End of V1.0.0 migration for thesis_im
