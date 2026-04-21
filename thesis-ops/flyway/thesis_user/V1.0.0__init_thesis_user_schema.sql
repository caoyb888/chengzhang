-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_user
-- Description: 初始化 thesis_user 用户权限库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_user
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_user;

-- ---------------------------------------------------------------------------
-- 表 school — 学校表
-- 多租户顶层实体，每所学校对应独立数据域
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS school (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID（雪花算法）',
  `name`          VARCHAR(128)     NOT NULL COMMENT '学校全称（如：曲阜师范大学）',
  `short_name`    VARCHAR(32)      NULL     COMMENT '学校简称（如：曲师大）',
  `code`          VARCHAR(32)      NOT NULL COMMENT '学校编码（唯一，全大写字母，如：QFNU）',
  `logo_url`      VARCHAR(512)     NULL     COMMENT '学校Logo文件URL（存储于MinIO）',
  `contact_phone` VARCHAR(20)      NULL     COMMENT '学校联系电话',
  `address`       VARCHAR(256)     NULL     COMMENT '学校地址',
  `status`        VARCHAR(32)      NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE-正常 SUSPENDED-停用）',
  `expire_at`     DATETIME         NULL     COMMENT '服务到期时间（到期后自动停用）',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`) COMMENT '学校编码全局唯一',
  KEY `idx_status` (`status`, `is_deleted`) COMMENT '按状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='学校表：多租户顶层实体，每所学校对应独立数据域';

-- ---------------------------------------------------------------------------
-- 表 teaching_point — 教学点表
-- 学校下的管理单元，教学点管理员只能管理本教学点数据
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS teaching_point (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `name`            VARCHAR(128)     NOT NULL COMMENT '教学点名称（如：济南教学点）',
  `code`            VARCHAR(32)      NULL     COMMENT '教学点编码（学校内唯一）',
  `contact_name`    VARCHAR(32)      NULL     COMMENT '负责人姓名',
  `contact_phone`   VARCHAR(20)      NULL     COMMENT '负责人联系电话',
  `data_scope`      VARCHAR(32)      NOT NULL DEFAULT 'POINT' COMMENT '数据权限范围（POINT-本教学点 SCHOOL-全校，用于特殊授权）',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE-正常 INACTIVE-禁用）',
  `sort_order`      INT              NOT NULL DEFAULT 0 COMMENT '排序权重（正序，数字小的排前面）',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_id`, `code`) COMMENT '同一学校内教学点编码唯一',
  KEY `idx_school_status` (`school_id`, `status`, `is_deleted`) COMMENT '按学校查询有效教学点'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='教学点表：学校下的管理单元，教学点管理员只能管理本教学点数据';

