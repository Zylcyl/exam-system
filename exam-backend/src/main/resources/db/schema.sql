-- ============================================
-- 计算机考试系统 数据库初始化脚本
-- 数据库：H2 (MySQL 兼容模式)
-- ============================================

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(200),
    role_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_type VARCHAR(20) NOT NULL,  -- directory/menu/button
    path VARCHAR(200),
    component VARCHAR(200),
    permission VARCHAR(200),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);

-- 科目表
CREATE TABLE IF NOT EXISTS subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    description VARCHAR(500),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    question_type VARCHAR(30) NOT NULL,  -- single_choice/multi_choice/true_false/fill_blank/short_answer/file_upload/coding
    title TEXT NOT NULL,
    difficulty INT DEFAULT 3,  -- 1-简单 2-中等 3-困难
    score INT DEFAULT 5,
    analysis TEXT,
    knowledge_points VARCHAR(500),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 题目选项表（选择题用）
CREATE TABLE IF NOT EXISTS question_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_label VARCHAR(10),
    option_content TEXT NOT NULL,
    is_correct TINYINT DEFAULT 0,
    sort_order INT DEFAULT 0
);

-- 考试表
CREATE TABLE IF NOT EXISTS exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(200) NOT NULL,
    subject_id BIGINT NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT NOT NULL,  -- 考试时长（分钟）
    total_score INT DEFAULT 100,
    pass_score INT DEFAULT 60,
    status VARCHAR(20) DEFAULT 'NOT_START',  -- NOT_START/IN_PROGRESS/FINISHED
    is_random_order TINYINT DEFAULT 1,  -- 题目是否随机排列
    allow_retry TINYINT DEFAULT 0,
    max_cheat_count INT DEFAULT 3,  -- 最大切屏次数
    create_by BIGINT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 考试-题目关联表
CREATE TABLE IF NOT EXISTS exam_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT DEFAULT 0,
    score INT DEFAULT 5
);

-- 考生关联表
CREATE TABLE IF NOT EXISTS exam_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING',  -- WAITING/ANSWERING/FINISHED/MARKED
    start_time DATETIME,
    submit_time DATETIME,
    total_score INT,
    cheat_count INT DEFAULT 0,
    is_marked TINYINT DEFAULT 0
);

-- 答题记录表
CREATE TABLE IF NOT EXISTS student_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_type VARCHAR(30) NOT NULL,
    answer_content TEXT,
    answer_file_ids VARCHAR(500),  -- 文件上传题的文件ID
    is_correct TINYINT DEFAULT -1,  -- -1未判分 0错误 1正确
    score INT DEFAULT 0,
    marked_by BIGINT,
    mark_comment VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 成绩表
CREATE TABLE IF NOT EXISTS exam_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    total_score INT DEFAULT 0,
    objective_score INT DEFAULT 0,
    subjective_score INT DEFAULT 0,
    is_passed TINYINT DEFAULT 0,
    rank_position INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 文件附件表
CREATE TABLE IF NOT EXISTS file_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(200) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(50),
    upload_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500),
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    status TINYINT DEFAULT 1,
    error_msg TEXT,
    execute_time BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
