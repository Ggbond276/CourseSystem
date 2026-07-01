package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.CourseMapper;
import com.coursemanager.mapper.UserCourseMapper;
import com.coursemanager.pojo.Course;
import com.coursemanager.pojo.UserCourse;
import com.coursemanager.service.IStudentCourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程业务层实现类（学生端）
 *
 * 设计说明：
 * 1. 本类继承 ServiceImpl<UserCourseMapper, UserCourse>，
 *    因为学生端业务的核心是 user_course 关联表（增删改查）。
 * 2. 课程主表(course)只读不写，因此另注入 CourseMapper 完成查询。
 */
@Service
public class StudentCourseServiceImpl extends ServiceImpl<UserCourseMapper, UserCourse> implements IStudentCourseService {

    private static final Logger log = LoggerFactory.getLogger(StudentCourseServiceImpl.class);

    @Autowired
    private CourseMapper courseMapper;

    @Override
    @Transactional
    public Map<String, Object> joinCourseByCode(String joinCode, Long studentId) {
        // 1. 根据加课码查询课程
        Course course = courseMapper.selectOne(
            new LambdaQueryWrapper<Course>().eq(Course::getJoinCode, joinCode)
        );
        if (course == null) {
            throw new RuntimeException("加课码无效或课程不存在");
        }

        // 2. 检查是否已加入：幂等设计 —— 重复点击加课码视作成功，
        //    直接返回课程信息，避免前端拿到 500 误以为系统故障。
        //    关键：必须限定 role=2（学生身份），否则会把"教师建课时写入的 role=1 关联"
        //    误判成"学生已加入"，导致 list 接口查不到任何数据。
        UserCourse existingRelation = this.getOne(
            new LambdaQueryWrapper<UserCourse>()
                .eq(UserCourse::getUserId, studentId)
                .eq(UserCourse::getCourseId, course.getId())
                .eq(UserCourse::getRole, 2)
        );
        if (existingRelation != null) {
            Map<String, Object> alreadyJoined = new LinkedHashMap<>();
            alreadyJoined.put("courseId", course.getId());
            alreadyJoined.put("courseName", course.getCourseName());
            alreadyJoined.put("alreadyJoined", true);
            return alreadyJoined;
        }

        // 3. 建立关联（role=2 学生）
        UserCourse uc = new UserCourse();
        uc.setUserId(studentId);
        uc.setCourseId(course.getId());
        uc.setRole(2);
        uc.setIsTop(0);
        uc.setSortWeight(0);
        this.save(uc);

        // 4. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseId", course.getId());
        result.put("courseName", course.getCourseName());
        result.put("alreadyJoined", false);
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentCourseGroupedList(Long studentId) {
        // 【DEBUG日志】打印入参 studentId，确认后端收到的是什么值
        log.info("[学生课程列表] 入参 studentId={}, 精确值={}", studentId, studentId);

        // 1. 查出该学生作为"正式学生(role=2)"的所有 user_course 关联记录
        List<UserCourse> userCourseList = this.list(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, studentId)
                        .eq(UserCourse::getRole, 2));

        // 2. 若没有任何关联，直接返回空列表
        if (userCourseList == null || userCourseList.isEmpty()) {
            log.info("[学生课程列表] user_course 表中该学生没有关联记录，返回空列表");
            return new ArrayList<>();
        }

        log.info("[学生课程列表] 查询到 {} 条 user_course 关联记录", userCourseList.size());

        // 3. 把所有课程ID收集起来，用于一次性查出课程详情
        List<Long> courseIdList = new ArrayList<>();
        for (UserCourse uc : userCourseList) {
            courseIdList.add(uc.getCourseId());
        }

        // 4. 一次性查出这些课程的基本信息
        List<Course> courseList = courseMapper.selectBatchIds(courseIdList);

        // 5. 把课程按 ID 放进 Map，便于按 user_course 顺序快速取详情
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course course : courseList) {
            courseMap.put(course.getId(), course);
        }

        // 6. 准备一个"学期 -> 该学期的卡片列表"的中间容器（保持原始顺序）
        Map<String, List<Map<String, Object>>> groupedCourses = new LinkedHashMap<>();

        // 7. 按 user_course 的顺序遍历，组装每个课程的卡片数据
        for (UserCourse uc : userCourseList) {
            Course course = courseMap.get(uc.getCourseId());
            if (course == null) {
                continue;
            }

            // 组装单张课程卡片
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", course.getId());
            card.put("courseNum", course.getCourseNum());
            card.put("courseName", course.getCourseName());
            card.put("className", course.getClassName());
            card.put("cover", course.getCover());
            // creatorId 即该课程的任课教师ID；等 Auth 模块开放按 ID 查用户名的接口后，
            // 在此处把 creatorId 换成真实 teacherName 即可（只改这一个文件）。
            card.put("creatorId", course.getCreatorId());
            card.put("teacherName", null);
            card.put("isTop", uc.getIsTop());
            card.put("sortWeight", uc.getSortWeight());

            // 用 term 作为分组键；若学期为空则统一归到"未分学期"
            String term = course.getTerm();
            if (term == null || term.trim().isEmpty()) {
                term = "未分学期";
            }

            // 把卡片塞进对应学期的列表里
            List<Map<String, Object>> termCourseList = groupedCourses.get(term);
            if (termCourseList == null) {
                termCourseList = new ArrayList<>();
                groupedCourses.put(term, termCourseList);
            }
            termCourseList.add(card);
        }

        // 8. 把分组结果转换为前端需要的格式：[{ term, courses: [...] }]
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedCourses.entrySet()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("term", entry.getKey());
            group.put("courses", entry.getValue());
            result.add(group);
        }
        return result;
    }
}
