package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.HomeworkMapper;
import com.coursemanager.mapper.HomeworkSubmitMapper;
import com.coursemanager.pojo.Homework;
import com.coursemanager.pojo.HomeworkSubmit;
import com.coursemanager.service.IHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业业务层实现类（教师端）
 */
@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements IHomeworkService {

    @Autowired
    private HomeworkSubmitMapper homeworkSubmitMapper;

    @Override
    @Transactional
    public Long createHomework(Homework homework) {
        // TODO(teacher-dev): 发布作业时可能需要批量插入待提交记录
        this.save(homework);
        return homework.getId();
    }

    @Override
    public List<Map<String, Object>> getTeacherHomeworkOverview(Long courseId, Long teacherId) {
        // TODO(teacher-dev): 实现教师作业总览列表查询
        // 提示：查询 homework 表，关联 homework_submit 统计各状态数量
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getSubmitList(Long homeworkId, Integer status, String studentName) {
        // TODO(teacher-dev): 实现批阅大厅提交明细查询
        // 提示：查询 homework_submit 表，关联 user 表，筛选条件支持 status 和 studentName
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void gradeHomework(Long submitId, Integer score, String teacherComment, Integer status) {
        // TODO(teacher-dev): 实现教师批阅打分
        // 提示：更新 homework_submit 表中对应 submitId 的 score、teacher_comment、status 字段
    }
}
