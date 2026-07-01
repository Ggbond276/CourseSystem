package com.coursemanager.controller;

import com.coursemanager.service.IStudentCourseService;
import com.coursemanager.utils.CommonResult;
import com.coursemanager.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CourseStudentController.class);

    private final IStudentCourseService studentCourseService;
    private final JwtUtil jwtUtil;

    /**
     * 从 Authorization header 中解析出当前登录用户 ID。
     * 关键安全设计：join/list 接口必须以 token 中的 userId 为准，
     * 即使前端传了 studentId 也只是兜底，避免前端 localStorage 残留教师 ID
     * 导致学生越权或加错身份。
     *
     * @param authHeader 浏览器自动带的 Authorization 头（"Bearer xxx"）
     * @return 当前登录用户 ID（解析失败则返回 null）
     */
    private Long resolveUserIdFromToken(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        String token = authHeader.trim();
        // 去掉 Bearer 前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length()).trim();
        }
        if (token.isEmpty()) {
            return null;
        }
        try {
            return jwtUtil.parseUserId(token);
        } catch (Exception e) {
            log.warn("[学生接口] token 解析失败，回退到前端传入的 studentId: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 凭唯一加课码申请加入课堂
     * POST /course/student/join
     * 请求体：{ joinCode, studentId? }
     *   studentId 可选；最终使用的 studentId 优先级：Authorization token > 请求体 > 默认测试 ID
     * 响应：{ code, msg, data: { courseId, courseName } }
     */
    @PostMapping("/join")
    public CommonResult<?> join(
            @RequestBody JoinCourseRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. 必填校验：加课码不能为空
        String joinCode = request.getJoinCode();
        if (joinCode == null || joinCode.trim().isEmpty()) {
            return CommonResult.fail("加课码不能为空");
        }
        // 2. studentId 兜底链：token > 请求体 > 测试期默认 ID
        Long finalStudentId = resolveUserIdFromToken(authHeader);
        if (finalStudentId == null) {
            finalStudentId = request.getStudentId();
        }
        if (finalStudentId == null) {
            finalStudentId = DEFAULT_STUDENT_ID;
        }
        // 3. 如果 token 与请求体不一致，打印告警日志（便于发现前端脏数据）
        if (request.getStudentId() != null && !request.getStudentId().equals(finalStudentId)) {
            log.warn("[学生加入课程] token 解析 userId={} 与请求体 studentId={} 不一致，以 token 为准",
                    finalStudentId, request.getStudentId());
        }
        // 4. 调用 Service 层处理业务逻辑
        Map<String, Object> data = studentCourseService.joinCourseByCode(joinCode.trim(), finalStudentId);
        return CommonResult.success(data, "成功加入课程");
    }

    /**
     * 获取学生加入学习的课程卡片列表（支持学期分组展示）
     * GET /course/student/list
     * Query 可选：?studentId=xxx；最终使用的 studentId 优先级：Authorization token > Query 参数 > 测试期默认 ID
     * 响应：{ code, msg, data: [{ term: "学期名", courses: [...] }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // studentId 兜底链：token > Query 参数 > 测试期默认 ID
        Long finalStudentId = resolveUserIdFromToken(authHeader);
        if (finalStudentId == null) {
            finalStudentId = studentId;
        }
        if (finalStudentId == null) {
            finalStudentId = DEFAULT_STUDENT_ID;
        }
        // 如果 token 与 Query 参数不一致，打印告警日志（便于发现前端脏数据）
        if (studentId != null && !studentId.equals(finalStudentId)) {
            log.warn("[学生课程列表] token 解析 userId={} 与 Query studentId={} 不一致，以 token 为准",
                    finalStudentId, studentId);
        }
        List<Map<String, Object>> data = studentCourseService.getStudentCourseGroupedList(finalStudentId);
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