package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.Homework;

import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业业务层接口（教师端）
 */
public interface IHomeworkService extends IService<Homework> {

    /**
     * 发布新作业
     * @param homework 作业实体
     * @return 发布成功返回作业ID
     */
    Long createHomework(Homework homework);

    /**
     * 获取某门课下的所有作业总览列表（带实时统计）
     * @param courseId 课程ID
     * @param teacherId 教师用户ID（用于权限校验）
     * @return 作业总览列表
     */
    List<Map<String, Object>> getTeacherHomeworkOverview(Long courseId, Long teacherId);

    /**
     * 获取某次作业的所有学生提交明细（教师批阅大厅）
     * @param homeworkId 作业ID
     * @param status 状态筛选（可选）
     * @param studentName 学生姓名模糊搜索（可选）
     * @return 提交明细列表
     */
    List<Map<String, Object>> getSubmitList(Long homeworkId, Integer status, String studentName);

    /**
     * 教师批阅打分
     * @param submitId 提交记录ID
     * @param score 得分
     * @param teacherComment 教师评语
     * @param status 批阅后状态（2=已批改，3=打回重做）
     */
    void gradeHomework(Long submitId, Integer score, String teacherComment, Integer status);
}
