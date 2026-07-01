package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.HomeworkMapper;
import com.coursemanager.mapper.HomeworkSubmitMapper;
import com.coursemanager.pojo.Homework;
import com.coursemanager.pojo.HomeworkSubmit;
import com.coursemanager.service.IStudentHomeworkService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * 学生作业列表查询实现思路：
     *   1) 先查 homework 表里该 courseId 的所有作业（按 publish_time 倒序）
     *   2) 批量查出这些作业对应的、该学生的所有 homework_submit 记录
     *   3) 在内存里按 homeworkId 映射成 Map，循环组装列表
     *   4) 没提交过的作业 → status = 0、score = null
     */
    @Override
    public List<Map<String, Object>> getStudentHomeworkList(Long courseId, Long studentId) {
        // 1. 防御：参数为空直接返回空集合
        if (courseId == null || studentId == null) {
            return new ArrayList<>();
        }

        // 2. 复用 HomeworkMapper 的条件查询，先把该课程下所有作业捞出来
        //    这里遵守白名单约定：复用 B1 的 selectCondition，不动它已有的 SQL
        com.coursemanager.lang.HomeworkCondition courseCondition =
                new com.coursemanager.lang.HomeworkCondition().setCourseId(courseId);
        List<Homework> homeworkList = baseMapper.selectCondition(courseCondition);
        if (homeworkList == null || homeworkList.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 把作业 ID 都收集起来，一次性查该学生的提交记录（避免 N+1）
        List<Long> homeworkIdList = new ArrayList<>();
        for (Homework homework : homeworkList) {
            homeworkIdList.add(homework.getId());
        }
        List<HomeworkSubmit> submitList =
                homeworkSubmitMapper.selectByHomeworkIdListAndStudentId(homeworkIdList, studentId);

        // 4. 把提交记录按 homeworkId 装进 Map，方便后续 O(1) 查找
        Map<Long, HomeworkSubmit> submitMapByHomeworkId = new HashMap<>();
        if (submitList != null) {
            for (HomeworkSubmit submit : submitList) {
                submitMapByHomeworkId.put(submit.getHomeworkId(), submit);
            }
        }

        // 5. 顺序遍历作业列表，把作业信息 + 我的提交状态拼成响应项
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Homework homework : homeworkList) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("homeworkId", homework.getId());
            itemMap.put("title", homework.getTitle());
            itemMap.put("activityTag", homework.getActivityTag());
            itemMap.put("deadline", homework.getDeadline());

            // 5.1 拿该作业的提交记录；没交过则 status=0、score=null
            HomeworkSubmit submit = submitMapByHomeworkId.get(homework.getId());
            if (submit != null) {
                itemMap.put("status", submit.getStatus());
                itemMap.put("score", submit.getScore());
            } else {
                itemMap.put("status", 0);
                itemMap.put("score", null);
            }

            resultList.add(itemMap);
        }
        return resultList;
    }

    /**
     * 学生作业详情查询实现思路：
     *   1) 根据 homeworkId 查询作业基本信息（baseMapper.selectById）
     *   2) 根据 homeworkId + studentId 查询该学生的提交记录（homeworkSubmitMapper.selectByHomeworkIdAndStudentId）
     *   3) 作业的 attachments 字段是 JSON 字符串，需要解析为对象数组
     *   4) 组装 mySubmission 对象：
     *      - 没提交过 → mySubmission = null
     *      - 提交过 → 把提交记录的所有字段组装进去
     */
    @Override
    public Map<String, Object> getStudentHomeworkDetail(Long homeworkId, Long studentId) {
        // 1. 参数防御检查
        if (homeworkId == null || studentId == null) {
            return null;
        }

        // 2. 查询作业基本信息
        Homework homework = baseMapper.selectById(homeworkId);
        if (homework == null) {
            return null;
        }

        // 3. 查询该学生的提交记录
        HomeworkSubmit submit = homeworkSubmitMapper.selectByHomeworkIdAndStudentId(homeworkId, studentId);

        // 4. 组装返回结果
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("homeworkId", homework.getId());
        resultMap.put("title", homework.getTitle());
        resultMap.put("description", homework.getDescription());
        resultMap.put("deadline", homework.getDeadline());
        resultMap.put("totalScore", homework.getTotalScore());

        // 4.1 解析作业附件 JSON（可能为空或 null）
        List<Map<String, String>> homeworkAttachments = parseAttachments(homework.getAttachments());
        resultMap.put("attachments", homeworkAttachments);

        // 4.2 组装 mySubmission
        if (submit != null) {
            Map<String, Object> submissionMap = new LinkedHashMap<>();
            submissionMap.put("submitId", submit.getId());
            submissionMap.put("status", submit.getStatus());
            submissionMap.put("content", submit.getContent());
            // 4.3 解析学生提交的附件 JSON
            List<Map<String, String>> studentAttachments = parseAttachments(submit.getAttachments());
            submissionMap.put("attachments", studentAttachments);
            submissionMap.put("submitTime", submit.getSubmitTime());
            submissionMap.put("score", submit.getScore());
            submissionMap.put("teacherComment", submit.getTeacherComment());
            resultMap.put("mySubmission", submissionMap);
        } else {
            // 该学生还没提交过
            resultMap.put("mySubmission", null);
        }

        return resultMap;
    }

    /**
     * 解析 JSON 格式的附件字符串，返回对象列表
     * 如果解析失败或字符串为空/null，则返回空列表
     *
     * @param attachmentsJson JSON 字符串，例如：[{"name":"a.pdf","url":"..."}]
     * @return 附件对象列表
     */
    private List<Map<String, String>> parseAttachments(String attachmentsJson) {
        List<Map<String, String>> result = new ArrayList<>();
        if (attachmentsJson == null || attachmentsJson.trim().isEmpty()) {
            return result;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(attachmentsJson, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception parseException) {
            // JSON 解析失败时返回空列表，不影响整体业务
        }
        return result;
    }

    /**
     * 学生提交作业实现思路：
     *   1) 查询作业基本信息，检查作业是否存在，同时获取 deadline 和 forbidLate
     *   2) 截止检查：若 forbidLate=1 且当前时间已超过 deadline，抛出异常禁止提交
     *   3) 查询 homework_submit 表，看该学生是否已有提交记录
     *      - 已有 → 更新（覆盖式提交）：更新 content、attachments、submit_time、status=1
     *      - 没有 → 插入新记录：homework_id、student_id、content、attachments、submit_time、status=1
     */
    @Override
    @Transactional
    public void submitHomework(Long homeworkId, Long studentId, String content, String attachments) {
        // 1. 参数防御检查
        if (homeworkId == null || studentId == null) {
            throw new RuntimeException("作业ID和学生ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("提交内容不能为空");
        }

        // 2. 查询作业基本信息（顺便校验作业是否存在）
        Homework homework = baseMapper.selectById(homeworkId);
        if (homework == null) {
            throw new RuntimeException("作业不存在");
        }

        // 3. 截止检查：forbidLate=1 且当前时间超过 deadline 时禁止提交
        java.util.Date now = new java.util.Date();
        java.util.Date deadline = homework.getDeadline();
        if (deadline != null && homework.getForbidLate() != null && homework.getForbidLate() == 1) {
            if (now.after(deadline)) {
                throw new RuntimeException("作业已截止，禁止超时提交");
            }
        }

        // 4. 查询该学生是否已有提交记录
        HomeworkSubmit existingSubmit = homeworkSubmitMapper.selectByHomeworkIdAndStudentId(homeworkId, studentId);

        if (existingSubmit != null) {
            // 4.1 已有记录 → 覆盖式更新
            existingSubmit.setContent(content);
            existingSubmit.setAttachments(attachments);
            existingSubmit.setSubmitTime(now);
            existingSubmit.setStatus(1);  // 已提交待批阅
            homeworkSubmitMapper.updateById(existingSubmit);
        } else {
            // 4.2 没有记录 → 插入新记录
            HomeworkSubmit newSubmit = new HomeworkSubmit();
            newSubmit.setHomeworkId(homeworkId);
            newSubmit.setStudentId(studentId);
            newSubmit.setContent(content);
            newSubmit.setAttachments(attachments);
            newSubmit.setSubmitTime(now);
            newSubmit.setStatus(1);  // 已提交待批阅
            homeworkSubmitMapper.insert(newSubmit);
        }
    }
}
