-- ---------------------------------------------------------------------------
-- V1.3.0__add_sensitive_word_preset_data.sql
-- 系统预置敏感词库数据
-- 说明：内置通用敏感词（school_id=0），涵盖政治、宗教、色情暴力、赌博广告等类别。
--       level=1 替换星号, level=2 拦截不发送, level=3 记录告警。
-- ---------------------------------------------------------------------------

-- 先清理已有系统预置敏感词（school_id=0，开发环境可重复执行）
DELETE FROM sensitive_word WHERE school_id = 0;

-- ===========================================================================
-- 政治敏感（POLITICAL）— level=2 拦截不发送
-- ===========================================================================
INSERT INTO sensitive_word (school_id, word, category, level, is_active, created_by) VALUES
(0, '法轮功',      'POLITICAL', 2, 1, NULL),
(0, '邪教',        'POLITICAL', 2, 1, NULL),
(0, '反动',        'POLITICAL', 2, 1, NULL),
(0, '颠覆国家政权','POLITICAL', 2, 1, NULL),
(0, '分裂国家',    'POLITICAL', 2, 1, NULL),
(0, '煽动暴乱',    'POLITICAL', 2, 1, NULL),
(0, '恐怖主义',    'POLITICAL', 2, 1, NULL),
(0, '极端主义',    'POLITICAL', 2, 1, NULL);

-- ===========================================================================
-- 宗教敏感（RELIGION）— level=2 拦截不发送
-- ===========================================================================
INSERT INTO sensitive_word (school_id, word, category, level, is_active, created_by) VALUES
(0, '邪教组织',    'RELIGION', 2, 1, NULL),
(0, '非法传教',    'RELIGION', 2, 1, NULL),
(0, '迷信活动',    'RELIGION', 2, 1, NULL);

-- ===========================================================================
-- 通用敏感（GENERAL）— 色情暴力
-- ===========================================================================
INSERT INTO sensitive_word (school_id, word, category, level, is_active, created_by) VALUES
(0, '色情',        'GENERAL', 2, 1, NULL),
(0, '淫秽',        'GENERAL', 2, 1, NULL),
(0, '暴力',        'GENERAL', 1, 1, NULL),
(0, '血腥',        'GENERAL', 1, 1, NULL),
(0, '虐待',        'GENERAL', 2, 1, NULL),
(0, '强奸',        'GENERAL', 2, 1, NULL),
(0, '杀人',        'GENERAL', 2, 1, NULL),
(0, '自杀',        'GENERAL', 1, 1, NULL),
(0, '毒品',        'GENERAL', 2, 1, NULL),
(0, '吸毒',        'GENERAL', 2, 1, NULL);

-- ===========================================================================
-- 通用敏感（GENERAL）— 赌博/诈骗/广告
-- ===========================================================================
INSERT INTO sensitive_word (school_id, word, category, level, is_active, created_by) VALUES
(0, '赌博',        'GENERAL', 2, 1, NULL),
(0, '博彩',        'GENERAL', 2, 1, NULL),
(0, '诈骗',        'GENERAL', 2, 1, NULL),
(0, '传销',        'GENERAL', 2, 1, NULL),
(0, '假证',        'GENERAL', 2, 1, NULL),
(0, '代写论文',    'GENERAL', 2, 1, NULL),
(0, '论文代写',    'GENERAL', 2, 1, NULL),
(0, '枪手',        'GENERAL', 2, 1, NULL),
(0, '代考',        'GENERAL', 2, 1, NULL),
(0, '作弊',        'GENERAL', 1, 1, NULL),
(0, '刷分',        'GENERAL', 1, 1, NULL);

-- ===========================================================================
-- 通用敏感（GENERAL）— 辱骂/歧视（level=1 替换星号）
-- ===========================================================================
INSERT INTO sensitive_word (school_id, word, category, level, is_active, created_by) VALUES
(0, '傻逼',        'GENERAL', 1, 1, NULL),
(0, '脑残',        'GENERAL', 1, 1, NULL),
(0, '混蛋',        'GENERAL', 1, 1, NULL),
(0, '垃圾',        'GENERAL', 1, 1, NULL),
(0, '白痴',        'GENERAL', 1, 1, NULL),
(0, '智障',        'GENERAL', 1, 1, NULL),
(0, '狗屎',        'GENERAL', 1, 1, NULL),
(0, '滚蛋',        'GENERAL', 1, 1, NULL),
(0, '去死',        'GENERAL', 1, 1, NULL),
(0, '贱人',        'GENERAL', 1, 1, NULL),
(0, '人渣',        'GENERAL', 1, 1, NULL);
