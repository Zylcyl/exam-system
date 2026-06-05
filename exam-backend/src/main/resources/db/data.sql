-- 初始化角色数据（使用 MERGE 避免主键冲突）
MERGE INTO sys_role (id, role_name, role_code, description) KEY(id) VALUES
(1, '系统管理员', 'ROLE_ADMIN', '系统最高权限，管理所有用户和系统配置'),
(2, '教师', 'ROLE_TEACHER', '管理题库、创建考试、批阅试卷'),
(3, '学生', 'ROLE_STUDENT', '参加考试、查看成绩');

-- 初始化管理员账号（密码：admin123，BCrypt 加密）
MERGE INTO sys_user (id, username, password, real_name, role_id) KEY(id) VALUES
(1, 'admin', '$2a$10$Kx6IQRxxXmHiACi4giVWH.RiJUhJgle7VzgwyNUrVhbFhfXUdAx7a', '系统管理员', 1);

-- 初始化教师账号（密码：teacher123）
MERGE INTO sys_user (id, username, password, real_name, role_id) KEY(id) VALUES
(2, 'teacher', '$2a$10$dSAk.fhjho1jrdyByHAbOONEtOocxHZ2GQabPbbXjEiwWX6XzUlTG', '张老师', 2);

-- 初始化学员账号（密码：student123）
MERGE INTO sys_user (id, username, password, real_name, role_id) KEY(id) VALUES
(3, 'student', '$2a$10$dAqRsAiEr9fsVvyQW.Tg3uCxtymKgDXOjSld7Z1U3p5p5Trx1BQ3K', '李同学', 3);

-- 初始化科目
MERGE INTO subject (id, subject_name, parent_id, description) KEY(id) VALUES
(1, 'Java程序设计', 0, 'Java编程语言考试'),
(2, '数据结构与算法', 0, '数据结构和算法考试'),
(3, '计算机网络', 0, '计算机网络基础考试');

-- 初始化菜单/权限
MERGE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order) KEY(id) VALUES
-- 管理员菜单
(1, 0, '系统管理', 'directory', '/admin', NULL, NULL, 'Setting', 100),
(2, 1, '用户管理', 'menu', '/admin/user', 'admin/UserManage', 'admin:user:list', 'User', 1),
(3, 1, '角色管理', 'menu', '/admin/role', 'admin/RoleManage', 'admin:role:list', 'Avatar', 2),
(4, 1, '系统配置', 'menu', '/admin/config', 'admin/SysConfig', 'admin:config:list', 'Tools', 3),
(5, 1, '操作日志', 'menu', '/admin/log', 'admin/LogManage', 'admin:log:list', 'Document', 4),
-- 教师菜单
(10, 0, '教学管理', 'directory', '/teacher', NULL, NULL, 'Reading', 200),
(11, 10, '题库管理', 'menu', '/teacher/question', 'teacher/QuestionManage', 'teacher:question:list', 'Edit', 1),
(12, 10, '科目管理', 'menu', '/teacher/subject', 'teacher/SubjectManage', 'teacher:subject:list', 'Collection', 2),
(13, 10, '考试管理', 'menu', '/teacher/exam', 'teacher/ExamManage', 'teacher:exam:list', 'Tickets', 3),
(14, 10, '阅卷管理', 'menu', '/teacher/marking', 'teacher/MarkingManage', 'teacher:marking:list', 'Finished', 4),
(15, 10, '成绩统计', 'menu', '/teacher/statistics', 'teacher/Statistics', 'teacher:statistics:view', 'DataAnalysis', 5),
-- 学生菜单
(20, 0, '我的考试', 'directory', '/student', NULL, NULL, 'School', 300),
(21, 20, '考试列表', 'menu', '/student/exam', 'student/ExamList', 'student:exam:list', 'Tickets', 1),
(22, 20, '我的成绩', 'menu', '/student/score', 'student/MyScore', 'student:score:list', 'DataBoard', 2),
(23, 20, '错题本', 'menu', '/student/wrongbook', 'student/WrongBook', 'student:wrong:list', 'Notebook', 3);

-- 初始化角色-菜单关联（管理员拥有所有菜单）
MERGE INTO sys_role_menu (role_id, menu_id) KEY(role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(1, 20), (1, 21), (1, 22), (1, 23);

-- 教师菜单
MERGE INTO sys_role_menu (role_id, menu_id) KEY(role_id, menu_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15);

-- 学生菜单
MERGE INTO sys_role_menu (role_id, menu_id) KEY(role_id, menu_id) VALUES
(3, 20), (3, 21), (3, 22), (3, 23);
