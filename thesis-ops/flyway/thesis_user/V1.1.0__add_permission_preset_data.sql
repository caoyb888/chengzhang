-- ---------------------------------------------------------------------------
-- V1.1.0__add_permission_preset_data.sql
-- 系统预置权限数据
-- 说明：本脚本初始化系统全量权限定义，覆盖前后端已声明的所有权限编码。
--       当前项目中前后端权限编码格式尚未完全统一（前端大量使用 perm: 前缀，
--       后端以 模块:资源:操作 为主），为保证系统可正常运行，本脚本同时保留
--       两种格式；后续版本将统一为 模块:资源:操作 格式。
-- ---------------------------------------------------------------------------

-- 先清理已有预置权限（ID <= 200 为预置范围，开发环境可重复执行）
DELETE FROM permission WHERE id <= 200;

-- ===========================================================================
-- USER 模块 — 用户管理、教学点、角色、权限
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
-- 菜单权限（页面访问）
(1,  'perm:user:list',            '用户管理',          'MENU',   'USER', NULL, '/admin/user/student',       'User',         10, '学生管理/教师管理页面访问权限'),
(2,  'perm:teaching-point:list',  '教学点管理',        'MENU',   'USER', NULL, '/admin/user/teaching-point','OfficeBuilding',20, '教学点管理页面访问权限'),
(3,  'perm:role:list',            '角色权限',          'MENU',   'USER', NULL, '/admin/user/role',          'UserFilled',   30, '角色权限页面访问权限'),
-- 前端按钮权限
(4,  'perm:user:create',          '新增用户',          'BUTTON', 'USER', NULL, NULL, NULL, 40, '新增用户按钮权限'),
(5,  'perm:user:edit',            '编辑用户',          'BUTTON', 'USER', NULL, NULL, NULL, 50, '编辑用户按钮权限'),
(6,  'perm:user:delete',          '删除用户',          'BUTTON', 'USER', NULL, NULL, NULL, 60, '删除用户按钮权限'),
(7,  'perm:user:import',          '批量导入用户',      'BUTTON', 'USER', NULL, NULL, NULL, 70, '批量导入用户按钮权限'),
(8,  'perm:user:reset-pwd',       '重置密码',          'BUTTON', 'USER', NULL, NULL, NULL, 80, '重置用户密码按钮权限'),
(9,  'perm:teaching-point:manage','教学点管理',        'BUTTON', 'USER', NULL, NULL, NULL, 90, '教学点综合管理权限'),
(10, 'perm:role:manage',          '角色管理',          'BUTTON', 'USER', NULL, NULL, NULL, 100, '角色综合管理权限'),
-- 后端 API 权限（模块:资源:操作 格式）
(11, 'user:create',               '创建用户(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 110, '创建用户接口权限'),
(12, 'user:read',                 '查询用户(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 120, '查询用户接口权限'),
(13, 'user:update',               '更新用户(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 130, '更新用户接口权限'),
(14, 'user:delete',               '删除用户(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 140, '删除用户接口权限'),
(15, 'user:reset-password',       '重置密码(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 150, '重置密码接口权限'),
(16, 'user:update-status',        '更新用户状态(API)', 'BUTTON', 'USER', NULL, NULL, NULL, 160, '更新用户状态接口权限'),
(17, 'teaching-point:create',     '创建教学点(API)',   'BUTTON', 'USER', NULL, NULL, NULL, 170, '创建教学点接口权限'),
(18, 'teaching-point:read',       '查询教学点(API)',   'BUTTON', 'USER', NULL, NULL, NULL, 180, '查询教学点接口权限'),
(19, 'teaching-point:update',     '更新教学点(API)',   'BUTTON', 'USER', NULL, NULL, NULL, 190, '更新教学点接口权限'),
(20, 'teaching-point:delete',     '删除教学点(API)',   'BUTTON', 'USER', NULL, NULL, NULL, 200, '删除教学点接口权限'),
(21, 'role:create',               '创建角色(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 210, '创建角色接口权限'),
(22, 'role:read',                 '查询角色(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 220, '查询角色接口权限'),
(23, 'role:update',               '更新角色(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 230, '更新角色接口权限'),
(24, 'role:delete',               '删除角色(API)',     'BUTTON', 'USER', NULL, NULL, NULL, 240, '删除角色接口权限'),
(25, 'role:update-perm',          '更新角色权限(API)', 'BUTTON', 'USER', NULL, NULL, NULL, 250, '更新角色权限接口权限'),
(26, 'role:apply-preset',         '应用预置角色(API)', 'BUTTON', 'USER', NULL, NULL, NULL, 260, '应用预置角色接口权限'),
(27, 'permission:read',           '查询权限列表(API)', 'BUTTON', 'USER', NULL, NULL, NULL, 270, '查询权限树接口权限');

-- ===========================================================================
-- BATCH 模块 — 批次与流程管理
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(51, 'perm:batch:list',   '批次列表',     'MENU',   'BATCH', NULL, '/admin/batch/list',         'Calendar', 10, '批次列表页面访问权限'),
(52, 'perm:batch:config', '流程配置',     'MENU',   'BATCH', NULL, '/admin/batch/flow-config',  'Setting',  20, '流程配置页面访问权限'),
(53, 'perm:batch:manage', '批次学生管理', 'MENU',   'BATCH', NULL, '/admin/batch/students',     'User',     30, '批次学生管理页面访问权限'),
(54, 'perm:batch:create', '创建批次',     'BUTTON', 'BATCH', NULL, NULL, NULL, 40, '创建批次按钮权限');

-- ===========================================================================
-- PAPER 模块 — 论文管理、电子签名、指导关系
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
-- 菜单权限
(61, 'perm:paper:list',           '论文列表',          'MENU',   'PAPER', NULL, NULL,                        'Document',   10, '论文列表页面访问权限'),
(62, 'perm:signature:list',       '电子签名',          'MENU',   'PAPER', NULL, '/admin/paper/signature',      'EditPen',    20, '电子签名页面访问权限'),
(63, 'perm:relationship:manage',  '指导关系',          'MENU',   'PAPER', NULL, '/admin/paper/relationship',   'Connection', 30, '指导关系页面访问权限'),
-- 前端按钮权限
(64, 'perm:paper:review',         '论文审阅',          'BUTTON', 'PAPER', NULL, NULL, NULL, 40, '论文审阅按钮权限'),
(65, 'perm:paper:annotate',       '论文批注',          'BUTTON', 'PAPER', NULL, NULL, NULL, 50, '论文批注按钮权限'),
(66, 'perm:paper:score',          '论文评分',          'BUTTON', 'PAPER', NULL, NULL, NULL, 60, '论文评分按钮权限'),
(67, 'perm:signature:review',     '签名审核',          'BUTTON', 'PAPER', NULL, NULL, NULL, 70, '签名审核按钮权限'),
-- 后端 API 权限
(68, 'paper:relationship:create', '创建指导关系(API)', 'BUTTON', 'PAPER', NULL, NULL, NULL, 80, '创建指导关系接口权限'),
(69, 'paper:relationship:query',  '查询指导关系(API)', 'BUTTON', 'PAPER', NULL, NULL, NULL, 90, '查询指导关系接口权限'),
(70, 'paper:relationship:update', '更新指导关系(API)', 'BUTTON', 'PAPER', NULL, NULL, NULL, 100, '更新指导关系接口权限'),
(71, 'paper:relationship:delete', '删除指导关系(API)', 'BUTTON', 'PAPER', NULL, NULL, NULL, 110, '删除指导关系接口权限');

-- ===========================================================================
-- AI 模块 — AI 功能管理
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(81, 'perm:ai:ideology',   '意识形态扫描', 'MENU', 'AI', NULL, '/admin/ai/ideology-scan', 'WarningFilled', 10, '意识形态扫描页面访问权限'),
(82, 'perm:ai:evaluate',   '论文初步评议', 'MENU', 'AI', NULL, '/admin/ai/evaluate',      'MagicStick',    20, '论文初步评议页面访问权限'),
(83, 'perm:ai:plagiarism', '同届查重',     'MENU', 'AI', NULL, '/admin/ai/plagiarism',    'Search',        30, '同届查重页面访问权限');

-- ===========================================================================
-- STATISTICS 模块 — 统计报表
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(91, 'perm:statistics:view',   '统计报表',     'MENU',   'STATISTICS', NULL, '/admin/statistics', 'TrendCharts', 10, '统计报表页面访问权限'),
(92, 'perm:statistics:export', '导出报表',     'BUTTON', 'STATISTICS', NULL, NULL, NULL, 20, '导出统计报表按钮权限');

-- ===========================================================================
-- TEMPLATE 模块 — 模板管理
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(96, 'perm:template:manage', '模板管理', 'MENU', 'TEMPLATE', NULL, '/admin/template', 'Files', 10, '模板管理页面访问权限');

-- ===========================================================================
-- IM 模块 — 即时通讯/群组管理
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(101, 'perm:im:manage', '群组管理', 'MENU', 'IM', NULL, '/admin/im', 'ChatDotRound', 10, '群组管理页面访问权限');

-- ===========================================================================
-- DEFENSE 模块 — 答辩管理
-- ===========================================================================
INSERT INTO permission (id, perm_code, perm_name, perm_type, module, parent_id, route_path, icon, sort_order, description) VALUES
(106, 'perm:defense:manage', '答辩管理', 'MENU', 'DEFENSE', NULL, '/admin/defense', 'VideoCamera', 10, '答辩管理页面访问权限');

-- ===========================================================================
-- 注意：perm:user:import 为后端 UserImportController 已硬编码的权限，
--       此处同时保留该格式与 user:import 规范格式，确保鉴权通过。
--       后续统一编码规范时，建议将后端 @SaCheckPermission("perm:user:import")
--       统一修改为 @SaCheckPermission("user:import")，然后删除 perm:user:import 记录。
-- ===========================================================================
