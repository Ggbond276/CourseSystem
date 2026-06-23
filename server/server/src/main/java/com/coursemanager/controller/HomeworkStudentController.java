package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
     * 获取某门课下属于学生的作业任务列表
     * GET /homework/student/list?courseId=1001
     * 响应：{ code, msg, data: [{ homeworkId, title, activityTag, deadline, status, score }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long courseId) {
        // TODO(student-dev): 实现学生作业列表查询
        // 1. 根据当前登录用户ID，从 user_course 表确认该学生已加入该课程
        // 2. 查询 homework 表中该 courseId 的所有作业
        // 3. 关联 homework_submit 表获取该学生的提交状态和得分
        // 4. 返回作业任务列表
        return CommonResult.success("获取成功");
    }

    /**
     * 查看特定作业要求以及个人的提交/批改记录详情
     * GET /homework/student/detail?homeworkId=5001
     * 响应：{ code, msg, data: { homeworkId, title, description, deadline, totalScore, attachments, mySubmission: {...} } }
     */
    @GetMapping("/detail")
    public CommonResult<?> detail(@RequestParam Long homeworkId) {
        // TODO(student-dev): 实现学生作业详情查询
        // 1. 查询 homework 表获取作业基本信息
        // 2. 查询 homework_submit 表获取该学生对此作业的提交记录
        // 3. 组装 mySubmission 对象（状态、内容、附件、得分、评语等）
        return CommonResult.success("详情加载完毕");
    }

    /**
     * 学生上传附件并提交作业
     * POST /homework/student/submit
     * 请求体：{ homeworkId, content, attachments: [{ name, url }] }
     * 响应：{ code, msg, data: true }
     */
    @PostMapping("/submit")
    public CommonResult<?> submit(@RequestBody SubmitHomeworkRequest request) {
        // TODO(student-dev): 实现学生提交作业逻辑
        // 1. 参数校验（homeworkId 和 content 不能为空）
        // 2. 检查作业是否已截止（若 forbid_late=1 则不允许超时提交）
        // 3. 查询 homework_submit 表，若已存在则更新（覆盖），不存在则插入
        // 4. 提交成功后 status 更新为 1（已提交待批阅）
        return CommonResult.success("作业提交成功");
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
