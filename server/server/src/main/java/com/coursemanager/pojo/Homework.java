package com.coursemanager.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.Date;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业发布主表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("homework")
public class Homework extends BaseBean<Homework> {

    @Serial
    private static final long serialVersionUID = 3234567890123456789L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 所属课程主键ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 作业标题
     */
    @TableField("title")
    private String title;

    /**
     * 作业要求描述
     */
    @TableField("description")
    private String description;

    /**
     * 作业模式：1=个人作业，2=小组作业
     */
    @TableField("type")
    private Integer type;

    /**
     * 应用环节标签（如：课前/课中/课后/期末）
     */
    @TableField("activity_tag")
    private String activityTag;

    /**
     * 本次作业满分值
     */
    @TableField("total_score")
    private Integer totalScore;

    /**
     * 超时提交策略：0=允许超时，1=超时禁止提交
     */
    @TableField("forbid_late")
    private Integer forbidLate;

    /**
     * 老师上传的附件列表（JSON数组存储）
     */
    @TableField("attachments")
    private String attachments;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 严格截止交作业时间
     */
    @TableField("deadline")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;
}
