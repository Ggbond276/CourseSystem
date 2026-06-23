package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.HomeworkMapper;
import com.coursemanager.mapper.HomeworkSubmitMapper;
import com.coursemanager.pojo.Homework;
import com.coursemanager.pojo.HomeworkSubmit;
import com.coursemanager.service.IStudentHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业业务层实现类（学生端）
 */
@Service
public class StudentHomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements IStudentHomeworkService {

    @Autowired
    private HomeworkSubmitMapper homeworkSubmitMapper;

    @Override
    public List<Map<String, Object>> getStudentHomeworkList(Long courseId, Long studentId) {
        // TODO(student-dev): 实现学生作业列表查询
        // 提示：查询 homework 表，关联 homework_submit 获取该学生的提交状态和得分
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getStudentHomeworkDetail(Long homeworkId, Long studentId) {
        // TODO(student-dev): 实现学生作业详情查询
        // 提示：查询 homework 表，关联 homework_submit 获取我的提交记录
        return new HashMap<>();
    }

    @Override
    @Transactional
    public void submitHomework(Long homeworkId, Long studentId, String content, String attachments) {
        // TODO(student-dev): 实现学生提交作业逻辑
        // 1. 检查是否已存在提交记录，存在则更新，不存在则插入
        // 2. 检查作业是否允许超时提交（forbid_late=1 时不允许）
        // 3. 更新 status=1（已提交待批阅），submit_time=当前时间
    }
}
