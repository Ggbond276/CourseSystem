package com.coursemanager.controller;

import com.coursemanager.service.IStudentCourseService;
import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/29
 * @description 课程中心模块 - 学生控制台
 * 包含凭加课码加入课程、查看已加入课程列表等接口
 */
@RestController
@RequestMapping("/course/student")
@RequiredArgsConstructor
public class CourseStudentController {

    /**
     * 测试期默认学生ID（对应 schema.sql 中的 S001）
     * 等 Auth 模块的 UserContext 工具类就绪后，本常量与所有兼容逻辑一并删除，
     * 改回从 UserContext.getCurrentUserId() 获取。
     */
    private static final Long DEFAULT_STUDENT_ID = 1856321478523690001L;

    private final IStudentCourseService studentCourseService;

    /**
     * 凭唯一加课码申请加入课堂
     * POST /course/student/join
     * 请求体：{ joinCode, studentId? }
     *   studentId 可选：未传时使用测试期默认学生 ID
     * 响应：{ code, msg, data: { courseId, courseName } }
     */
    @PostMapping("/join")
    public CommonResult<?> join(@RequestBody JoinCourseRequest request) {
        // 1. 必填校验：加课码不能为空
        String joinCode = request.getJoinCode();
        if (joinCode == null || joinCode.trim().isEmpty()) {
            return CommonResult.fail("加课码不能为空");
        }
        // 2. studentId 兜底（Postman 测试时未传也能跑通）
        if (request.getStudentId() == null) {
            request.setStudentId(DEFAULT_STUDENT_ID);
        }
        // 3. 调用 Service 层处理业务逻辑
        Map<String, Object> data = studentCourseService.joinCourseByCode(joinCode.trim(), request.getStudentId());
        return CommonResult.success(data, "成功加入课程");
    }

    /**
     * 获取学生加入学习的课程卡片列表（支持学期分组展示）
     * GET /course/student/list
     * Query 可选：?studentId=xxx（未传则使用测试期默认学生 ID）
     * 响应：{ code, msg, data: [{ term: "学期名", courses: [...] }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam(value = "studentId", required = false) Long studentId) {
        // 兜底
        if (studentId == null) {
            studentId = DEFAULT_STUDENT_ID;
        }
        List<Map<String, Object>> data = studentCourseService.getStudentCourseGroupedList(studentId);
        return CommonResult.success(data, "查询成功");
    }

    /**
     * 学生加入课程的请求体
     */
    public static class JoinCourseRequest {
        private String joinCode;
        private Long studentId;

        public String getJoinCode() { return joinCode; }
        public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
    }
}