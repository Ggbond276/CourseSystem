package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.Homework;

import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业业务层接口（学生端）
 */
public interface IStudentHomeworkService extends IService<Homework> {

    /**
     * 获取某门课下属于学生的作业任务列表
     * @param courseId 课程ID
     * @param studentId 学生用户ID
     * @return 作业任务列表
     */
    java.util.List<java.util.Map<String, Object>> getStudentHomeworkList(Long courseId, Long studentId);

    /**
     * 查看特定作业的详情及个人提交/批改记录
     * @param homeworkId 作业ID
     * @param studentId 学生用户ID
     * @return 作业详情（包含 mySubmission）
     */
    Map<String, Object> getStudentHomeworkDetail(Long homeworkId, Long studentId);

    /**
     * 学生提交作业
     * @param homeworkId 作业ID
     * @param studentId 学生用户ID
     * @param content 提交内容
     * @param attachments 附件 JSON 字符串
     */
    void submitHomework(Long homeworkId, Long studentId, String content, String attachments);
}
