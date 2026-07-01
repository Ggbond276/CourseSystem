package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.HomeworkComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业评论持久层
 */
@Mapper
public interface HomeworkCommentMapper extends BaseMapper<HomeworkComment> {

    /**
     * 作业评论列表查询（教师 / 学生共用）
     * 联表 user 获取评论者姓名和头像
     * 匿名逻辑：
     *   - isAnonymous = 1（匿名） → userName 显示"匿名用户"，userAvatar 为 null
     *   - isAnonymous = 0（实名） → 返回真实姓名和头像
     *
     * @param homeworkId 作业主键
     * @return 评论列表，每项字段：commentId, userName, userAvatar, content, createTime
     */
    java.util.List<java.util.Map<String, Object>> selectCommentList(
            @org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId);
}
