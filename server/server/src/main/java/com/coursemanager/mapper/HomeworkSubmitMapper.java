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
     * 用于「发布作业」时查询该课程所有学生，为每人创建一条 status=0 的待提交记录
     *
     * @param courseId 课程主键
     * @return 学生 user_id 列表（没有学生时返回空集合）
     */
    java.util.List<Long> selectStudentIdListByCourseId(Long courseId);

    java.util.List<java.util.Map<String, Object>> selectSubmitListForTeacher(
            @org.apache.ibatis.annotations.Param("homeworkId") Long homeworkId,
            @org.apache.ibatis.annotations.Param("filterStatus") Integer filterStatus,
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

    /**
     * 学生工作台统计：拉取"已批改作业"的总分
     * 用在学生 dashboard 顶部统计卡片，避免前端遍历所有课程 N+1
     *
     * @param studentId 学生用户ID
     * @return 该学生已批改（status=2）作业的 score 总和（无任何记录返回 0）
     */
    Integer sumScoredScoreByStudentId(@org.apache.ibatis.annotations.Param("studentId") Long studentId);
}
