ALTER TABLE `user_course`
  ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间（BaseBean 继承字段）' AFTER `join_time`,
  ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    COMMENT '修改时间（BaseBean 继承字段）' AFTER `create_time`;

ALTER TABLE `homework_submit`
  ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间' AFTER `word_count`;

ALTER TABLE `homework_comment`
  ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    COMMENT '修改时间' AFTER `create_time`;

-- ============================================================
-- 课程详情扩展：增加课程大纲与课程介绍字段
-- 用途：CourseDetail.vue 的"课程信息"Tab 中展示课程大纲（syllabus，JSON数组）
--       和课程介绍（intro，长文本）。允许教师在课程详情页编辑这两项。
-- ============================================================
ALTER TABLE `course`
  ADD COLUMN `syllabus` text DEFAULT NULL
    COMMENT '课程大纲（JSON数组，每项 {week, title, content, type}，CourseDetail.vue 的"课程大纲"卡片渲染）' AFTER `cover`,
  ADD COLUMN `intro`   text DEFAULT NULL
    COMMENT '课程介绍（长文本，CourseDetail.vue 的"课程介绍"卡片渲染）' AFTER `syllabus`;

-- ============================================================
-- 课程卡片扩展：增加授课教师名快照
-- 设计思路：建课时把教师真实姓名快照进 course 表，
--   避免后续教师改名（user.name 变化）导致历史课程显示变化
--   也避免 list 接口每次 join user 表（性能更好）
-- ============================================================
ALTER TABLE `course`
  ADD COLUMN `teacher_name` varchar(50) DEFAULT NULL
    COMMENT '授课教师姓名（建课时从 user.name 快照）' AFTER `creator_id`;

-- ============================================================
-- 历史数据回填：把已有课程的 teacher_name 从 user.name 同步
--   只回填 creator_id 指向真实存在的用户的课程；
--   孤立课程（creator_id 为 NULL 或对应用户已被删除）的 teacher_name 保持 NULL
-- ============================================================
UPDATE `course` c
LEFT JOIN `user` u ON c.creator_id = u.id
SET c.teacher_name = u.name
WHERE c.creator_id IS NOT NULL AND u.id IS NOT NULL;

-- ============================================================
-- 学期字典表 term
-- 用途：学年学期由"学院统一发布"作为字典数据，禁止前端随意输入字符串。
--       通过 term_id 外键关联到 course，前端只做下拉选择，不做文本输入。
-- 字段说明：
--   school_year  - 学年（如 "2024-2025"）
--   semester     - 学期（1=第一学期 / 2=第二学期）
--   display_name - 完整显示名（如 "2024-2025 第一学期"）
--   start_date / end_date - 该学期的起止日期
--   is_current   - 是否当前学期（用于高亮默认选中）
-- ============================================================
CREATE TABLE IF NOT EXISTS `term` (
  `id` bigint NOT NULL COMMENT '学期ID（雪花算法）',
  `school_year` varchar(20) NOT NULL COMMENT '学年（如 2024-2025）',
  `semester` tinyint(1) NOT NULL COMMENT '学期：1=第一学期 / 2=第二学期',
  `display_name` varchar(50) NOT NULL COMMENT '完整显示名（如 2024-2025 第一学期）',
  `start_date` date DEFAULT NULL COMMENT '学期开始日期',
  `end_date` date DEFAULT NULL COMMENT '学期结束日期',
  `is_current` tinyint(1) DEFAULT 0 COMMENT '是否为当前学期（0=否，1=是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_year_semester` (`school_year`, `semester`),
  KEY `idx_current` (`is_current`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学年学期字典表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 学院字典表 department
-- 用途：学院名称属于"学校机构数据"，禁止前端随意输入字符串。
--       通过 department_id 外键关联到 course，前端只做下拉选择。
-- 字段说明：
--   school_id - 所属学校（关联 school.id），允许多学校共用同一张字典
-- ============================================================
CREATE TABLE IF NOT EXISTS `department` (
  `id` bigint NOT NULL COMMENT '学院ID（雪花算法）',
  `school_id` bigint DEFAULT NULL COMMENT '所属学校ID（关联 school.id）',
  `name` varchar(100) NOT NULL COMMENT '学院名称（如 计算机学院）',
  `code` varchar(50) DEFAULT NULL COMMENT '学院代码（如 CS / SE）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_school` (`school_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院字典表' ROW_FORMAT = Dynamic;

-- ============================================================
-- course 表加 term_id / department_id 外键列
--   term_id       - 关联 term.id（NULL=未指定学期，保留旧数据兼容）
--   department_id - 关联 department.id（NULL=未指定学院，保留旧数据兼容）
--   旧的 term 字符串字段保留作为冗余快照，避免关联表被清空时丢信息
-- ============================================================
ALTER TABLE `course`
  ADD COLUMN `term_id` bigint DEFAULT NULL
    COMMENT '学期ID（关联 term.id）' AFTER `term`,
  ADD COLUMN `department_id` bigint DEFAULT NULL
    COMMENT '学院ID（关联 department.id）' AFTER `term_id`;

-- 索引：term_id / department_id 单独建索引，便于按学期/学院统计
ALTER TABLE `course`
  ADD KEY `idx_term` (`term_id`),
  ADD KEY `idx_department` (`department_id`);

-- ============================================================
-- 学期字典初始化数据（4 条，覆盖 2 个学年）
--   只有 init 阶段才会插入，重复执行会被 UNIQUE KEY 拦截
--   如需重新初始化：先 TRUNCATE TABLE term; 再 INSERT
-- ============================================================
INSERT IGNORE INTO `term` (id, school_year, semester, display_name, start_date, end_date, is_current) VALUES
  (1856321478523700001, '2024-2025', 1, '2024-2025 第一学期', '2024-09-01', '2025-01-15', 0),
  (1856321478523700002, '2024-2025', 2, '2024-2025 第二学期', '2025-02-15', '2025-07-15', 0),
  (1856321478523700003, '2025-2026', 1, '2025-2026 第一学期', '2025-09-01', '2026-01-15', 0),
  (1856321478523700004, '2025-2026', 2, '2025-2026 第二学期', '2026-02-15', '2026-07-15', 1);

-- ============================================================
-- 学院字典初始化数据（北京大学下挂 4 个学院）
-- ============================================================
INSERT IGNORE INTO `department` (id, school_id, name, code) VALUES
  (1856321478523710001, 1856321478523691000, '计算机学院',           'CS'),
  (1856321478523710002, 1856321478523691000, '软件学院',             'SE'),
  (1856321478523710003, 1856321478523691000, '数学科学学院',         'MATH'),
  (1856321478523710004, 1856321478523691000, '信息科学与技术学院',   'INFO');