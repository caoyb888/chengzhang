-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_paper
-- Description: 初始化 thesis_paper 论文核心库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_paper
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_paper;

-- ---------------------------------------------------------------------------
-- 表 batch — 批次表
-- 论文管理的顶层组织单元，一个批次对应一次论文周期（学期）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS batch (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `name`            VARCHAR(128)     NOT NULL COMMENT '批次名称（如：2024年春季毕业论文批次）',
  `academic_year`   VARCHAR(16)      NOT NULL COMMENT '学年（如：2023-2024）',
  `semester`        VARCHAR(16)      NOT NULL COMMENT '学期（SPRING-春季 AUTUMN-秋季）',
  `paper_type`      VARCHAR(32)      NOT NULL COMMENT '论文类型（GRADUATION-毕业论文 DEGREE-学位论文）',
  `start_time`      DATETIME         NOT NULL COMMENT '批次整体开始时间（学生可见时间）',
  `end_time`        DATETIME         NOT NULL COMMENT '批次整体结束时间',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'DRAFT' COMMENT '批次状态（DRAFT-草稿 ACTIVE-进行中 FINISHED-已结束 ARCHIVED-已归档转存）',
  `description`     TEXT             NULL     COMMENT '批次说明（富文本，可包含注意事项）',
  `total_students`  INT              NOT NULL DEFAULT 0 COMMENT '批次学生总人数（冗余统计，更新时同步）',
  `created_by`      BIGINT UNSIGNED  NOT NULL COMMENT '创建人用户ID（学校管理员）',
  `archived_at`     DATETIME         NULL     COMMENT '归档时间（数据转存给学校的时间）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_status` (`school_id`, `status`, `is_deleted`) COMMENT '按学校查询有效批次，高频',
  KEY `idx_school_year` (`school_id`, `academic_year`, `semester`) COMMENT '按学年学期查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='批次表：论文管理的顶层组织单元，一个批次对应一次论文周期（学期），包含开始/结束时间和状态流转';

-- ---------------------------------------------------------------------------
-- 表 batch_flow_config — 批次流程节点配置表
-- 核心配置驱动表，定义每个批次中论文各阶段的规则、时间、审核要求
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS batch_flow_config (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id`            BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID，关联 batch.id',
  `school_id`           BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（冗余，加速过滤）',
  `node_type`           VARCHAR(32)      NOT NULL COMMENT '节点类型枚举（SIGN/TOPIC/OUTLINE/TOPIC_FORM/TASK_BOOK/DRAFT_SEG/DRAFT_FULL/FINAL_DRAFT/FINAL）',
  `node_name`           VARCHAR(64)      NOT NULL COMMENT '节点自定义显示名称（管理员可修改，如：将"大纲"改为"研究提纲"）',
  `sort_order`          INT              NOT NULL COMMENT '节点执行顺序（正序，数字越小越先执行，允许自定义调整）',
  `is_enabled`          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否启用该节点（0-跳过 1-启用）',
  `is_required`         TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否强制顺序（1-必须前置节点全部通过才能提交 0-可跳过前置）',
  `need_guide`          TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否需要指导（1-提交后进入指导流程 0-直接进入审核）',
  `need_review`         TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否需要审核（1-需要教师审核通过 0-学生提交即视为完成）',
  `allow_resubmit`      TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '被驳回后是否允许重新提交（0-不允许 1-允许）',
  `max_resubmit_count`  INT              NOT NULL DEFAULT 99 COMMENT '最大重新提交次数（99=不限制）',
  `start_time`          DATETIME         NOT NULL COMMENT '本节点开放提交的开始时间',
  `end_time`            DATETIME         NOT NULL COMMENT '本节点截止提交时间',
  `review_deadline`     DATETIME         NULL     COMMENT '教师审核截止时间（逾期视为自动通过，NULL表示无截止）',
  `min_comment_length`  INT              NOT NULL DEFAULT 0 COMMENT '教师评语最低字数要求（0=不限制，招标要求：须设置强制最低字数）',
  `description`         VARCHAR(512)     NULL     COMMENT '节点说明（对学生的操作提示）',
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`          TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_node` (`batch_id`, `node_type`) COMMENT '同一批次内节点类型唯一',
  KEY `idx_school_batch` (`school_id`, `batch_id`, `is_enabled`) COMMENT '按批次加载流程配置，高频查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='批次流程节点配置表：核心配置驱动表，定义每个批次中论文各阶段的规则、时间、审核要求。所有流程逻辑由此表驱动，禁止硬编码';

-- ---------------------------------------------------------------------------
-- 表 node_teacher_level — 节点导师层级配置表
-- 实现多导师机制，每个流程节点可配置多级导师，按level升序依次审核
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS node_teacher_level (
  `id`                    BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id`             BIGINT UNSIGNED  NOT NULL COMMENT '所属流程节点配置ID，关联 batch_flow_config.id',
  `batch_id`              BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID（冗余，加速查询）',
  `level`                 TINYINT UNSIGNED NOT NULL COMMENT '导师层级（1=第一级，数字越小越先审核）',
  `level_name`            VARCHAR(64)      NOT NULL COMMENT '层级名称（如：专业指导教师、院系审核教师）',
  `allow_reject`          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否允许退回论文给学生（1-允许 0-只能通过或转高层级）',
  `review_mode`           VARCHAR(32)      NOT NULL DEFAULT 'ALL' COMMENT '评审模式（ALL-全检，对所有学生论文审核；SAMPLE-抽检，按比例随机分配）',
  `sample_rate`           DECIMAL(5,2)     NULL     COMMENT '抽检比例（review_mode=SAMPLE时有效，如：0.30=抽检30%）',
  `push_to_student`       TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '评审意见是否推送给学生可见（1-推送 0-仅内部可见）',
  `is_final_level`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否末级（1-末级通过后节点完成 0-通过后流转至下一层级）',
  `auto_approve_hours`    INT              NULL     COMMENT '超时自动通过小时数（NULL=不自动通过，配合review_deadline使用）',
  `created_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`            TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_level` (`config_id`, `level`) COMMENT '同一节点内层级编号唯一',
  KEY `idx_batch_config` (`batch_id`, `config_id`, `level`) COMMENT '加载批次导师层级配置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='节点导师层级配置表：实现多导师机制，每个流程节点可配置多级导师，按level升序依次审核。招标要求的关键功能▲';

-- ---------------------------------------------------------------------------
-- 表 student_batch — 学生批次关联表
-- 记录学生参与哪些批次，同一学生可参与多个批次（不同届）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_batch (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id`    BIGINT UNSIGNED  NOT NULL COMMENT '学生用户ID，关联 thesis_user.user.id',
  `batch_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批次ID，关联 batch.id',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（多租户隔离）',
  `major`         VARCHAR(128)     NOT NULL COMMENT '当前专业（从user表快照，因学生可能转专业）',
  `teaching_point_id` BIGINT UNSIGNED NULL  COMMENT '当前所属教学点ID（快照）',
  `enrolled_by`   BIGINT UNSIGNED  NOT NULL COMMENT '添加操作人ID（管理员）',
  `enrolled_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入批次时间',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_batch` (`student_id`, `batch_id`) COMMENT '同一学生在同一批次只能出现一次',
  KEY `idx_batch_point` (`batch_id`, `teaching_point_id`, `is_deleted`) COMMENT '教学点管理员查询本批次本教学点学生',
  KEY `idx_batch_major` (`batch_id`, `major`) COMMENT '按专业统计批次学生'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='学生批次关联表：记录学生参与哪些批次，同一学生可参与多个批次（不同届）';

-- ---------------------------------------------------------------------------
-- 表 teaching_relationship — 师生指导关系表
-- 支持一个学生对应多个教师（不同类型和层级），一个教师对应多个学生
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS teaching_relationship (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`           BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `batch_id`            BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID（指导关系按批次区分）',
  `student_id`          BIGINT UNSIGNED  NOT NULL COMMENT '学生用户ID',
  `teacher_id`          BIGINT UNSIGNED  NOT NULL COMMENT '教师用户ID',
  `teacher_type`        VARCHAR(32)      NOT NULL COMMENT '教师类型（MAIN-指导教师 ASSIST-辅助指导教师）',
  `level`               TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '指导层级（对应node_teacher_level.level，用于多导师场景）',
  `teaching_point_id`   BIGINT UNSIGNED  NULL     COMMENT '教学点ID（辅助教师归属的教学点，用于数据权限过滤）',
  `assign_type`         VARCHAR(32)      NOT NULL DEFAULT 'MANUAL' COMMENT '分配方式（IMPORT-模板导入 MANUAL-手工单个匹配）',
  `assigned_by`         BIGINT UNSIGNED  NOT NULL COMMENT '分配操作人ID',
  `is_active`           TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否有效（0-已解除 1-有效）',
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`          TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_student_teacher_type` (`batch_id`, `student_id`, `teacher_id`, `teacher_type`, `level`) COMMENT '同批次同学生同教师同类型同层级唯一',
  KEY `idx_batch_student` (`batch_id`, `student_id`, `is_active`) COMMENT '查询学生的所有导师，高频',
  KEY `idx_batch_teacher` (`batch_id`, `teacher_id`, `teacher_type`, `is_active`) COMMENT '查询教师的所有学生，高频',
  KEY `idx_school_point` (`school_id`, `teaching_point_id`, `batch_id`) COMMENT '教学点管理端查询本点指导关系'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='师生指导关系表：支持一个学生对应多个教师（不同类型和层级），一个教师对应多个学生。指导关系是即时通讯群组创建的依据';

-- ---------------------------------------------------------------------------
-- 表 paper — 论文主表
-- 每个学生在每个批次对应一条记录
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper (
  `id`                    BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID（论文全局唯一标识）',
  `school_id`             BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `batch_id`              BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID',
  `student_id`            BIGINT UNSIGNED  NOT NULL COMMENT '论文归属学生ID',
  `teaching_point_id`     BIGINT UNSIGNED  NULL     COMMENT '学生所在教学点ID（冗余快照，用于数据权限过滤）',
  `major`                 VARCHAR(128)     NOT NULL COMMENT '学生专业（快照，避免专业变更影响历史数据）',
  `paper_type`            VARCHAR(32)      NOT NULL COMMENT '论文类型（GRADUATION-毕业论文 DEGREE-学位论文，从批次继承）',
  `current_node`          VARCHAR(32)      NULL     COMMENT '当前所在流程节点类型（NULL=尚未开始）',
  `overall_status`        VARCHAR(32)      NOT NULL DEFAULT 'NOT_STARTED' COMMENT '论文整体状态（NOT_STARTED/IN_PROGRESS/COMPLETED/FAILED）',
  `word_count`            INT              NOT NULL DEFAULT 0 COMMENT '论文当前总字数（编辑器实时更新）',
  `sign_confirmed`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '电子签名是否已确认（快速判断，避免关联查询）',
  `topic_confirmed`       TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '选题是否已确认通过',
  `outline_confirmed`     TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '大纲是否已确认通过',
  `draft_confirmed`       TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '初稿是否已确认通过',
  `final_draft_confirmed` TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '定稿是否已确认通过',
  `final_confirmed`       TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '终稿是否已确认通过',
  `total_score`           DECIMAL(6,2)     NULL     COMMENT '最终综合评分（由评分系统汇总填充）',
  `score_level`           VARCHAR(32)      NULL     COMMENT '评分等级（EXCELLENT/GOOD/PASS/FAIL）',
  `export_url`            VARCHAR(512)     NULL     COMMENT '最新导出的标准格式论文文件URL（MinIO）',
  `export_at`             DATETIME         NULL     COMMENT '最近一次导出时间',
  `created_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（通常为学生加入批次后自动创建）',
  `updated_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`            TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_student` (`batch_id`, `student_id`) COMMENT '同一批次内每个学生只有一篇论文',
  KEY `idx_school_batch_status` (`school_id`, `batch_id`, `overall_status`, `is_deleted`) COMMENT '管理端按批次和状态过滤，高频核心查询',
  KEY `idx_school_batch_point` (`school_id`, `batch_id`, `teaching_point_id`) COMMENT '教学点维度查询',
  KEY `idx_school_batch_major` (`school_id`, `batch_id`, `major`) COMMENT '专业维度查询',
  KEY `idx_student` (`student_id`, `batch_id`) COMMENT '学生端查询自己的论文'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文主表：每个学生在每个批次对应一条记录，is_xxx_confirmed字段是冗余快捷状态，避免关联paper_node_status查询全局状态';

-- ---------------------------------------------------------------------------
-- 表 paper_node_status — 论文节点状态表
-- 记录每篇论文在每个流程节点的当前状态，是统计报表的核心数据源
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_node_status (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`      BIGINT UNSIGNED  NOT NULL COMMENT '论文ID，关联 paper.id',
  `batch_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余，加速查询）',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `node_type`     VARCHAR(32)      NOT NULL COMMENT '节点类型（与batch_flow_config.node_type对应）',
  `status`        VARCHAR(32)      NOT NULL DEFAULT 'NOT_STARTED' COMMENT '节点状态（NOT_STARTED-未开始 PENDING-待提交 SUBMITTED-已提交 REVIEWING-审核中 APPROVED-已通过 REJECTED-已驳回）',
  `current_level` TINYINT UNSIGNED NULL     COMMENT '当前处于第几级导师审核（NULL=未到审核阶段）',
  `submit_count`  TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '学生提交次数（含重新提交次数）',
  `first_submit_at`  DATETIME      NULL     COMMENT '首次提交时间',
  `last_submit_at`   DATETIME      NULL     COMMENT '最后提交时间',
  `approved_at`      DATETIME      NULL     COMMENT '审核通过时间',
  `approved_by`      BIGINT UNSIGNED NULL   COMMENT '最终审核通过的教师用户ID',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_node` (`paper_id`, `node_type`) COMMENT '每篇论文的每个节点只有一条状态记录',
  KEY `idx_batch_node_status` (`batch_id`, `node_type`, `status`) COMMENT '统计某批次某节点各状态人数，高频统计查询',
  KEY `idx_school_batch_node` (`school_id`, `batch_id`, `node_type`, `status`) COMMENT '多维度统计索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文节点状态表：记录每篇论文在每个流程节点的当前状态，是统计报表的核心数据源';

-- ---------------------------------------------------------------------------
-- 表 paper_submit_record — 论文提交记录表
-- 记录学生每次提交的完整历史
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_submit_record (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`      BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `node_type`     VARCHAR(32)      NOT NULL COMMENT '提交的节点类型',
  `student_id`    BIGINT UNSIGNED  NOT NULL COMMENT '提交的学生用户ID',
  `submit_seq`    TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '本次提交序号（第1次/第2次重新提交...）',
  `submit_type`   VARCHAR(32)      NOT NULL DEFAULT 'FULL' COMMENT '提交类型（FULL-整篇提交 SEGMENT-分段提交）',
  `segment_node_id` VARCHAR(64)    NULL     COMMENT '分段提交时的大纲节点ID（与paper_outline中的节点id对应）',
  `content_snapshot` LONGTEXT      NULL     COMMENT '提交时的内容快照（JSON，用于历史版本追溯，大字段）',
  `file_url`      VARCHAR(512)     NULL     COMMENT '提交的文件URL（选题登记表/任务书等文件类型节点使用）',
  `file_name`     VARCHAR(256)     NULL     COMMENT '提交文件的原始文件名',
  `file_size`     BIGINT           NULL     COMMENT '文件大小（字节）',
  `word_count`    INT              NULL     COMMENT '提交时的字数统计（论文写作类节点）',
  `is_deadline_ext` TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否通过逾期提交通道提交（1-是 0-否）',
  `submitted_at`  DATETIME         NOT NULL COMMENT '提交时间（业务时间，与created_at区分）',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_node_seq` (`paper_id`, `node_type`, `submit_seq`) COMMENT '查询某论文某节点的提交历史',
  KEY `idx_batch_node_time` (`batch_id`, `node_type`, `submitted_at`) COMMENT '统计某节点提交时序分布',
  KEY `idx_student_batch` (`student_id`, `batch_id`, `node_type`) COMMENT '学生端查询自己的提交记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文提交记录表：记录学生每次提交的完整历史，content_snapshot保留版本快照支持历史查看，file_url用于文件类型节点';

-- ---------------------------------------------------------------------------
-- 表 paper_review_record — 论文审核记录表
-- 记录每次审核的完整信息，含评语和决定
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_review_record (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `submit_record_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的提交记录ID（审核的是哪次提交）',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `node_type`       VARCHAR(32)      NOT NULL COMMENT '审核的节点类型',
  `teacher_id`      BIGINT UNSIGNED  NOT NULL COMMENT '审核教师用户ID',
  `teacher_type`    VARCHAR(32)      NOT NULL COMMENT '教师类型（MAIN-指导教师 ASSIST-辅助指导教师）',
  `teacher_level`   TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '审核时的导师层级',
  `decision`        VARCHAR(32)      NOT NULL COMMENT '审核决定（APPROVED-通过 REJECTED-驳回 GUIDE_ONLY-仅指导不审核）',
  `comment`         TEXT             NULL     COMMENT '审核意见/指导建议（按配置强制最低字数，学生可见性由push_to_student控制）',
  `comment_length`  INT              NOT NULL DEFAULT 0 COMMENT '评语字数（冗余，用于统计和校验）',
  `is_visible_to_student` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '评语是否对学生可见（由node_teacher_level.push_to_student决定）',
  `reviewed_at`     DATETIME         NOT NULL COMMENT '审核时间（业务时间）',
  `review_duration_seconds` INT      NULL     COMMENT '审核耗时秒数（从打开论文到提交审核的时长，用于效率统计）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_node_teacher` (`paper_id`, `node_type`, `teacher_id`) COMMENT '查询某论文某节点的审核记录',
  KEY `idx_teacher_batch` (`teacher_id`, `batch_id`, `node_type`, `decision`) COMMENT '教师端查询自己的审核情况和统计',
  KEY `idx_batch_node_decision` (`batch_id`, `node_type`, `decision`) COMMENT '批次维度统计审核通过率'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文审核记录表：记录每次审核的完整信息，含评语和决定。多导师场景下同一提交记录可有多条审核记录（不同level）';

-- ---------------------------------------------------------------------------
-- 表 paper_annotation — 论文批注表
-- 记录教师对论文PDF的批注数据
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_annotation (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `submit_record_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的提交记录ID（批注属于哪个版本）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `teacher_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批注教师用户ID',
  `annot_type`      VARCHAR(32)      NOT NULL COMMENT '批注类型（TEXT_HIGHLIGHT-文本高亮 AREA_BOX-区域框选 FREE_TEXT-自由文本框）',
  `page_no`         SMALLINT UNSIGNED NOT NULL COMMENT '批注所在PDF页码（从1开始）',
  `position_json`   JSON             NOT NULL COMMENT '批注位置信息（{"x1":100,"y1":200,"x2":400,"y2":220}，PDF坐标系）',
  `selected_text`   TEXT             NULL     COMMENT '被选中的原文文本（文本高亮类型时记录）',
  `highlight_color` VARCHAR(16)      NOT NULL DEFAULT '#FFEB3B' COMMENT '高亮颜色（HEX格式，如：#FFEB3B）',
  `comment`         TEXT             NOT NULL COMMENT '批注内容（教师的意见）',
  `is_resolved`     TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否已处理（学生确认修改后标记为1）',
  `resolved_at`     DATETIME         NULL     COMMENT '处理时间（学生确认处理的时间）',
  `is_visible_to_student` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '批注是否对学生可见',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '批注创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（教师可撤回批注）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_submit` (`paper_id`, `submit_record_id`, `is_deleted`) COMMENT '加载某版本论文的所有批注，高频（PDF渲染时）',
  KEY `idx_teacher_paper` (`teacher_id`, `paper_id`) COMMENT '教师查看自己的批注',
  KEY `idx_page_no` (`submit_record_id`, `page_no`) COMMENT '按页码加载批注（翻页时按需加载）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文批注表：记录教师对论文PDF的批注数据，position_json存储坐标，前端PDF.js据此渲染批注图层。招标要求▲在线批注功能的数据基础';

-- ---------------------------------------------------------------------------
-- 表 paper_score — 论文评分表
-- 记录教师逐项评分和综合评语
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_score (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `teacher_id`      BIGINT UNSIGNED  NOT NULL COMMENT '评分教师用户ID',
  `scheme_id`       BIGINT UNSIGNED  NOT NULL COMMENT '评分方案ID，关联 thesis_template.score_scheme.id',
  `node_type`       VARCHAR(32)      NOT NULL COMMENT '评分所属节点（通常为FINAL-终稿评审时评分）',
  `item_scores_json` JSON            NOT NULL COMMENT '各评分项得分列表（[{"item_id":1,"score":18,"max_score":20}]）',
  `total_score`     DECIMAL(6,2)     NOT NULL COMMENT '汇总总分（系统自动计算）',
  `score_level`     VARCHAR(32)      NULL     COMMENT '评分等级（EXCELLENT≥90 GOOD≥75 PASS≥60 FAIL<60，按学校配置）',
  `overall_comment` TEXT             NOT NULL COMMENT '综合评语（强制最低字数，由batch_flow_config.min_comment_length控制）',
  `is_locked`       TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否锁定（提交后锁定，只有管理员可解锁重新评分）',
  `scored_at`       DATETIME         NOT NULL COMMENT '评分提交时间',
  `unlocked_by`     BIGINT UNSIGNED  NULL     COMMENT '解锁操作人ID（管理员操作记录）',
  `unlocked_at`     DATETIME         NULL     COMMENT '解锁时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_teacher_node` (`paper_id`, `teacher_id`, `node_type`) COMMENT '同一教师对同一论文同一节点只评一次分',
  KEY `idx_batch_score` (`batch_id`, `total_score`, `score_level`) COMMENT '批次评分分布统计',
  KEY `idx_teacher_batch` (`teacher_id`, `batch_id`) COMMENT '教师评分工作量统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文评分表：记录教师逐项评分和综合评语，item_scores_json存储各评分细则得分，total_score由系统自动汇总';

-- ---------------------------------------------------------------------------
-- 表 paper_topic — 论文选题表
-- 记录学生确定的选题，topic_hash+唯一索引是选题去重的数据库层保障
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_topic (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（选题去重在批次内进行）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `student_id`      BIGINT UNSIGNED  NOT NULL COMMENT '学生用户ID（冗余）',
  `title`           VARCHAR(512)     NOT NULL COMMENT '选题题目（论文标题）',
  `title_en`        VARCHAR(512)     NULL     COMMENT '英文题目（可选）',
  `topic_hash`      VARCHAR(64)      NOT NULL COMMENT '选题哈希值（MD5(batch_id+title.trim())，用于去重索引）',
  `source`          VARCHAR(32)      NOT NULL COMMENT '选题来源（SELF-学生自拟 AI-AI推荐 PRESET-教师预设）',
  `preset_topic_id` BIGINT UNSIGNED  NULL     COMMENT '教师预设选题ID（source=PRESET时关联）',
  `keywords`        VARCHAR(512)     NULL     COMMENT '关键词（逗号分隔，不超过5个）',
  `research_field`  VARCHAR(128)     NULL     COMMENT '研究领域方向',
  `teacher_approved_at` DATETIME     NULL     COMMENT '教师确认选题时间（流程审核通过时间）',
  `teacher_approved_by` BIGINT UNSIGNED NULL  COMMENT '确认选题的教师ID',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交选题时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper` (`paper_id`) COMMENT '每篇论文只有一个确定选题',
  UNIQUE KEY `uk_batch_hash` (`batch_id`, `topic_hash`) COMMENT '批次内选题哈希唯一（实现选题去重约束，配合Redisson分布式锁）',
  KEY `idx_batch_school` (`batch_id`, `school_id`, `source`) COMMENT '统计各来源选题数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文选题表：记录学生确定的选题，topic_hash+唯一索引是选题去重的数据库层保障，Redisson分布式锁是应用层保障，双重保证';

-- ---------------------------------------------------------------------------
-- 表 preset_topic — 教师预设选题表
-- 教师提前发布的可选题目，学生可直接选择
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS preset_topic (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID',
  `teacher_id`      BIGINT UNSIGNED  NOT NULL COMMENT '发布预设选题的教师ID',
  `title`           VARCHAR(512)     NOT NULL COMMENT '预设选题题目',
  `description`     TEXT             NULL     COMMENT '选题说明（学生可查看，包含研究要求/参考资料等）',
  `remark`          VARCHAR(512)     NULL     COMMENT '备注信息（如：直接扫码进入讨论群的二维码URL）',
  `major_limit`     VARCHAR(256)     NULL     COMMENT '限定专业（逗号分隔，NULL=不限专业）',
  `max_select_count` INT             NOT NULL DEFAULT 1 COMMENT '最多允许几名学生选择（1=独占，>1=允许多人选同题）',
  `selected_count`  INT              NOT NULL DEFAULT 0 COMMENT '已选人数（冗余计数，实时维护）',
  `is_active`       TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否开放选择（0-关闭 1-开放）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_batch_teacher` (`batch_id`, `teacher_id`, `is_active`) COMMENT '教师查看自己发布的预设选题',
  KEY `idx_batch_major` (`batch_id`, `major_limit`, `is_active`) COMMENT '学生按专业筛选可选题目'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='教师预设选题表：教师提前发布的可选题目，学生可直接选择（招标要求▲论文选题三种模式之一）';

-- ---------------------------------------------------------------------------
-- 表 paper_outline — 论文大纲表
-- 使用JSON存储树形大纲结构，支持历史版本
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_outline (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `source`          VARCHAR(32)      NOT NULL DEFAULT 'SELF' COMMENT '大纲来源（SELF-自行编写 AI-AI推荐 TEMPLATE-使用学校模板）',
  `template_id`     BIGINT UNSIGNED  NULL     COMMENT '使用的大纲模板ID（source=TEMPLATE时关联）',
  `outline_json`    JSON             NOT NULL COMMENT '大纲树形结构（JSON数组，见文档§2格式定义）',
  `node_count`      INT              NOT NULL DEFAULT 0 COMMENT '大纲节点总数（一级+二级+三级标题总计）',
  `version`         SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号（每次保存递增，保留最近10个历史版本）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建/最后保存时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-当前版本 1-历史版本）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_version` (`paper_id`, `version`, `is_deleted`) COMMENT '获取论文最新大纲和历史版本'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文大纲表：使用JSON存储树形大纲结构，支持历史版本，outline_json格式详见技术方案§5.3.2';

-- ---------------------------------------------------------------------------
-- 表 paper_content — 论文内容表（大字段分离）
-- 按章节（大纲节点）存储正文HTML，大字段与paper主表分离
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_content (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `outline_node_id` VARCHAR(64)      NOT NULL COMMENT '对应大纲节点ID（chapter节点ID，如node_001）',
  `node_title`      VARCHAR(256)     NULL     COMMENT '章节标题快照（冗余，避免关联outline查标题）',
  `content_html`    LONGTEXT         NOT NULL COMMENT '章节正文内容（WangEditor输出的HTML，含图片URL/公式LaTeX/表格HTML）',
  `word_count`      INT              NOT NULL DEFAULT 0 COMMENT '本章节字数（纯文本字数，不含HTML标签）',
  `version`         SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '内容版本号（每次保存递增）',
  `auto_saved_at`   DATETIME         NULL     COMMENT '最后自动保存时间（每60秒自动保存）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-当前版本 1-历史版本）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_node` (`paper_id`, `outline_node_id`, `is_deleted`) COMMENT '加载论文各章节内容，高频（编辑器打开时）',
  KEY `idx_paper_version` (`paper_id`, `version`) COMMENT '按版本查询内容历史'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC
  COMMENT='论文内容表：按章节（大纲节点）存储正文HTML，大字段与paper主表分离，避免主表扫描时IO放大。ROW_FORMAT=DYNAMIC支持大VARCHAR';

-- ---------------------------------------------------------------------------
-- 表 electronic_signature — 电子签名表
-- 记录学生和教师的电子签名图片及审核状态
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS electronic_signature (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT UNSIGNED  NOT NULL COMMENT '签名用户ID（学生或教师）',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '所属批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `user_type`       VARCHAR(32)      NOT NULL COMMENT '用户类型（STUDENT-学生 TEACHER-教师）',
  `sign_type`       VARCHAR(32)      NOT NULL COMMENT '签名方式（WECHAT-微信扫码手写 UPLOAD-上传图片）',
  `image_url`       VARCHAR(512)     NOT NULL COMMENT '签名图片URL（存储于MinIO，格式JPG/PNG）',
  `image_size`      INT              NULL     COMMENT '图片文件大小（字节）',
  `review_status`   VARCHAR(32)      NOT NULL DEFAULT 'PENDING' COMMENT '审核状态（PENDING-待审核 APPROVED-通过 REJECTED-驳回）',
  `reviewer_id`     BIGINT UNSIGNED  NULL     COMMENT '审核人用户ID（教学点管理员）',
  `reviewed_at`     DATETIME         NULL     COMMENT '审核时间',
  `reject_reason`   VARCHAR(256)     NULL     COMMENT '驳回原因',
  `wechat_session`  VARCHAR(128)     NULL     COMMENT '微信签名会话ID（WECHAT类型时记录，用于追溯）',
  `submitted_at`    DATETIME         NOT NULL COMMENT '学生/教师提交签名时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_batch` (`user_id`, `batch_id`) COMMENT '同一用户在同一批次只有一条签名记录',
  KEY `idx_batch_review` (`batch_id`, `review_status`, `user_type`) COMMENT '管理端按批次查询待审核签名进度',
  KEY `idx_school_batch` (`school_id`, `batch_id`, `user_type`, `review_status`) COMMENT '全校签名进度统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='电子签名表：记录学生和教师的电子签名图片及审核状态，支持微信扫码和图片上传两种方式';

-- ---------------------------------------------------------------------------
-- 表 paper_deadline_extension — 逾期提交通道授权表
-- 教师为特殊情况学生开启截止时间后的提交权限，全程留痕审计
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_deadline_extension (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`      BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `student_id`    BIGINT UNSIGNED  NOT NULL COMMENT '被授权逾期提交的学生ID',
  `node_type`     VARCHAR(32)      NOT NULL COMMENT '授权逾期提交的节点类型',
  `teacher_id`    BIGINT UNSIGNED  NOT NULL COMMENT '开启逾期通道的教师ID（招标要求▲：仅指导教师可开启）',
  `extend_hours`  INT              NOT NULL COMMENT '延期小时数（从通道开启时刻起的有效时长）',
  `expire_at`     DATETIME         NOT NULL COMMENT '逾期通道到期时间（= created_at + extend_hours）',
  `reason`        VARCHAR(512)     NOT NULL COMMENT '开启逾期通道的原因（必填，留存审计）',
  `is_used`       TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否已使用（学生实际提交后标记为1）',
  `used_at`       DATETIME         NULL     COMMENT '实际使用时间',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（通道开启时间）',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已撤销）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_node` (`paper_id`, `node_type`, `is_deleted`, `expire_at`) COMMENT '校验学生是否有有效的逾期授权，提交前查询',
  KEY `idx_teacher_batch` (`teacher_id`, `batch_id`) COMMENT '教师查看自己开启的逾期通道列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='逾期提交通道授权表：教师为特殊情况学生开启截止时间后的提交权限，全程留痕审计（招标要求▲特殊情况逾期提交通道）';

-- ---------------------------------------------------------------------------
-- 表 paper_operation_log — 论文操作日志表
-- 记录所有对论文的状态变更操作，不可删除不可修改，用于审计追溯
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_operation_log (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`      BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`      BIGINT UNSIGNED  NOT NULL COMMENT '批次ID（冗余）',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `operator_id`   BIGINT UNSIGNED  NOT NULL COMMENT '操作人用户ID',
  `operator_type` VARCHAR(32)      NOT NULL COMMENT '操作人类型（STUDENT/TEACHER/ASSIST/POINT_ADMIN/SCHOOL_ADMIN）',
  `op_type`       VARCHAR(64)      NOT NULL COMMENT '操作类型（SUBMIT/REVIEW_PASS/REVIEW_REJECT/ANNOTATE/SCORE/DEADLINE_EXT等）',
  `node_type`     VARCHAR(32)      NULL     COMMENT '相关节点类型（NULL=非节点操作）',
  `before_status` VARCHAR(32)      NULL     COMMENT '操作前状态',
  `after_status`  VARCHAR(32)      NULL     COMMENT '操作后状态',
  `op_detail`     JSON             NULL     COMMENT '操作详情（JSON，如审核决定、评语摘要等）',
  `ip_address`    VARCHAR(64)      NULL     COMMENT '操作IP地址',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_time` (`paper_id`, `created_at`) COMMENT '查询论文操作时间线',
  KEY `idx_operator_batch` (`operator_id`, `batch_id`, `op_type`) COMMENT '查询操作人的操作记录',
  KEY `idx_batch_time` (`batch_id`, `created_at`) COMMENT '批次级别审计查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文操作日志表：记录所有对论文的状态变更操作，不可删除不可修改，用于审计追溯。所有@Transactional状态变更必须同步写入此表';

-- ---------------------------------------------------------------------------
-- 表 plagiarism_check_record — 查重记录表
-- 记录同届查重和第三方查重结果，支持多次查重历史
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS plagiarism_check_record (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `student_id`      BIGINT UNSIGNED  NOT NULL COMMENT '学生用户ID',
  `check_type`      VARCHAR(32)      NOT NULL COMMENT '查重类型（INTRA_BATCH-同届查重 THIRD_PARTY-第三方查重，如知网/维普/万方）',
  `third_party_name` VARCHAR(64)     NULL     COMMENT '第三方查重系统名称（CNKI/VIP/WANFANG，check_type=THIRD_PARTY时填写）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'PROCESSING' COMMENT '查重状态（PROCESSING-处理中 SUCCESS-完成 FAILED-失败）',
  `similarity_rate` DECIMAL(5,2)     NULL     COMMENT '相似率（0.00-100.00，百分比）',
  `report_url`      VARCHAR(512)     NULL     COMMENT '查重报告文件URL（MinIO存储的PDF）',
  `report_data_json` JSON            NULL     COMMENT '结构化查重结果（相似来源列表，用于展示重复片段）',
  `check_duration_seconds` INT       NULL     COMMENT '查重耗时秒数',
  `error_message`   VARCHAR(256)     NULL     COMMENT '失败原因',
  `is_final`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否为最终版查重（学生提交给教师审核的那次）',
  `teacher_reviewed` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '教师是否已审核查重报告（0-未审核 1-已审核）',
  `teacher_id`      BIGINT UNSIGNED  NULL     COMMENT '审核查重报告的教师ID',
  `teacher_reviewed_at` DATETIME     NULL     COMMENT '教师审核时间',
  `submitted_at`    DATETIME         NOT NULL COMMENT '发起查重时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_final` (`paper_id`, `is_final`, `check_type`) COMMENT '获取论文的最终查重结果',
  KEY `idx_batch_similarity` (`batch_id`, `check_type`, `similarity_rate`) COMMENT '批次查重率分布统计',
  KEY `idx_teacher_review` (`teacher_id`, `teacher_reviewed`, `batch_id`) COMMENT '教师待审核查重报告列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='查重记录表：记录同届查重和第三方查重结果（招标要求▲同届查重），支持多次查重历史，is_final标记提交教师审核的版本';

-- ---------------------------------------------------------------------------
-- 表 ideology_scan_record — 意识形态扫描记录表
-- 记录AI对论文的意识形态扫描结果
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ideology_scan_record (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `triggered_by`    BIGINT UNSIGNED  NOT NULL COMMENT '触发扫描的操作人ID（管理员）',
  `trigger_type`    VARCHAR(32)      NOT NULL DEFAULT 'MANUAL' COMMENT '触发方式（MANUAL-手动触发 AUTO_BATCH-批量触发）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'PROCESSING' COMMENT '扫描状态（PROCESSING/SUCCESS/FAILED）',
  `overall_risk`    VARCHAR(16)      NULL     COMMENT '整体风险等级（LOW/MEDIUM/HIGH）',
  `scan_result_json` JSON            NULL     COMMENT '扫描结果详情（数组，含各维度风险评估，格式见技术方案§5.9.6）',
  `risk_item_count` INT              NULL     COMMENT '风险项总数',
  `high_risk_count` INT              NULL     COMMENT '高风险项数量',
  `error_message`   VARCHAR(256)     NULL     COMMENT '扫描失败原因',
  `scan_duration_seconds` INT        NULL     COMMENT '扫描耗时秒数',
  `submitted_at`    DATETIME         NOT NULL COMMENT '发起扫描时间',
  `completed_at`    DATETIME         NULL     COMMENT '扫描完成时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_risk` (`paper_id`, `overall_risk`, `completed_at`) COMMENT '查询论文最近扫描结果',
  KEY `idx_batch_risk` (`batch_id`, `overall_risk`, `status`) COMMENT '批次风险分布统计，管理端展示'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='意识形态扫描记录表：记录AI对论文的意识形态扫描结果（招标要求▲），scan_result_json存储各维度风险详情，high_risk_count驱动预警';

-- ---------------------------------------------------------------------------
-- 表 ai_preliminary_review — AI初步评议记录表
-- 记录AI对论文的自动评议结果
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_preliminary_review (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id`        BIGINT UNSIGNED  NOT NULL COMMENT '论文ID',
  `batch_id`        BIGINT UNSIGNED  NOT NULL COMMENT '批次ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（多租户隔离）',
  `triggered_by`    BIGINT UNSIGNED  NOT NULL COMMENT '触发操作人ID（管理员）',
  `trigger_type`    VARCHAR(32)      NOT NULL DEFAULT 'MANUAL' COMMENT '触发方式（MANUAL/AUTO_BATCH）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'PROCESSING' COMMENT '评议状态（PROCESSING/SUCCESS/FAILED）',
  `review_standard` VARCHAR(128)     NULL     COMMENT '评议标准名称（如：山东省教委毕业论文评议标准2024版）',
  `review_report`   LONGTEXT         NULL     COMMENT '评议报告（Markdown格式，含选题合理性/大纲完整性/内容质量/格式规范性等维度）',
  `overall_rating`  VARCHAR(32)      NULL     COMMENT '整体评价（EXCELLENT/GOOD/NEEDS_IMPROVEMENT/POOR）',
  `dimension_scores_json` JSON       NULL     COMMENT '各维度得分（JSON，如{"选题合理性":85,"大纲完整性":78}）',
  `error_message`   VARCHAR(256)     NULL     COMMENT '处理失败原因',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_paper_status` (`paper_id`, `status`, `created_at`) COMMENT '查询论文最新评议结果',
  KEY `idx_batch_rating` (`batch_id`, `overall_rating`, `status`) COMMENT '批次评议结果分布统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI初步评议记录表：记录AI对论文的自动评议结果（招标要求▲论文初步评议），异步处理，结果通过review_report字段Markdown格式展示';

-- End of V1.0.0 migration for thesis_paper
