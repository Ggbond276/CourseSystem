package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取当前作业详情页下的所有评论交流列表
     * GET /homework/comment/list?homeworkId=5001
     * 响应：{ code, msg, data: [{ commentId, userName, userAvatar, content, createTime }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long homeworkId) {
        // TODO(teacher-dev or student-dev): 实现评论列表查询
        // 1. 查询 homework_comment 表中该 homeworkId 的所有记录
        // 2. 关联 user 表获取评论者姓名和头像
        // 3. 按 create_time 升序排列（时间正序，早的在前）
        // 4. 若 is_anonymous=1，则 userName 显示为"匿名用户"，不暴露真实姓名
        return CommonResult.success("加载成功");
    }

    /**
     * 在作业详情页下发表留言/讨论
     * POST /homework/comment/add
     * 请求体：{ homeworkId, content, isAnonymous }
     * 响应：{ code, msg, data: { commentId, createTime } }
     */
    @PostMapping("/add")
    public CommonResult<?> add(@RequestBody AddCommentRequest request) {
        // TODO(teacher-dev or student-dev): 实现发表评论逻辑
        // 1. 参数校验（homeworkId 和 content 不能为空）
        // 2. 插入 homework_comment 表
        // 3. 返回评论ID和发表时间
        return CommonResult.success("发表评论成功");
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
