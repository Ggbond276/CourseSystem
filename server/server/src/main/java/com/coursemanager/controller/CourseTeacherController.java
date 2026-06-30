package com.coursemanager.controller;

import com.coursemanager.dto.CourseAppendDto;
import com.coursemanager.pojo.Course;
import com.coursemanager.service.ICourseService;
import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 课程中心模块 - 教师控制台
 * 包含创建课程、课程列表、排序、置顶、详情等接口
 */
@RestController
@RequestMapping("/course/teacher")
@RequiredArgsConstructor
public class CourseTeacherController {

    /**
     * 测试期默认教师ID（对应 schema.sql 中的 T001）
     * 等 Auth 模块的 UserContext 工具类就绪后，本常量与所有兼容逻辑一并删除，
     * 改回从 UserContext.getCurrentUserId() 获取。
     */
    private static final Long DEFAULT_TEACHER_ID = 1856321478523690000L;

    private final ICourseService courseService;

    /**
     * 创建全新教学班级
     * POST /course/teacher/create
     * 请求体（CourseAppendDto）：{ teacherId?, courseNum, courseName, className, term, period, credit, cover }
     *   teacherId 可选：未传时使用测试期默认值（1756321478523690000L）
     * 响应：{ code, msg, data: { courseId, joinCode } }
     */
    @PostMapping("/create")
    public CommonResult<?> create(@RequestBody CourseAppendDto dto) {
        // 1. 兜底 teacherId：Postman 测试时若没传，使用测试教师 ID
        if (dto.getTeacherId() == null) {
            dto.setTeacherId(DEFAULT_TEACHER_ID);
        }
        // 2. 必填项兜底
        if (dto.getCourseName() == null || dto.getCourseName().trim().isEmpty()) {
            return CommonResult.fail("课程名称不能为空");
        }
        if (dto.getClassName() == null || dto.getClassName().trim().isEmpty()) {
            return CommonResult.fail("教学班级不能为空");
        }
        if (dto.getTerm() == null || dto.getTerm().trim().isEmpty()) {
            return CommonResult.fail("学年学期不能为空");
        }

        // 3. 把业务全交给 Service（使用 appendCourseAndReturn 拿到带 id 与 joinCode 的 Course）
        Course createdCourse = courseService.appendCourseAndReturn(dto);
        if (createdCourse == null) {
            return CommonResult.fail("建课失败");
        }

        // 4. 直接用 Service 返回的 Course 构造响应
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseId", createdCourse.getId());
        data.put("joinCode", createdCourse.getJoinCode());
        return CommonResult.success(data, "建课成功");
    }

    /**
     * 获取教师所教的课程大厅列表（支持学期分组展示）
     * GET /course/teacher/list
     * Query 可选：?teacherId=xxx（未传则使用测试期默认教师 ID）
     * 响应：{ code, msg, data: [{ term: "学期名", courses: [...] }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam(value = "teacherId", required = false) Long teacherId) {
        // 兜底：未传 teacherId 时使用测试默认值（Postman 不带参数也能跑通）
        if (teacherId == null) {
            teacherId = DEFAULT_TEACHER_ID;
        }
        List<Map<String, Object>> data = courseService.getTeacherCourseGroupedList(teacherId);
        return CommonResult.success(data, "查询成功");
    }

    /**
     * 课程卡片拖拽排序权重保存
     * PUT /course/teacher/sort
     * 请求体：{ teacherId?, sortedCourseIds: [..] }
     *   teacherId 可选：未传时使用测试期默认教师 ID
     * 响应：{ code, msg, data: true }
     */
    @PutMapping("/sort")
    public CommonResult<?> sort(@RequestBody SortCourseRequest request) {
        // teacherId 兜底
        if (request.getTeacherId() == null) {
            request.setTeacherId(DEFAULT_TEACHER_ID);
        }
        // 必填项兜底
        List<Long> sortedCourseIds = request.getSortedCourseIds();
        if (sortedCourseIds == null || sortedCourseIds.isEmpty()) {
            return CommonResult.fail("课程ID列表不能为空");
        }
        // 防御：雪花 ID 必须转 Long，转不动的直接当字符串全跳过
        List<Long> validIds = new java.util.ArrayList<>();
        for (Long id : sortedCourseIds) {
            if (id != null && id > 0) {
                validIds.add(id);
            }
        }
        if (validIds.isEmpty()) {
            return CommonResult.fail("有效的课程ID列表不能为空");
        }
        courseService.saveCourseSort(request.getTeacherId(), validIds);
        return CommonResult.success(Boolean.TRUE, "排序权重保存成功");
    }

    /**
     * 单门课程切换置顶状态
     * PUT /course/teacher/top
     * 请求体：{ teacherId?, courseId, isTop }
     *   teacherId 可选：未传时使用测试期默认教师 ID
     * 响应：{ code, msg, data: { courseId, isTop } }
     */
    @PutMapping("/top")
    public CommonResult<?> toggleTop(@RequestBody ToggleTopRequest request) {
        // teacherId 兜底
        if (request.getTeacherId() == null) {
            request.setTeacherId(DEFAULT_TEACHER_ID);
        }
        Long courseId = request.getCourseId();
        if (courseId == null) {
            return CommonResult.fail("课程ID不能为空");
        }
        Integer isTop = request.getIsTop();
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            return CommonResult.fail("置顶状态值只能为 0 或 1");
        }
        courseService.toggleCourseTop(request.getTeacherId(), courseId, isTop);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseId", courseId);
        data.put("isTop", isTop);
        return CommonResult.success(data, "操作成功");
    }

    /**
     * 获取课程详情与顶部面包屑面板数据
     * GET /course/teacher/detail/{courseId}
     * Query 可选：?teacherId=xxx（未传则使用测试期默认教师 ID）
     * 响应：{ code, msg, data: { id, courseName, className, joinCode, memberCount, status } }
     */
    @GetMapping("/detail/{courseId}")
    public CommonResult<?> detail(@PathVariable Long courseId,
                                  @RequestParam(value = "teacherId", required = false) Long teacherId) {
        // teacherId 兜底
        if (teacherId == null) {
            teacherId = DEFAULT_TEACHER_ID;
        }
        Map<String, Object> data = courseService.getTeacherCourseDetail(courseId, teacherId);
        return CommonResult.success(data, "获取成功");
    }

    // ==================== 内部请求类 ====================

    /**
     * 拖拽排序请求体
     */
    public static class SortCourseRequest {
        private Long teacherId;
        private List<Long> sortedCourseIds;

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public List<Long> getSortedCourseIds() { return sortedCourseIds; }
        public void setSortedCourseIds(List<Long> sortedCourseIds) { this.sortedCourseIds = sortedCourseIds; }
    }

    /**
     * 置顶切换请求体
     */
    public static class ToggleTopRequest {
        private Long teacherId;
        private Long courseId;
        private Integer isTop;

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Integer getIsTop() { return isTop; }
        public void setIsTop(Integer isTop) { this.isTop = isTop; }
    }
}