package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.UserCourse;

import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程业务层接口（学生端）
 *
 * 注意：本接口继承 IService<UserCourse>，因为学生端业务的核心是
 *       操作"用户-课程关联表(user_course)"，而不是直接 CRUD 课程主表。
 *       Course 的查询通过注入的 CourseMapper 完成。
 */
public interface IStudentCourseService extends IService<UserCourse> {

    /**
     * 凭加课码申请加入课堂
     * @param joinCode 加课码
     * @param studentId 学生用户ID
     * @return 加入成功返回课程基本信息（courseId, courseName）
     */
    Map<String, Object> joinCourseByCode(String joinCode, Long studentId);

    /**
     * 获取学生已加入的课程列表（按学期分组）
     * @param studentId 学生用户ID
     * @return 分组后的课程数据
     */
    List<Map<String, Object>> getStudentCourseGroupedList(Long studentId);

    /**
     * 获取课程详情（学生端）
     * @param courseId 课程ID
     * @param studentId 学生用户ID（用于权限校验）
     * @return 课程详情，不存在或无权限返回 null
     */
    Map<String, Object> getStudentCourseDetail(Long courseId, Long studentId);
}