-- ---------------------------------------------------------------------------
-- 表 user — 用户主表
-- 系统所有角色（管理员/教师/学生）的统一用户表
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID（雪花算法，全局唯一）',
  `school_id`         BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（多租户隔离核心字段）',
  `teaching_point_id` BIGINT UNSIGNED  NULL     COMMENT '所属教学点ID（学生和辅助教师必填，学校教师可为空）',
  `username`          VARCHAR(64)      NOT NULL COMMENT '登录账号（学校内唯一，支持自定义修改）',
  `phone`             VARCHAR(20)      NULL     COMMENT '手机号（脱敏存储，AES256加密，用于短信验证码登录）',
  `phone_hash`        VARCHAR(64)      NULL     COMMENT '手机号哈希（SHA256，用于手机号查重，明文不存储）',
  `email`             VARCHAR(128)     NULL     COMMENT '邮箱地址',
  `password_hash`     VARCHAR(128)     NULL     COMMENT '密码哈希（BCrypt cost=12，登录方式为纯微信时可为空）',
  `real_name`         VARCHAR(64)      NOT NULL COMMENT '真实姓名',
  `id_card`           VARCHAR(64)      NULL     COMMENT '身份证号（AES256加密存储，仅管理员可查看）',
  `user_type`         VARCHAR(32)      NOT NULL COMMENT '用户类型（SCHOOL_ADMIN/POINT_ADMIN/TEACHER/ASSISTANT/STUDENT）',
  `student_no`        VARCHAR(32)      NULL     COMMENT '学号（学生专用，学校内唯一）',
  `teacher_no`        VARCHAR(32)      NULL     COMMENT '工号（教师专用，学校内唯一）',
  `major`             VARCHAR(128)     NULL     COMMENT '专业名称（学生专用）',
  `department`        VARCHAR(128)     NULL     COMMENT '所属院系（教师专用）',
  `title`             VARCHAR(64)      NULL     COMMENT '职称（教师专用，如：副教授）',
  `avatar_url`        VARCHAR(512)     NULL     COMMENT '头像文件URL',
  `gender`            TINYINT(1)       NULL     COMMENT '性别（0-女 1-男 NULL-未知）',
  `status`            VARCHAR(32)      NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态（ACTIVE-正常 LOCKED-锁定 INACTIVE-停用）',
  `lock_until`        DATETIME         NULL     COMMENT '锁定到期时间（暴力破解自动锁定）',
  `last_login_at`     DATETIME         NULL     COMMENT '最后登录时间',
  `last_login_ip`     VARCHAR(64)      NULL     COMMENT '最后登录IP地址',
  `pwd_changed_at`    DATETIME         NULL     COMMENT '密码最后修改时间',
  `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（导入时间）',
  `updated_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`        TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_username` (`school_id`, `username`) COMMENT '同一学校内登录账号唯一',
  UNIQUE KEY `uk_school_student_no` (`school_id`, `student_no`) COMMENT '同一学校内学号唯一（NULL不计入唯一约束）',
  UNIQUE KEY `uk_school_teacher_no` (`school_id`, `teacher_no`) COMMENT '同一学校内工号唯一',
  KEY `idx_school_type_point` (`school_id`, `user_type`, `teaching_point_id`, `is_deleted`) COMMENT '按学校+类型+教学点过滤，高频查询',
  KEY `idx_phone_hash` (`phone_hash`) COMMENT '按手机号哈希查询（登录时）',
  KEY `idx_school_major` (`school_id`, `major`, `is_deleted`) COMMENT '按专业筛选学生'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户主表：系统所有角色（管理员/教师/学生）的统一用户表，学号/工号按角色使用';

-- ---------------------------------------------------------------------------
-- 表 role — 角色表
-- 支持系统预置角色和学校自定义角色
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id`   BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（0表示系统内置角色）',
  `role_code`   VARCHAR(64)      NOT NULL COMMENT '角色编码（学校内唯一，大写字母+下划线，如：SCHOOL_ADMIN）',
  `role_name`   VARCHAR(64)      NOT NULL COMMENT '角色显示名称（如：学校管理员）',
  `description` VARCHAR(256)     NULL     COMMENT '角色描述',
  `is_preset`   TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否系统预置角色（1-预置不可删除，0-自定义可删除）',
  `is_active`   TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用 1-启用）',
  `sort_order`  INT              NOT NULL DEFAULT 0 COMMENT '显示排序（正序）',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`  TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_id`, `role_code`) COMMENT '同一学校内角色编码唯一',
  KEY `idx_school_active` (`school_id`, `is_active`, `is_deleted`) COMMENT '按学校查询有效角色'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='角色表：支持系统预置角色（SCHOOL_ADMIN等）和学校自定义角色，支持角色套餐概念';

-- ---------------------------------------------------------------------------
-- 表 permission — 权限表
-- 系统全量权限定义，由开发维护，不支持学校自定义
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permission (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `perm_code`     VARCHAR(128)     NOT NULL COMMENT '权限编码（全局唯一，格式：模块:资源:操作，如：paper:submit:read）',
  `perm_name`     VARCHAR(64)      NOT NULL COMMENT '权限显示名称（如：查看论文提交）',
  `perm_type`     VARCHAR(32)      NOT NULL COMMENT '权限类型（MENU-菜单页面访问 BUTTON-按钮操作）',
  `module`        VARCHAR(32)      NOT NULL COMMENT '所属模块（PAPER/USER/STATISTICS/IM/DEFENSE/AI/TEMPLATE）',
  `parent_id`     BIGINT UNSIGNED  NULL     COMMENT '父权限ID（菜单权限的上级菜单，NULL表示顶级）',
  `route_path`    VARCHAR(256)     NULL     COMMENT '前端路由路径（菜单权限使用）',
  `icon`          VARCHAR(64)      NULL     COMMENT '菜单图标名称（Element Plus 图标名）',
  `sort_order`    INT              NOT NULL DEFAULT 0 COMMENT '同级排序（正序）',
  `description`   VARCHAR(256)     NULL     COMMENT '权限功能说明',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`) COMMENT '权限编码全局唯一',
  KEY `idx_module_type` (`module`, `perm_type`) COMMENT '按模块和类型加载权限树'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='权限表：系统全量权限定义，由开发维护，不支持学校自定义。perm_code对应@SaCheckPermission注解值';

