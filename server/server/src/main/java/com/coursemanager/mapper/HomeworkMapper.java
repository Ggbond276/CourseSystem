package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.HomeworkCondition;
import com.coursemanager.pojo.Homework;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业持久层
 */
@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    /**
     * 条件查询作业列表
     * @param condition 查询条件
     * @return 作业列表
     */
    java.util.List<Homework> selectCondition(HomeworkCondition condition);

    /**
     * 教师作业总览列表查询（接口 2：GET /homework/teacher/list）
     * 联表 homework_submit 一次性统计每条作业的三种状态数量
     *
     * @param courseId 课程主键
     * @return List<Map> 每个 Map 对应 B1_API.md 接口2 文档中 data[] 的一项
     *         字段：homeworkId, title, activityTag, deadline, totalScore,
     *               gradedCount, ungradedCount, unsubmittedCount
     */
    java.util.List<java.util.Map<String, Object>> selectTeacherHomeworkOverview(@org.apache.ibatis.annotations.Param("courseId") Long courseId);
}
