package com.coursemanager.controller;

import com.coursemanager.pojo.Homework;
import com.coursemanager.service.IHomeworkService;
import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final IHomeworkService homeworkService;

    /**
     * 发布新作业/设置复杂策略
     * POST /homework/teacher/create
     * 请求体：{ courseId, title, description, type, activityTag, totalScore, forbidLate, deadline, attachments }
     * 响应：{ code, msg, data: { homeworkId } }
     */
    @PostMapping("/create")
    public CommonResult<?> create(@RequestBody CreateHomeworkRequest request) {
        // 1. 入参整体不能为空
        if (request == null) {
            return CommonResult.fail("请求体不能为空");
        }

        // 2. 必填字段：courseId 必填（API 文档约定 courseId 是 String 雪花 ID）
        if (request.getCourseId() == null || request.getCourseId().trim().length() == 0) {
            return CommonResult.fail("课程ID不能为空");
        }
        Long courseIdLong;
        try {
            courseIdLong = Long.parseLong(request.getCourseId().trim());
        } catch (Exception numberFormatError) {
            return CommonResult.fail("课程ID格式错误");
        }

        // 3. 必填字段：title 非空字符串
        if (request.getTitle() == null || request.getTitle().trim().length() == 0) {
            return CommonResult.fail("作业标题不能为空");
        }

        // 4. 必填字段：deadline 非空字符串
        if (request.getDeadline() == null || request.getDeadline().trim().length() == 0) {
            return CommonResult.fail("截止时间不能为空");
        }

        // 5. 解析 deadline 字符串为 Date 对象（格式：yyyy-MM-dd HH:mm:ss）
        Date deadlineDate;
        try {
            deadlineDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(request.getDeadline().trim());
        } catch (Exception parseError) {
            return CommonResult.fail("截止时间格式错误，正确格式为 yyyy-MM-dd HH:mm:ss");
        }

        // 6. 组装 Homework 实体
        Homework homework = new Homework();
        homework.setCourseId(courseIdLong);
        homework.setTitle(request.getTitle().trim());
        homework.setDescription(request.getDescription());
        homework.setType(request.getType());
        homework.setActivityTag(request.getActivityTag());
        homework.setTotalScore(request.getTotalScore());
        homework.setForbidLate(request.getForbidLate());
        homework.setDeadline(deadlineDate);
        homework.setPublishTime(new Date());

        // 7. 把 Controller 的附件内部类 AttachmentItem 翻译成 Map 列表，避免 Service 依赖 Controller 内部类
        List<Map<String, String>> attachmentMapList = new ArrayList<>();
        if (request.getAttachments() != null) {
            for (AttachmentItem item : request.getAttachments()) {
                if (item == null) {
                    continue;
                }
                Map<String, String> mapItem = new HashMap<>();
                mapItem.put("name", item.getName());
                mapItem.put("url", item.getUrl());
                attachmentMapList.add(mapItem);
            }
        }

        // 8. 调用 Service 完成作业发布 + 学生分发
        Long homeworkId = homeworkService.createHomework(homework, attachmentMapList);

        // 9. homeworkId 用 String 返回，符合文档"雪花 ID 必须加引号"的约定
        Map<String, Object> data = new HashMap<>();
        data.put("homeworkId", homeworkId == null ? null : homeworkId.toString());
        return CommonResult.success(data, "作业发布并分发完毕");
    }

    /**
     * 教师作业列表（带实时提交数量统计）
     * GET /homework/teacher/list?courseId=1856321478523691000
     * 响应：{ code, msg, data: [{ homeworkId, title, activityTag, deadline, isOver, gradedCount, ungradedCount, unsubmittedCount }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam Long courseId) {
        // 1. 必填校验：courseId 不能为空（兜底防御，正常 Spring 框架会自动报 400）
        if (courseId == null) {
            return CommonResult.fail("课程ID不能为空");
        }

        // 2. 调用 Service 拿数据（业务完全交给 Service）
        java.util.List<java.util.Map<String, Object>> dataList = homeworkService.listByTeacher(courseId);

        // 3. 返回结果，msg 与 B1_API.md 文档示例一致
        return CommonResult.success(dataList, "获取成功");
    }

    /**
     * 教师批阅大厅：获取某次作业的所有学生提交明细
     * GET /homework/teacher/submit-list?homeworkId=1856321478523692000&status=1&studentName=张
     * Query 参数：
     *   homeworkId  必填，作业主键
     *   status      可选，0=未交 / 1=已提交 / 2=已批改 / 3=打回重做
     *   studentName 可选，学生姓名模糊搜索
     * 响应：{ code, msg, data: [{ submitId, studentName, studentAccount, status, submitTime, similarity, wordCount, score }] }
     */
    @GetMapping("/submit-list")
    public CommonResult<?> submitList(
            @RequestParam Long homeworkId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String studentName) {
        // 1. 必填校验：homeworkId 不能为空
        if (homeworkId == null) {
            return CommonResult.fail("作业ID不能为空");
        }

        // 2. 调用 Service 拿数据（业务完全交给 Service）
        java.util.List<java.util.Map<String, Object>> dataList = homeworkService.getSubmitList(homeworkId, status, studentName);

        // 3. 返回结果，msg 与 B1_API.md 文档示例一致
        return CommonResult.success(dataList, "获取成功");
    }

    /**
     * 教师执行作业单人打分与评语留存（B1_API.md 接口 4 / liuyu.mdc B1-B2 约定写入三项字段）
     * POST /homework/teacher/grade
     *
     * 请求体（B1_API.md 第 4 节字段表）：
     *   submitId        String   必填   提交记录 ID（雪花算法字符串，必须加引号）
     *   score           Integer  必填   得分（范围 0 - homework.totalScore）
     *   teacherComment  String   选填   教师评语；不传或 null 一律视为空串
     *   status          Integer  必填   2=已批改（默认），3=打回重做
     *
     * 响应（B1_API.md 第 4 节成功响应示例）：
     *   { "code": 200, "msg": "批阅成功", "data": true }
     *
     * 落库：HomeworkSubmit.score / teacherComment / status（详见 liuyu.mdc 的 B1-B2 接口约定）
     */
    @PostMapping("/grade")
    public CommonResult<?> grade(@RequestBody GradeHomeworkRequest request) {
        // 1. 入参整体不能为空
        if (request == null) {
            return CommonResult.fail("请求体不能为空");
        }

        // 2. 必填校验：submitId 必填（API 文档约定 submitId 是 String 雪花 ID）
        if (request.getSubmitId() == null || request.getSubmitId().trim().length() == 0) {
            return CommonResult.fail("提交记录ID不能为空");
        }
        Long submitIdLong;
        try {
            submitIdLong = Long.parseLong(request.getSubmitId().trim());
        } catch (Exception numberFormatError) {
            return CommonResult.fail("提交记录ID格式错误");
        }

        // 3. 必填校验：score 必填，且必须 ≥ 0
        if (request.getScore() == null) {
            return CommonResult.fail("分数不能为空");
        }
        if (request.getScore() < 0) {
            return CommonResult.fail("分数不能为负数");
        }

        // 4. 必填校验：status 必填，仅允许 2=已批改 / 3=打回重做（严格对齐 B1_API.md 字段说明）
        if (request.getStatus() == null) {
            return CommonResult.fail("批阅状态不能为空");
        }
        if (request.getStatus() != 2 && request.getStatus() != 3) {
            return CommonResult.fail("批阅状态非法，仅支持 2=已批改 或 3=打回重做");
        }

        // 5. teacherComment 选填：把 null 标准化为空串，方便 Service 层写库（与文档"字段可省略"保持一致）
        String safeComment = request.getTeacherComment() == null ? "" : request.getTeacherComment();

        // 6. 调用 Service 完成批阅（业务全交给 Service：
        //    包括 status=1/3 才允许批改的幂等校验、score 不超过 totalScore 的上限校验、
        //    status=3 时清空 score 的状态机处理，以及 UPDATE 写库）
        homeworkService.gradeHomework(submitIdLong, request.getScore(), safeComment, request.getStatus());

        // 7. 返回布尔值 true 与 msg="批阅成功"，与 B1_API.md 接口 4 成功响应示例逐字一致
        return CommonResult.success(Boolean.TRUE, "批阅成功");
    }

    // ---------- 内部请求类 ----------

    public static class CreateHomeworkRequest {
        private String courseId;
        private String title;
        private String description;
        private Integer type;
        private String activityTag;
        private Integer totalScore;
        private Integer forbidLate;
        private String deadline;
        private java.util.List<AttachmentItem> attachments;

        // getter/setter 略...
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
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
        private String submitId;
        private Integer score;
        private String teacherComment;
        private Integer status;

        public String getSubmitId() { return submitId; }
        public void setSubmitId(String submitId) { this.submitId = submitId; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getTeacherComment() { return teacherComment; }
        public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}
