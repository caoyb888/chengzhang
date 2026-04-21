-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_template
-- Description: 初始化 thesis_template 模板库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_template
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_template;

-- ---------------------------------------------------------------------------
-- 表 paper_template — 论文格式模板表
-- 学校定制的论文排版模板（招标要求▲一校多模板）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paper_template (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `name`            VARCHAR(128)     NOT NULL COMMENT '模板名称（如：本科毕业论文标准模板2024版）',
  `paper_type`      VARCHAR(32)      NOT NULL COMMENT '适用论文类型（GRADUATION-毕业论文 DEGREE-学位论文 ALL-通用）',
  `education_level` VARCHAR(32)      NULL     COMMENT '适用学历层次（UNDERGRADUATE-本科 JUNIOR_COLLEGE-专科 ALL-通用）',
  `major_ids_json`  JSON             NULL     COMMENT '适用专业ID列表（JSON数组，NULL=所有专业）',
  `styles_json`     JSON             NOT NULL COMMENT '排版样式配置（字体/字号/行距/页边距/页眉页脚等，详见附录A格式）',
  `file_url`        VARCHAR(512)     NOT NULL COMMENT '原始DOCX模板文件URL（MinIO存储，Apache POI填充时使用）',
  `preview_url`     VARCHAR(512)     NULL     COMMENT '模板预览图URL（PDF渲染截图）',
  `is_default`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否为学校默认模板（每所学校只能有一个默认）',
  `sort_order`      INT              NOT NULL DEFAULT 0 COMMENT '显示排序（正序）',
  `created_by`      BIGINT UNSIGNED  NOT NULL COMMENT '上传管理员ID',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_type` (`school_id`, `paper_type`, `is_deleted`) COMMENT '按学校和论文类型查询可用模板'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='论文格式模板表：学校定制的论文排版模板（招标要求▲一校多模板），Apache POI读取file_url的DOCX进行内容填充';

-- ---------------------------------------------------------------------------
-- 表 outline_template — 大纲模板表
-- 按专业配置（招标要求▲分专业大纲模板），每专业可设置多个模板
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS outline_template (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`   BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `major`       VARCHAR(128)     NOT NULL COMMENT '适用专业（精确匹配，每专业可配置多个大纲模板）',
  `name`        VARCHAR(128)     NOT NULL COMMENT '模板名称（如：计算机科学与技术专业大纲模板A）',
  `description` VARCHAR(256)     NULL     COMMENT '模板说明（适用场景/参考方向等）',
  `outline_json` JSON            NOT NULL COMMENT '大纲树结构（含各节点remark备注信息，供学生填写时参考）',
  `sort_order`  INT              NOT NULL DEFAULT 0 COMMENT '同专业内排序（正序）',
  `is_active`   TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否启用（0-停用 1-启用）',
  `created_by`  BIGINT UNSIGNED  NOT NULL COMMENT '创建管理员ID',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`  TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_major` (`school_id`, `major`, `is_active`, `sort_order`) COMMENT '学生按专业查询可用大纲模板列表（招标要求▲分专业大纲模板）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='大纲模板表：按专业配置（招标要求▲分专业大纲模板），每专业可设置多个模板，outline_json中每个节点包含remark字段（备注信息）';

-- ---------------------------------------------------------------------------
-- 表 score_scheme — 评分方案表
-- 学校配置的论文评分体系，定义总分、及格线等，与score_item组合使用
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS score_scheme (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `name`          VARCHAR(128)     NOT NULL COMMENT '评分方案名称（如：2024届毕业论文评分标准）',
  `paper_type`    VARCHAR(32)      NOT NULL COMMENT '适用论文类型（GRADUATION/DEGREE/ALL）',
  `total_score`   DECIMAL(6,2)     NOT NULL COMMENT '总满分（所有评分项满分之和，通常为100）',
  `pass_score`    DECIMAL(6,2)     NOT NULL DEFAULT 60 COMMENT '及格分数线',
  `good_score`    DECIMAL(6,2)     NOT NULL DEFAULT 75 COMMENT '良好分数线',
  `excellent_score` DECIMAL(6,2)   NOT NULL DEFAULT 90 COMMENT '优秀分数线',
  `is_active`     TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否启用（每所学校建议只有一个启用的方案）',
  `description`   VARCHAR(512)     NULL     COMMENT '方案说明',
  `created_by`    BIGINT UNSIGNED  NOT NULL COMMENT '创建管理员ID',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_active` (`school_id`, `is_active`, `paper_type`) COMMENT '查询学校当前启用的评分方案'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='评分方案表：学校配置的论文评分体系，定义总分、及格线等，与score_item组合使用';

-- ---------------------------------------------------------------------------
-- 表 score_item — 评分项目表
-- 定义论文评分的各个维度（招标要求▲在线评分细则在线查阅）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS score_item (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scheme_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属评分方案ID，关联 score_scheme.id',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '学校ID（冗余，加速查询）',
  `item_name`     VARCHAR(64)      NOT NULL COMMENT '评分项名称（如：选题合理性、内容质量、创新性、格式规范性）',
  `max_score`     DECIMAL(6,2)     NOT NULL COMMENT '本项满分（如：20.00）',
  `weight`        DECIMAL(5,2)     NULL     COMMENT '权重百分比（NULL=按max_score占total_score比例计算）',
  `description`   TEXT             NULL     COMMENT '评分细则描述（告知教师如何评分该项，前端评分页面展示）',
  `is_required`   TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否必填（1-教师必须为此项评分 0-可选）',
  `sort_order`    INT              NOT NULL DEFAULT 0 COMMENT '显示排序（正序）',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_scheme` (`scheme_id`, `sort_order`, `is_deleted`) COMMENT '加载某方案的所有评分项'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='评分项目表：定义论文评分的各个维度（招标要求▲在线评分细则在线查阅），教师逐项评分，系统自动汇总总分';

-- ---------------------------------------------------------------------------
-- 表 misc_template — 其他模板表（选题登记表/任务书）
-- 统一管理选题登记表和任务书两类模板
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS misc_template (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `template_type` VARCHAR(32)      NOT NULL COMMENT '模板类型（TOPIC_FORM-选题登记表 TASK_BOOK-任务书）',
  `name`          VARCHAR(128)     NOT NULL COMMENT '模板名称',
  `file_url`      VARCHAR(512)     NOT NULL COMMENT '模板文件URL（MinIO，DOCX格式）',
  `version`       VARCHAR(32)      NOT NULL DEFAULT '1.0' COMMENT '版本号（管理员更新时递增）',
  `is_active`     TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否当前使用（每种类型只有一个active）',
  `created_by`    BIGINT UNSIGNED  NOT NULL COMMENT '上传管理员ID',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_school_type_active` (`school_id`, `template_type`, `is_active`) COMMENT '获取学校当前有效的选题登记表/任务书模板'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='其他模板表：统一管理选题登记表和任务书两类模板，学生下载模板填写后上传（招标要求：模板下载和一键上传）';

-- End of V1.0.0 migration for thesis_template
