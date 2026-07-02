SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 学校表 (school)
-- ----------------------------
DROP TABLE IF EXISTS `school`;
CREATE TABLE `school` (
  `id` bigint NOT NULL COMMENT '雪花算法主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学校名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学校代码',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属地区',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学校信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 用户基础信息表 (user)
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '雪花算法主键（MyBatis-Plus ASSIGN_ID，非自增）',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号/工号/唯一登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '绑定的手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像托管URL',
  `school_id` bigint DEFAULT NULL COMMENT '所属学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`),
  KEY `idx_school_id` (`school_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基础表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 课程信息表 (course)
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL,
  `course_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程编号',
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程名',
  `period` int NULL DEFAULT NULL COMMENT '学时',
  `credit` double NULL DEFAULT NULL COMMENT '学分',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '课程状态（0：未启用；1：启用）',
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教学班级（如: 201班）',
  `term` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年学期（如: 2022-2023 第二学期）',
  `join_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '6位唯一加课码',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程封面背景图URL',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建课堂的教师ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_join_code` (`join_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 用户-课程多对多关系表 (user_course)
-- ----------------------------
DROP TABLE IF EXISTS `user_course`;
CREATE TABLE `user_course` (
  `id` bigint NOT NULL COMMENT '关联ID（雪花算法）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `role` tinyint(1) NOT NULL COMMENT '课堂角色: 1=任课教师, 2=正式学生, 3=助教',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '个人层面的置顶状态: 0=未置顶, 1=已置顶',
  `sort_weight` int DEFAULT 0 COMMENT '拖拽排序权重值，越大越靠前',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入课堂时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（BaseBean 继承字段）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间（BaseBean 继承字段）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
  KEY `idx_course` (`course_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与课程多对多关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 作业发布主表 (homework)
-- ----------------------------
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework` (
  `id` bigint NOT NULL COMMENT '作业唯一主键（雪花算法）',
  `course_id` bigint NOT NULL COMMENT '所属课程主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '作业要求描述',
  `type` tinyint(1) DEFAULT 1 COMMENT '作业模式: 1=个人作业, 2=小组作业',
  `activity_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '作业' COMMENT '应用环节标签(如: 课前/课中/课后/期末)',
  `total_score` int DEFAULT 100 COMMENT '本次作业满分值',
  `forbid_late` tinyint(1) DEFAULT 0 COMMENT '超时提交策略: 0=允许超时, 1=超时禁止提交',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '老师上传的附件列表(JSON数组存储)',
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `deadline` datetime NOT NULL COMMENT '严格截止交作业时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业发布主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 学生作业提交与批阅表 (homework_submit)
-- ----------------------------
DROP TABLE IF EXISTS `homework_submit`;
CREATE TABLE `homework_submit` (
  `id` bigint NOT NULL COMMENT '提交记录唯一主键（雪花算法）',
  `homework_id` bigint NOT NULL COMMENT '作业发布ID',
  `student_id` bigint NOT NULL COMMENT '学生用户ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态: 0=未交, 1=已提交(待批阅), 2=老师已批改, 3=打回重做',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学生填写的文本回答',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学生提交的附件集合(JSON格式)',
  `submit_time` datetime DEFAULT NULL COMMENT '实际电子打卡提交时间',
  `score` int DEFAULT NULL COMMENT '教师给出的最终得分',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '教师留下的评语',
  `similarity` decimal(5,2) DEFAULT '0.00' COMMENT '查重相似度百分比',
  `word_count` int DEFAULT 0 COMMENT '自动统计的字数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（BaseBean 继承字段）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_homework_student` (`homework_id`, `student_id`),
  KEY `idx_student` (`student_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生作业提交与批阅状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 作业留言评论/讨论表 (homework_comment)
-- ----------------------------
DROP TABLE IF EXISTS `homework_comment`;
CREATE TABLE `homework_comment` (
  `id` bigint NOT NULL COMMENT '评论ID（雪花算法）',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `user_id` bigint NOT NULL COMMENT '发表评论的用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论正文',
  `is_anonymous` tinyint(1) DEFAULT 0 COMMENT '是否匿名发表: 0=实名, 1=匿名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发表时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间（BaseBean 继承字段）',
  PRIMARY KEY (`id`),
  KEY `idx_homework` (`homework_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业详情页评论互动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 学期字典表 (term)
-- ----------------------------
DROP TABLE IF EXISTS `term`;
CREATE TABLE `term` (
  `id` bigint NOT NULL COMMENT '学期ID（雪花算法）',
  `school_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学年（如 2024-2025）',
  `semester` tinyint(1) NOT NULL COMMENT '学期：1=第一学期 / 2=第二学期',
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '完整显示名（如 2024-2025 第一学期）',
  `start_date` date DEFAULT NULL COMMENT '学期开始日期',
  `end_date` date DEFAULT NULL COMMENT '学期结束日期',
  `is_current` tinyint(1) DEFAULT 0 COMMENT '是否为当前学期（0=否，1=是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_year_semester` (`school_year`, `semester`),
  KEY `idx_current` (`is_current`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学年学期字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 9. 学院字典表 (department)
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint NOT NULL COMMENT '学院ID（雪花算法）',
  `school_id` bigint DEFAULT NULL COMMENT '所属学校ID（关联 school.id）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学院名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学院代码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_school` (`school_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院字典表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 测试数据
-- BCrypt 加密结果对应明文：123456
-- ============================================================

-- 学校数据
INSERT INTO `school` (id, name, code, region, create_time) VALUES
  (1856321478523691000, '北京大学', 'PKU', '北京', NOW()),
  (1856321478523691001, '清华大学', 'THU', '北京', NOW()),
  (1856321478523691002, '复旦大学', 'FDU', '上海', NOW()),
  (1856321478523691003, '浙江大学', 'ZJU', '杭州', NOW()),
  (1856321478523691004, '南京大学', 'NJU', '南京', NOW());

-- 教师测试账号：T001 / 123456（属于北京大学）
INSERT INTO `user` (id, account, password, name, phone, avatar, school_id, create_time, update_time)
VALUES
  (1856321478523690000, 'T001', '$2b$10$BcWbKT8Y9Yu97xujrh3gt.5G1tGv1HZumVw300xiSAX7BYpDwaegC', '张老师', '13800138000', NULL, 1856321478523691000, NOW(), NOW());

-- 学生测试账号：S001 / 123456（属于北京大学）
INSERT INTO `user` (id, account, password, name, phone, avatar, school_id, create_time, update_time)
VALUES
  (1856321478523690001, 'S001', '$2b$10$BcWbKT8Y9Yu97xujrh3gt.5G1tGv1HZumVw300xiSAX7BYpDwaegC', '张三', '13800138001', NULL, 1856321478523691000, NOW(), NOW());
