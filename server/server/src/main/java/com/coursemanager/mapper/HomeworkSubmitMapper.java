package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.HomeworkSubmitCondition;
import com.coursemanager.pojo.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业提交记录持久层
 */
@Mapper
public interface HomeworkSubmitMapper extends BaseMapper<HomeworkSubmit> {

    /**
     * 条件查询作业提交列表
     * @param condition 查询条件
     * @return 提交记录列表
     */
    java.util.List<HomeworkSubmit> selectCondition(HomeworkSubmitCondition condition);

    /**
     * 查询某门课程的所有学生用户ID（role=2 的成员）
     * 用于「发布作业」时为每个学生创建一条 status=0 的待提交记录
     *
     * @param courseId 课程主键
     * @return 学生 user_id 列表（没有学生时返回空集合）
     */
    java.util.List<Long> selectStudentIdListByCourseId(Long courseId);

    /**
     * 批量插入「待提交」占位记录（status=0）
     * 教师发布作业后，为 course 下每个学生插入一条 HomeworkSubmit 记录
     * 通过 INSERT ... ON DUPLICATE KEY UPDATE 保证幂等（同 homework+student 唯一）
     *
     * @param homeworkId    新发布的作业ID
     * @param studentIdList 该课程所有学生的 userId 列表
     */
    void batchInsertPendingSubmit(@org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId,
                                  @org.apache.ibatis.annotations.Param("studentIdList") java.util.List<Long> studentIdList);

    /**
     * 教师批阅大厅：联表 user 拿到学生姓名/学号的提交明细
     * GET /homework/teacher/submit-list?homeworkId=xxx&status=1&studentName=张
     *
     * @param homeworkId  作业主键（必填）
     * @param status      状态筛选（可选；0/1/2/3）
     * @param studentName 学生姓名模糊搜索（可选；空或 null 时不筛）
     * @return List<Map> 每个 Map 对应 B1_API.md 接口3 文档中 data[] 的一项
     *         字段：submitId, studentName, studentAccount, status, submitTime, similarity, wordCount, score
     */
    java.util.List<java.util.Map<String, Object>> selectSubmitListForTeacher(
            @org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId,
            @org.apache.ibatis.annotations.Param("status") Integer status,
            @org.apache.ibatis.annotations.Param("studentName") String studentName);

    /**
     * 学生端：根据 作业ID + 学生ID 精准定位一条提交记录
     * 用于学生作业列表 / 详情场景，一次取一条，避免 in (...) 过度查询
     *
     * @param homeworkId 作业主键
     * @param studentId  学生用户ID
     * @return 该学生的提交记录（没有时返回 null）
     */
    HomeworkSubmit selectByHomeworkIdAndStudentId(@org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId,
                                                  @org.apache.ibatis.annotations.Param("studentId") Long studentId);

    /**
     * 学生端：批量根据 作业ID 列表 + 学生ID 取该学生在所有作业下的提交记录
     * 用于学生作业列表场景，一次 JOIN 取完所有 status/score，避免 N+1
     *
     * @param homeworkIdList 作业主键集合
     * @param studentId      学生用户ID
     * @return 提交记录列表（没交过的作业不会出现）
     */
    java.util.List<HomeworkSubmit> selectByHomeworkIdListAndStudentId(@org.apache.ibatis.annotations.Param("homeworkIdList") java.util.List<Long> homeworkIdList,
                                                                      @org.apache.ibatis.annotations.Param("studentId") Long studentId);
}