-- ---------------------------------------------------------------------------
-- 表 role_permission — 角色权限关联表
-- 多对多，记录每个角色拥有的权限列表
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_permission (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id`       BIGINT UNSIGNED  NOT NULL COMMENT '角色ID，关联 role.id',
  `perm_id`       BIGINT UNSIGNED  NOT NULL COMMENT '权限ID，关联 permission.id',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`) COMMENT '角色-权限组合唯一',
  KEY `idx_perm_id` (`perm_id`) COMMENT '按权限查询关联角色'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='角色权限关联表：多对多，记录每个角色拥有的权限列表';

-- ---------------------------------------------------------------------------
-- 表 user_role — 用户角色关联表
-- 记录用户与角色的绑定关系，权限由角色聚合得出
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_role (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT UNSIGNED  NOT NULL COMMENT '用户ID，关联 user.id',
  `role_id`     BIGINT UNSIGNED  NOT NULL COMMENT '角色ID，关联 role.id',
  `school_id`   BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（冗余，加速数据权限过滤）',
  `granted_by`  BIGINT UNSIGNED  NULL     COMMENT '授权人用户ID（管理员操作）',
  `granted_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`  TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '用户-角色组合唯一',
  KEY `idx_role_school` (`role_id`, `school_id`) COMMENT '按角色查询关联用户'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户角色关联表：记录用户与角色的绑定关系，权限由角色聚合得出';

-- ---------------------------------------------------------------------------
-- 表 user_import_task — 用户导入任务表
-- 记录异步导入任务的状态和进度，前端通过task_id轮询
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_import_task (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id`         VARCHAR(64)      NOT NULL COMMENT '任务UUID（前端轮询进度使用）',
  `school_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `operator_id`     BIGINT UNSIGNED  NOT NULL COMMENT '操作人用户ID',
  `import_type`     VARCHAR(32)      NOT NULL COMMENT '导入类型（STUDENT/TEACHER/ASSISTANT/TEACHING_POINT/RELATIONSHIP）',
  `file_url`        VARCHAR(512)     NOT NULL COMMENT '上传的Excel文件URL（MinIO）',
  `total_count`     INT              NULL     COMMENT '总记录数（解析后填充）',
  `success_count`   INT              NOT NULL DEFAULT 0 COMMENT '成功导入数量',
  `fail_count`      INT              NOT NULL DEFAULT 0 COMMENT '失败记录数量',
  `status`          VARCHAR(32)      NOT NULL DEFAULT 'PENDING' COMMENT '任务状态（PENDING-待处理 PROCESSING-处理中 SUCCESS-完成 PARTIAL-部分成功 FAILED-失败）',
  `error_file_url`  VARCHAR(512)     NULL     COMMENT '错误明细文件URL（含失败原因的Excel）',
  `error_message`   VARCHAR(512)     NULL     COMMENT '任务级失败原因（如：文件格式错误）',
  `started_at`      DATETIME         NULL     COMMENT '开始处理时间',
  `finished_at`     DATETIME         NULL     COMMENT '完成时间',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`) COMMENT '任务ID全局唯一',
  KEY `idx_school_operator` (`school_id`, `operator_id`, `created_at`) COMMENT '按学校和操作人查询历史任务'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户批量导入任务表：记录异步导入任务的状态和进度，前端通过task_id轮询';

-- End of V1.0.0 migration for thesis_user
