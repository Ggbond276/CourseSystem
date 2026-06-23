package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
     * 创建全新教学班级
     * POST /course/teacher/create
     * 请求体：{ courseNum, courseName, className, term, period, credit, cover }
     * 响应：{ code, msg, data: { courseId, joinCode } }
     */
    @PostMapping("/create")
    public CommonResult<?> create(@RequestBody CreateCourseRequest request) {
        // TODO(teacher-dev): 实现创建课程逻辑
        // 1. 参数校验（课程编号、名称、班级不能为空）
        // 2. 生成6位唯一加课码（joinCode）
        // 3. 插入 course 表，creator_id 为当前登录用户ID
        // 4. 在 user_course 表中建立创建者与课程的关联（role=1 任课教师）
        // 5. 返回课程ID和加课码
        return CommonResult.success("建课成功");
    }

    /**
     * 获取教师所教的课程大厅列表（支持学期分组展示）
     * GET /course/teacher/list
     * 响应：{ code, msg, data: [{ term: "学期名", courses: [...] }] }
     */
    @GetMapping("/list")
    public CommonResult<?> list() {
        // TODO(teacher-dev): 实现教师课程列表查询
        // 1. 根据当前登录用户ID，从 user_course 表查询 role=1 的记录
        // 2. 关联 course 表获取课程详情
        // 3. 关联 user_course 读取 is_top 和 sort_weight 字段
        // 4. 按 term 学期分组返回
        return CommonResult.success("查询成功");
    }

    /**
     * 课程卡片拖拽排序权重保存
     * PUT /course/teacher/sort
     * 请求体：{ sortedCourseIds: [1003, 1001, 1002] }
     * 响应：{ code, msg, data: true }
     */
    @PutMapping("/sort")
    public CommonResult<?> sort(@RequestBody SortCourseRequest request) {
        // TODO(teacher-dev): 实现课程排序保存
        // 1. 遍历 sortedCourseIds，按顺序更新每门课程的 sort_weight 字段
        // 2. sort_weight 值越大越靠前
        return CommonResult.success("排序权重保存成功");
    }

    /**
     * 单门课程切换置顶状态
     * PUT /course/teacher/top
     * 请求体：{ courseId: 1001, isTop: 1 }
     * 响应：{ code, msg, data: { courseId, isTop } }
     */
    @PutMapping("/top")
    public CommonResult<?> toggleTop(@RequestBody ToggleTopRequest request) {
        // TODO(teacher-dev): 实现置顶状态切换
        // 1. 更新 user_course 表中对应记录的 is_top 字段
        return CommonResult.success("操作成功");
    }

    /**
     * 获取课程详情与顶部面包屑面板数据
     * GET /course/teacher/detail/{courseId}
     * 响应：{ code, msg, data: { id, courseName, className, joinCode, memberCount, status } }
     */
    @GetMapping("/detail/{courseId}")
    public CommonResult<?> detail(@PathVariable Long courseId) {
        // TODO(teacher-dev): 实现课程详情查询
        // 1. 查询 course 表获取课程基本信息
        // 2. 统计 user_course 表中该课程的成员数量
        return CommonResult.success("获取成功");
    }

    // ---------- 内部请求类 ----------

    public static class CreateCourseRequest {
        private String courseNum;
        private String courseName;
        private String className;
        private String term;
        private Integer period;
        private Double credit;
        private String cover;

        public String getCourseNum() { return courseNum; }
        public void setCourseNum(String courseNum) { this.courseNum = courseNum; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public Integer getPeriod() { return period; }
        public void setPeriod(Integer period) { this.period = period; }
        public Double getCredit() { return credit; }
        public void setCredit(Double credit) { this.credit = credit; }
        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
    }

    public static class SortCourseRequest {
        private java.util.List<Long> sortedCourseIds;

        public java.util.List<Long> getSortedCourseIds() { return sortedCourseIds; }
        public void setSortedCourseIds(java.util.List<Long> sortedCourseIds) { this.sortedCourseIds = sortedCourseIds; }
    }

    public static class ToggleTopRequest {
        private Long courseId;
        private Integer isTop;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Integer getIsTop() { return isTop; }
        public void setIsTop(Integer isTop) { this.isTop = isTop; }
    }
}
