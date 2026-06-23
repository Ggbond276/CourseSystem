package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.dto.CourseAppendDto;
import com.coursemanager.dto.CourseChangeDto;
import com.coursemanager.lang.CourseCondition;
import com.coursemanager.lang.PageQuery;
import com.coursemanager.pojo.Course;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @author hhl
 * @date 2024/06/06 18:01
 * @description 课程业务层接口
 */
public interface ICourseService extends IService<Course> {

    /**
     * 分页查询课程列表
     * @param query 分页参数（pageNum, pageSize）
     * @param condition 查询条件
     * @return 分页后的课程列表
     */
    PageInfo<Course> getPage(PageQuery query, CourseCondition condition);

    /**
     * 新增课程
     * @param dto 课程新增参数（包含课程名称、学时、学分等）
     * @return 新增成功返回 true，失败返回 false
     */
    boolean appendCourse(CourseAppendDto dto);

    /**
     * 修改课程信息
     * @param dto 课程修改参数（包含课程ID及待修改字段）
     * @return 修改成功返回 true，失败返回 false
     */
    boolean changeCourse(CourseChangeDto dto);

    /**
     * 删除课程
     * @param id 课程ID
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteCourse(Long id);

    /**
     * 创建全新教学班级
     * @param course 课程实体
     * @param creatorId 创建者用户ID
     * @return 创建成功返回 true
     */
    boolean createCourse(Course course, Long creatorId);

    /**
     * 获取教师所教的课程列表（按学期分组）
     * @param teacherId 教师用户ID
     * @return 分组后的课程数据，格式：[{ term: "学期名", courses: [...] }]
     */
    List<Map<String, Object>> getTeacherCourseGroupedList(Long teacherId);

    /**
     * 课程卡片拖拽排序权重保存
     * @param teacherId 教师用户ID
     * @param sortedCourseIds 排好序的课程ID列表
     */
    void saveCourseSort(Long teacherId, List<Long> sortedCourseIds);

    /**
     * 单门课程切换置顶状态
     * @param teacherId 教师用户ID
     * @param courseId 课程ID
     * @param isTop 是否置顶（0=取消置顶，1=置顶）
     */
    void toggleCourseTop(Long teacherId, Long courseId, Integer isTop);

    /**
     * 获取课程详情（教师端）
     * @param courseId 课程ID
     * @param teacherId 教师用户ID（用于权限校验）
     * @return 课程详情
     */
    Map<String, Object> getTeacherCourseDetail(Long courseId, Long teacherId);
}
