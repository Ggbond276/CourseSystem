package com.coursemanager.lang;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业提交记录查询条件类
 */
@Data
@Accessors(chain = true)
public class HomeworkSubmitCondition {

    /**
     * 提交记录ID
     */
    private Long id;

    /**
     * 作业ID
     */
    private Long homeworkId;

    /**
     * 学生用户ID
     */
    private Long studentId;

    /**
     * 状态：0=未交，1=已提交，2=已批改，3=打回重做
     */
    private Integer status;

    /**
     * 学生姓名（模糊查询）
     */
    private String studentName;

    /**
     * 提交时间范围-开始
     */
    private String startTime;

    /**
     * 提交时间范围-结束
     */
    private String endTime;
}
