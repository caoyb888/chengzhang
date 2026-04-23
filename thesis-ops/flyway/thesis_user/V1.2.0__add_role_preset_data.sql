-- ---------------------------------------------------------------------------
-- V1.2.0__add_role_preset_data.sql
-- 系统预置角色及角色-权限关联数据
-- 说明：本脚本初始化5个系统预置角色（school_id=0），并为每个角色分配默认权限。
--       预置角色编码与后端 RoleServiceImpl.PRESET_ROLE_CODES 保持一致。
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. 插入系统预置角色（school_id=0 表示系统内置，不可删除）
-- ===========================================================================
INSERT INTO role (id, school_id, role_code, role_name, description, is_preset, is_active, sort_order) VALUES
(1, 0, 'SCHOOL_ADMIN', '学校管理员',   '系统预置角色：学校管理员',   1, 1, 10),
(2, 0, 'POINT_ADMIN',  '教学点管理员', '系统预置角色：教学点管理员', 1, 1, 20),
(3, 0, 'TEACHER',      '指导教师',     '系统预置角色：指导教师',     1, 1, 30),
(4, 0, 'ASSISTANT',    '辅助指导教师', '系统预置角色：辅助指导教师', 1, 1, 40),
(5, 0, 'STUDENT',      '学生',         '系统预置角色：学生',         1, 1, 50);

-- ===========================================================================
-- 2. 角色-权限关联数据
--    每个预置角色分配与其职责匹配的默认权限。
--    注意：权限 ID 来自 V1.1.0__add_permission_preset_data.sql
-- ===========================================================================

-- SCHOOL_ADMIN（学校管理员）— 拥有全部权限
INSERT INTO role_permission (role_id, perm_id) VALUES
(1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),  (1, 7),  (1, 8),  (1, 9),  (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25), (1, 26), (1, 27),
(1, 51), (1, 52), (1, 53), (1, 54),
(1, 61), (1, 62), (1, 63), (1, 64), (1, 65), (1, 66), (1, 67), (1, 68), (1, 69), (1, 70), (1, 71),
(1, 81), (1, 82), (1, 83),
(1, 91), (1, 92),
(1, 96),
(1, 101),
(1, 106);

-- POINT_ADMIN（教学点管理员）— 用户/教学点/批次/论文/统计/IM
INSERT INTO role_permission (role_id, perm_id) VALUES
(2, 1),   -- perm:user:list
(2, 2),   -- perm:teaching-point:list
(2, 9),   -- perm:teaching-point:manage
(2, 12),  -- user:read
(2, 17),  -- teaching-point:create
(2, 18),  -- teaching-point:read
(2, 19),  -- teaching-point:update
(2, 20),  -- teaching-point:delete
(2, 51),  -- perm:batch:list
(2, 53),  -- perm:batch:manage
(2, 61),  -- perm:paper:list
(2, 62),  -- perm:signature:list
(2, 63),  -- perm:relationship:manage
(2, 64),  -- perm:paper:review
(2, 65),  -- perm:paper:annotate
(2, 67),  -- perm:signature:review
(2, 68),  -- paper:relationship:create
(2, 69),  -- paper:relationship:query
(2, 70),  -- paper:relationship:update
(2, 71),  -- paper:relationship:delete
(2, 91),  -- perm:statistics:view
(2, 92),  -- perm:statistics:export
(2, 101); -- perm:im:manage

-- TEACHER（指导教师）— 论文审阅评分/指导关系/签名/统计/IM
INSERT INTO role_permission (role_id, perm_id) VALUES
(3, 61),  -- perm:paper:list
(3, 62),  -- perm:signature:list
(3, 63),  -- perm:relationship:manage
(3, 64),  -- perm:paper:review
(3, 65),  -- perm:paper:annotate
(3, 66),  -- perm:paper:score
(3, 67),  -- perm:signature:review
(3, 69),  -- paper:relationship:query
(3, 70),  -- paper:relationship:update
(3, 71),  -- paper:relationship:delete
(3, 91),  -- perm:statistics:view
(3, 101); -- perm:im:manage

-- ASSISTANT（辅助指导教师）— 论文审阅批注/指导关系/签名/IM
INSERT INTO role_permission (role_id, perm_id) VALUES
(4, 61),  -- perm:paper:list
(4, 63),  -- perm:relationship:manage
(4, 64),  -- perm:paper:review
(4, 65),  -- perm:paper:annotate
(4, 69),  -- paper:relationship:query
(4, 101); -- perm:im:manage

-- STUDENT（学生）— 论文/AI/IM/答辩
INSERT INTO role_permission (role_id, perm_id) VALUES
(5, 61),  -- perm:paper:list
(5, 81),  -- perm:ai:ideology
(5, 82),  -- perm:ai:evaluate
(5, 83),  -- perm:ai:plagiarism
(5, 101), -- perm:im:manage
(5, 106); -- perm:defense:manage
