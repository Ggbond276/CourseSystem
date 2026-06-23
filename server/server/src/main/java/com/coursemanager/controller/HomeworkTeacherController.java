package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业核心全生命周期模块 - 教师端
 * 包含发布作业、作业列表、批阅大厅、批阅打分等接口
 */
@RestController
@RequestMapping("/homework/teacher")
@RequiredArgsConstructor
public class HomeworkTeacherController {

    /**
     * 发布新作业/设置复杂策略
     * POST /homework/teacher/create
     * 请求体：{ courseId, title, description, type, activityTag, totalScore, forbidLate, deadline, attachments }
     * 响应：{ code, msg, data: { homeworkId } }
     */
    @PostMapping("/create")
    public CommonResult<?> create(@RequestBody CreateHomeworkRequest request) {
        // TODO(teacher-dev): 实现发布作业逻辑
        // 1. 参数校验（标题、截止时间必填）
        // 2. 将 attachments JSON 数组存入作业表 attachments 字段
        // 3. 插入 homework 表
        // 4. （可选）可同时向所有已加入该课程的学生批量插入 homework_submit 待提交记录
        return CommonResult.success("作业发布并分发完毕");
    }

    /**
     * 获取某门课下的所有作业总览列表（带实时数字统计）
     * GET /homework/teacher/list?courseId=1001
     * 响应：{ code, msg, data: [{ homeworkId, title, activityTag, deadline, isOver, gradedCount, ungradedCount, unsubmittedCount }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long courseId) {
        // TODO(teacher-dev): 实现教师作业列表查询
        // 1. 从 homework 表查询该 courseId 下的所有作业
        // 2. 对每条作业，统计 homework_submit 表中各状态的提交数量
        // 3. 判断是否已截止（deadline < 当前时间 -> isOver=true）
        return CommonResult.success("获取成功");
    }

    /**
     * 教师进入批阅大厅：获取某次作业的所有学生提交明细
     * GET /homework/teacher/submit-list?homeworkId=5001&status=1&studentName=
     * 响应：{ code, msg, data: [{ submitId, studentName, studentAccount, status, submitTime, similarity, wordCount, score }] }
     */
    @GetMapping("/submit-list")
    public CommonResult<?> submitList(
            @RequestParam Long homeworkId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String studentName) {
        // TODO(teacher-dev): 实现批阅大厅提交明细查询
        // 1. 查询 homework_submit 表中该 homeworkId 的记录
        // 2. 关联 user 表获取学生姓名和学号
        // 3. 支持 status 筛选和学生姓名模糊搜索
        return CommonResult.success("获取成功");
    }

    /**
     * 教师执行作业单人打分与评语留存
     * POST /homework/teacher/grade
     * 请求体：{ submitId, score, teacherComment, status }
     * 响应：{ code, msg, data: true }
     */
    @PostMapping("/grade")
    public CommonResult<?> grade(@RequestBody GradeHomeworkRequest request) {
        // TODO(teacher-dev): 实现教师批阅打分逻辑
        // 1. 更新 homework_submit 表中对应 submitId 的 score、teacher_comment、status 字段
        // 2. status=2 表示已批改，status=3 表示打回重做
        return CommonResult.success("批阅成功");
    }

    // ---------- 内部请求类 ----------

    public static class CreateHomeworkRequest {
        private Long courseId;
        private String title;
        private String description;
        private Integer type;
        private String activityTag;
        private Integer totalScore;
        private Integer forbidLate;
        private String deadline;
        private java.util.List<AttachmentItem> attachments;

        // getter/setter 略...
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public String getActivityTag() { return activityTag; }
        public void setActivityTag(String activityTag) { this.activityTag = activityTag; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Integer getForbidLate() { return forbidLate; }
        public void setForbidLate(Integer forbidLate) { this.forbidLate = forbidLate; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
        public java.util.List<AttachmentItem> getAttachments() { return attachments; }
        public void setAttachments(java.util.List<AttachmentItem> attachments) { this.attachments = attachments; }
    }

    public static class AttachmentItem {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class GradeHomeworkRequest {
        private Long submitId;
        private Integer score;
        private String teacherComment;
        private Integer status;

        public Long getSubmitId() { return submitId; }
        public void setSubmitId(Long submitId) { this.submitId = submitId; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getTeacherComment() { return teacherComment; }
        public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}
