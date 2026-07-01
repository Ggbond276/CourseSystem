package com.coursemanager.controller;

import com.coursemanager.service.IHomeworkCommentService;
import com.coursemanager.utils.CommonResult;
import com.coursemanager.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业详情讨论区互动模块
 * 包含获取评论列表、发表评论等接口
 */
@RestController
@RequestMapping("/homework/comment")
@RequiredArgsConstructor
public class HomeworkCommentController {

    private final IHomeworkCommentService homeworkCommentService;

    private final JwtUtil jwtUtil;

    /**
     * 获取当前作业详情页下的所有评论交流列表
     * GET /homework/comment/list?homeworkId=5001
     * 响应：{ code, msg, data: [{ commentId, userName, userAvatar, content, createTime }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long homeworkId) {
        // 1. 调用 Service 查询评论列表
        List<Map<String, Object>> commentList = homeworkCommentService.getCommentList(homeworkId);
        // 2. 返回结果
        return CommonResult.success(commentList, "加载成功");
    }

    /**
     * 在作业详情页下发表留言/讨论
     * POST /homework/comment/add
     * 请求体：{ homeworkId, content, isAnonymous }
     * 响应：{ code, msg, data: { commentId, createTime } }
     */
    @PostMapping("/add")
    public CommonResult<?> add(@RequestBody AddCommentRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 1. 解析当前登录用户ID
        Long currentUserId = resolveCurrentUserId(authorizationHeader);

        // 2. 调用 Service 发表评论（Service 内部会抛出 RuntimeException，被 WebExceptionHandler 捕获返回 500）
        Long commentId = homeworkCommentService.addComment(
                request.getHomeworkId(),
                currentUserId,
                request.getContent(),
                request.getIsAnonymous()
        );

        // 3. 组装成功响应数据：{ commentId, createTime }
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("commentId", commentId);
        resultData.put("createTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // 4. 返回成功响应
        return CommonResult.success(resultData, "发表评论成功");
    }

    /**
     * 解析当前请求的用户ID
     * 优先级：
     *   1) Authorization 头里的 JWT token 解析得到 userId
     *   2) 测试期默认值 DEFAULT_USER_ID（Postman 不带 token 的场景）
     */
    private static final Long DEFAULT_USER_ID = 1856321478523690001L;

    private Long resolveCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length()).trim();
            if (!token.isEmpty()) {
                try {
                    Long userIdFromToken = jwtUtil.parseUserId(token);
                    if (userIdFromToken != null) {
                        return userIdFromToken;
                    }
                } catch (Exception parseFailure) {
                    // token 不合法、过期等情况下静默回落到兜底默认值
                }
            }
        }
        // 兜底默认值，方便 Postman 联调
        return DEFAULT_USER_ID;
    }

    // ---------- 内部请求类 ----------

    public static class AddCommentRequest {
        private Long homeworkId;
        private String content;
        private Integer isAnonymous;

        public Long getHomeworkId() { return homeworkId; }
        public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getIsAnonymous() { return isAnonymous; }
        public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    }
}
