package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.Course;

import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程业务层接口（学生端）
 */
public interface IStudentCourseService extends IService<Course> {

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
}
