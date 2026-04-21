-- =============================================================================
-- Flyway Migration Script
-- Version    : V1.0.0
-- Database   : thesis_auth
-- Description: 初始化 thesis_auth 认证库全量 Schema
-- Created    : 2026-04-22
-- Author     : 宸章高等学历继续教育论文综合服务系统
-- =============================================================================

CREATE DATABASE IF NOT EXISTS thesis_auth
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thesis_auth;

-- ---------------------------------------------------------------------------
-- 表 auth_refresh_token — 刷新令牌表
-- 存储 Sa-Token 长期令牌，支持多端登录和强制下线
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auth_refresh_token (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL COMMENT '用户ID，关联 thesis_user.user.id',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID（多租户隔离）',
  `token_value`   VARCHAR(512)     NOT NULL COMMENT '刷新令牌值（UUID v4，存储哈希值）',
  `client_type`   VARCHAR(32)      NOT NULL DEFAULT 'WEB' COMMENT '客户端类型（WEB / WECHAT）',
  `device_info`   VARCHAR(256)     NULL     COMMENT '设备信息（UA 摘要，用于异地登录检测）',
  `ip_address`    VARCHAR(64)      NULL     COMMENT '颁发时的IP地址',
  `expire_at`     DATETIME         NOT NULL COMMENT '令牌过期时间',
  `is_revoked`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否已吊销（0-有效 1-已吊销）',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_value` (`token_value`(128))  COMMENT '令牌唯一索引（前128字符）',
  KEY `idx_user_client` (`user_id`, `client_type`, `is_revoked`) COMMENT '按用户和客户端类型查询有效令牌',
  KEY `idx_expire_at` (`expire_at`) COMMENT '过期清理任务索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='刷新令牌表：存储 Sa-Token 长期令牌，支持多端登录和强制下线';

-- ---------------------------------------------------------------------------
-- 表 auth_wechat_bind — 微信绑定表
-- 记录用户与微信 openid 的绑定关系，支持微信扫码电子签名
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auth_wechat_bind (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL COMMENT '绑定的用户ID',
  `school_id`     BIGINT UNSIGNED  NOT NULL COMMENT '所属学校ID',
  `open_id`       VARCHAR(128)     NOT NULL COMMENT '微信 openid（应用级唯一）',
  `union_id`      VARCHAR(128)     NULL     COMMENT '微信 unionid（跨应用唯一，可选）',
  `nickname`      VARCHAR(64)      NULL     COMMENT '微信昵称（快照）',
  `avatar_url`    VARCHAR(512)     NULL     COMMENT '微信头像URL',
  `bind_time`     DATETIME         NOT NULL COMMENT '绑定时间',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`) COMMENT '微信 openid 全局唯一',
  UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '一个用户只能绑定一个微信账号',
  KEY `idx_school` (`school_id`) COMMENT '按学校查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='微信账号绑定表：记录用户与微信 openid 的绑定关系，支持微信扫码电子签名';

-- ---------------------------------------------------------------------------
-- 表 auth_login_log — 登录日志表
-- 记录所有登录尝试，用于安全审计和暴力破解检测，保留180天
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auth_login_log (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT UNSIGNED  NULL     COMMENT '用户ID（登录失败时可为空）',
  `school_id`     BIGINT UNSIGNED  NULL     COMMENT '所属学校ID',
  `login_account` VARCHAR(64)      NOT NULL COMMENT '登录账号（手机号/用户名，脱敏存储）',
  `login_type`    VARCHAR(32)      NOT NULL COMMENT '登录方式（PASSWORD/SMS/WECHAT）',
  `login_result`  VARCHAR(32)      NOT NULL COMMENT '登录结果（SUCCESS/FAIL_PWD/FAIL_LOCKED/FAIL_NOT_EXIST）',
  `ip_address`    VARCHAR(64)      NOT NULL COMMENT '登录IP地址',
  `user_agent`    VARCHAR(512)     NULL     COMMENT '浏览器User-Agent',
  `fail_reason`   VARCHAR(256)     NULL     COMMENT '失败原因描述',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted`    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `created_at`) COMMENT '按用户查询登录历史',
  KEY `idx_account_result` (`login_account`, `login_result`, `created_at`) COMMENT '暴力破解检测索引',
  KEY `idx_created_at` (`created_at`) COMMENT '按时间清理历史日志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='登录日志表：记录所有登录尝试，用于安全审计和暴力破解检测，保留180天';

-- End of V1.0.0 migration for thesis_auth
