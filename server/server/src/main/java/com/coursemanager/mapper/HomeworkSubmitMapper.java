package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.HomeworkSubmitCondition;
import com.coursemanager.pojo.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业提交记录持久层
 */
@Mapper
public interface HomeworkSubmitMapper extends BaseMapper<HomeworkSubmit> {

    /**
     * 条件查询作业提交列表
     * @param condition 查询条件
     * @return 提交记录列表
     */
    java.util.List<HomeworkSubmit> selectCondition(HomeworkSubmitCondition condition);

    /**
     * 查询某门课程的所有学生用户ID（role=2 的成员）
     * 用于「发布作业」时查询该课程所有学生，为每人创建一条 status=0 的待提交记录
     *
     * @param courseId 课程主键
     * @return 学生 user_id 列表（没有学生时返回空集合）
     */
    java.util.List<Long> selectStudentIdListByCourseId(Long courseId);

    java.util.List<java.util.Map<String, Object>> selectSubmitListForTeacher(
            @org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId,
            @org.apache.ibatis.annotations.Param("status") Integer status,
            @org.apache.ibatis.annotations.Param("studentName") String studentName);
}
