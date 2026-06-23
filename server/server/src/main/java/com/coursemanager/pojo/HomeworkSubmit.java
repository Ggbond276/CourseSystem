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
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 学生作业提交与批阅记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("homework_submit")
public class HomeworkSubmit extends BaseBean<HomeworkSubmit> {

    @Serial
    private static final long serialVersionUID = 4234567890123456789L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 作业发布ID
     */
    @TableField("homework_id")
    private Long homeworkId;

    /**
     * 学生用户ID
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 状态：0=未交，1=已提交待批阅，2=老师已批改，3=打回重做
     */
    @TableField("status")
    private Integer status;

    /**
     * 学生填写的文本回答
     */
    @TableField("content")
    private String content;

    /**
     * 学生提交的附件集合（JSON格式）
     */
    @TableField("attachments")
    private String attachments;

    /**
     * 实际电子打卡提交时间
     */
    @TableField("submit_time")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /**
     * 教师给出的最终得分
     */
    @TableField("score")
    private Integer score;

    /**
     * 教师留下的评语
     */
    @TableField("teacher_comment")
    private String teacherComment;

    /**
     * 查重相似度百分比
     */
    @TableField("similarity")
    private BigDecimal similarity;

    /**
     * 自动统计的字数
     */
    @TableField("word_count")
    private Integer wordCount;
}
