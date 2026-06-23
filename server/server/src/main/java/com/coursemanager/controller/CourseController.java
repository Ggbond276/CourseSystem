package com.coursemanager.controller;

import com.coursemanager.dto.CourseAppendDto;
import com.coursemanager.dto.CourseChangeDto;
import com.coursemanager.lang.CourseCondition;
import com.coursemanager.lang.PageQuery;
import com.coursemanager.pojo.Course;
import com.coursemanager.service.ICourseService;
import com.coursemanager.utils.CommonResult;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author hhl
 * @date 2024/06/06 18:03
 * @description 课程应用层
 */
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;

    /**
     * 分页查询课程列表
     * GET /course
     * @param query 分页参数（pageNum, pageSize）
     * @param condition 查询条件
     * @return 分页后的课程列表
     */
    @GetMapping
    public CommonResult<PageInfo<Course>> getPage(PageQuery query, CourseCondition condition) {
        PageInfo<Course> pageInfo = this.courseService.getPage(query, condition);
        return CommonResult.success(pageInfo, "查询成功");
    }

    /**
     * 新增课程
     * POST /course
     * @param dto 课程新增参数
     * @return 操作结果
     */
    @PostMapping
    public CommonResult<Void> append(@RequestBody CourseAppendDto dto) {
        boolean success = this.courseService.appendCourse(dto);
        if (success) {
            return CommonResult.success("新增成功");
        } else {
            return CommonResult.fail("新增失败");
        }
    }

    /**
     * 修改课程信息
     * PUT /course
     * @param dto 课程修改参数
     * @return 操作结果
     */
    @PutMapping
    public CommonResult<Void> change(@RequestBody @Validated CourseChangeDto dto) {
        boolean success = this.courseService.changeCourse(dto);
        if (success) {
            return CommonResult.success("修改成功");
        } else {
            return CommonResult.fail("修改失败");
        }
    }

    /**
     * 删除课程
     * DELETE /course/{id}
     * @param id 课程ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        boolean success = this.courseService.deleteCourse(id);
        if (success) {
            return CommonResult.success("删除成功");
        } else {
            return CommonResult.fail("删除失败");
        }
    }
}
