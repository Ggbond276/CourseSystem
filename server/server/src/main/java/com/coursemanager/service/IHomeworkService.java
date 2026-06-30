package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.Homework;

import java.util.List;
import java.util.Map;

public interface IHomeworkService extends IService<Homework> {

    /**
     * 发布新作业
     *
     * @param homework         作业实体（由 Controller 组装后传入）
     * @param attachmentMapList 老师附件的 Map 列表（key 为 name、url），允许为 null
     *                          业务层负责把附件列表 JSON 化并存入 homework.attachments；
     *                          同时为该课程所有学生批量插入 status=0 的待提交记录。
     * @return 新建作业的主键 ID
     */
    Long createHomework(Homework homework, List<Map<String, String>> attachmentMapList);

    /**
     * 教师作业总览列表查询（接口 2：B1-B2 接口约定方法名 listByTeacher）
     * GET /homework/teacher/list?courseId=xxx
     *
     * @param courseId 课程主键
     * @return 作业总览列表，每项含 homeworkId/title/activityTag/deadline/isOver/gradedCount/ungradedCount/unsubmittedCount
     */
    List<Map<String, Object>> listByTeacher(Long courseId);

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
