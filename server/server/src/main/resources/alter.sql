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