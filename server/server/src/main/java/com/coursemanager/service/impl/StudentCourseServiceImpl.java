package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.CourseMapper;
import com.coursemanager.mapper.UserCourseMapper;
import com.coursemanager.pojo.Course;
import com.coursemanager.pojo.UserCourse;
import com.coursemanager.service.IStudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程业务层实现类（学生端）
 */
@Service
public class StudentCourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements IStudentCourseService {

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Override
    @Transactional
    public Map<String, Object> joinCourseByCode(String joinCode, Long studentId) {
        // 1. 根据加课码查询课程
        Course course = this.getOne(
            new LambdaQueryWrapper<Course>().eq(Course::getJoinCode, joinCode)
        );
        if (course == null) {
            throw new RuntimeException("加课码无效或课程不存在");
        }

        // 2. 检查是否已加入
        boolean exists = userCourseMapper.exists(
            new LambdaQueryWrapper<UserCourse>()
                .eq(UserCourse::getUserId, studentId)
                .eq(UserCourse::getCourseId, course.getId())
        );
        if (exists) {
            throw new RuntimeException("你已经加入过这门课程了");
        }

        // 3. 建立关联（role=2 学生）
        UserCourse uc = new UserCourse();
        uc.setUserId(studentId);
        uc.setCourseId(course.getId());
        uc.setRole(2);
        uc.setIsTop(0);
        uc.setSortWeight(0);
        userCourseMapper.insert(uc);

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("courseId", course.getId());
        result.put("courseName", course.getCourseName());
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentCourseGroupedList(Long studentId) {
        // TODO(student-dev): 实现学生课程分组列表查询
        // 提示：从 user_course 表查询 role=2 的记录，关联 course 表和 user 表获取教师名称
        return new ArrayList<>();
    }
}
