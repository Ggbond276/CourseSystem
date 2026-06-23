package com.coursemanager.lang;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业查询条件类
 */
@Data
@Accessors(chain = true)
public class HomeworkCondition {

    /**
     * 作业ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 作业标题（模糊查询）
     */
    private String title;

    /**
     * 作业模式：1=个人作业，2=小组作业
     */
    private Integer type;

    /**
     * 应用环节标签
     */
    private String activityTag;
}
