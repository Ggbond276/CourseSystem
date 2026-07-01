package com.coursemanager.controller;

import com.coursemanager.service.IStudentHomeworkService;
import com.coursemanager.utils.CommonResult;
import com.coursemanager.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业核心生命周期模块 - 学生端
 * 包含获取作业列表、作业详情查看、提交作业等接口
 */
@RestController
@RequestMapping("/homework/student")
@RequiredArgsConstructor
public class HomeworkStudentController {

    /**
     * 测试期默认学生ID（对应 schema.sql 中的 S001）
     * 等 Auth 模块的 UserContext 工具类就绪后，本常量与所有兼容逻辑一并删除，
     * 改回从 UserContext.getCurrentUserId() 获取。
     */
    private static final Long DEFAULT_STUDENT_ID = 1856321478523690001L;

    private final IStudentHomeworkService studentHomeworkService;

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取某门课下属于学生的作业任务列表
     * GET /homework/student/list?courseId=1001&studentId=1856...
     * Headers（可选）：Authorization: Bearer xxx
     *   - 带 token 时：从 token 解析出 userId 作为 studentId
     *   - 不带 token 时：要求 query 必传 studentId，否则走测试期默认值 DEFAULT_STUDENT_ID（Postman 联调用）
     * 响应：{ code, msg, data: [{ homeworkId, title, activityTag, deadline, status, score }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long courseId,
                                @RequestParam(value = "studentId", required = false) Long studentId,
                                @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 1. 解析当前登录用户ID（三个优先级：token → query studentId → 测试默认值）
        Long currentUserId = resolveCurrentStudentId(studentId, authorizationHeader);

        // 2. 业务交给 Service
        List<Map<String, Object>> homeworkList = studentHomeworkService.getStudentHomeworkList(courseId, currentUserId);
        return CommonResult.success(homeworkList, "获取成功");
    }

    /**
     * 解析当前请求的学生用户ID
     * 优先级：
     *   1) Authorization 头里的 JWT token 解析得到 userId（最准确）
     *   2) query 参数中的 studentId（手动覆盖 / Postman 不带 token 的场景）
     *   3) 测试期默认值（兜底，方便 Postman 一键跑通）
     *
     * @param studentId           query 里手动传的 studentId
     * @param authorizationHeader 请求头里的 Authorization（格式 "Bearer xxx"）
     * @return 最终采用的学生用户ID
     */
    private Long resolveCurrentStudentId(Long studentId, String authorizationHeader) {
        // 1) 优先解析 token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length()).trim();
            if (!token.isEmpty()) {
                try {
                    Long userIdFromToken = jwtUtil.parseUserId(token);
                    if (userIdFromToken != null) {
                        return userIdFromToken;
                    }
                } catch (Exception parseFailure) {
                    // token 不合法、过期等情况下静默回落到下方兜底逻辑
                }
            }
        }
        // 2) query 里指定的 studentId
        if (studentId != null) {
            return studentId;
        }
        // 3) 兜底
        return DEFAULT_STUDENT_ID;
    }

    /**
     * 查看特定作业要求以及个人的提交/批改记录详情
     * GET /homework/student/detail?homeworkId=5001&studentId=...
     * 响应：{ code, msg, data: { homeworkId, title, description, deadline, totalScore, attachments, mySubmission: {...} } }
     */
    @GetMapping("/detail")
    public CommonResult<?> detail(@RequestParam Long homeworkId,
                                  @RequestParam(value = "studentId", required = false) Long studentId,
                                  @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 1. 解析当前登录用户ID
        Long currentUserId = resolveCurrentStudentId(studentId, authorizationHeader);

        // 2. 调用 Service 查询作业详情（包含我的提交记录）
        Map<String, Object> detailMap = studentHomeworkService.getStudentHomeworkDetail(homeworkId, currentUserId);

        // 3. 如果作业不存在，返回错误提示
        if (detailMap == null) {
            return CommonResult.error("作业不存在");
        }

        // 4. 正常返回
        return CommonResult.success(detailMap, "详情加载完毕");
    }

    /**
     * 学生上传附件并提交作业
     * POST /homework/student/submit
     * 请求体：{ homeworkId, content, attachments: [{ name, url }] }
     * 响应：{ code, msg, data: true }
     */
    @PostMapping("/submit")
    public CommonResult<?> submit(@RequestBody SubmitHomeworkRequest request,
                                  @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 1. 解析当前登录用户ID
        Long currentUserId = resolveCurrentStudentId(null, authorizationHeader);

        // 2. 将前端传来的附件列表序列化为 JSON 字符串，存入数据库
        String attachmentsJson = null;
        List<SubmitAttachmentItem> rawAttachments = request.getAttachments();
        if (rawAttachments != null && !rawAttachments.isEmpty()) {
            List<Map<String, String>> attachmentList = new ArrayList<>();
            for (SubmitAttachmentItem item : rawAttachments) {
                Map<String, String> attachmentMap = new HashMap<>();
                attachmentMap.put("name", item.getName());
                attachmentMap.put("url", item.getUrl());
                attachmentList.add(attachmentMap);
            }
            try {
                attachmentsJson = objectMapper.writeValueAsString(attachmentList);
            } catch (Exception serializationException) {
                // JSON 序列化失败时，忽略附件，不阻断提交
                attachmentsJson = null;
            }
        }

        // 3. 调用 Service 执行业务逻辑（Service 内部会抛出 RuntimeException，被 WebExceptionHandler 捕获返回 500）
        studentHomeworkService.submitHomework(
                request.getHomeworkId(),
                currentUserId,
                request.getContent(),
                attachmentsJson
        );

        // 4. 提交成功
        return CommonResult.success(true, "作业提交成功");
    }

    // ---------- 内部请求类 ----------

    public static class SubmitHomeworkRequest {
        private Long homeworkId;
        private String content;
        private java.util.List<SubmitAttachmentItem> attachments;

        public Long getHomeworkId() { return homeworkId; }
        public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public java.util.List<SubmitAttachmentItem> getAttachments() { return attachments; }
        public void setAttachments(java.util.List<SubmitAttachmentItem> attachments) { this.attachments = attachments; }
    }

    public static class SubmitAttachmentItem {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
