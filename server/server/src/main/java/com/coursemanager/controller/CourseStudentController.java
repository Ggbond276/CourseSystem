package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程中心模块 - 学生控制台
 * 包含凭加课码加入课程、查看已加入课程列表等接口
 */
@RestController
@RequestMapping("/course/student")
@RequiredArgsConstructor
public class CourseStudentController {

    /**
     * 凭唯一加课码申请加入课堂
     * POST /course/student/join
     * 请求体：{ joinCode: "9B3299" }
     * 响应：{ code, msg, data: { courseId, courseName } }
     */
    @PostMapping("/join")
    public CommonResult<?> join(@RequestBody JoinCourseRequest request) {
        // TODO(student-dev): 实现学生加入课程逻辑
        // 1. 根据 joinCode 从 course 表查询课程（若不存在抛出异常）
        // 2. 检查 user_course 表中是否已存在该用户与课程的关联（防止重复加入）
        // 3. 在 user_course 表中插入新记录（role=2 正式学生）
        // 4. 返回课程ID和课程名称
        return CommonResult.success("成功加入课程");
    }

    /**
     * 获取学生加入学习的课程卡片列表（支持学期分组展示）
     * GET /course/student/list
     * 响应：{ code, msg, data: [{ term: "学期名", courses: [...] }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list() {
        // TODO(student-dev): 实现学生课程列表查询
        // 1. 根据当前登录用户ID，从 user_course 表查询 role=2 的记录
        // 2. 关联 course 表获取课程详情
        // 3. 关联 user 表获取任课教师名称（teacherName）
        // 4. 按 term 学期分组返回
        return CommonResult.success("查询成功");
    }

    // ---------- 内部请求类 ----------

    public static class JoinCourseRequest {
        private String joinCode;

        public String getJoinCode() { return joinCode; }
        public void setJoinCode(String joinCode) { this.joinCode = joinCode; }
    }
}
