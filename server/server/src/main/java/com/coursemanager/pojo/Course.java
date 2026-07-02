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

/**
 * @author hhl
 * @date 2024/06/06 17:27
 * @description 课程实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("course")
public class Course extends BaseBean<Course> {

    @Serial
    private static final long serialVersionUID = -9136510046337434102L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 课程编号
     */
    @TableField("course_num")
    private String courseNum;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 学时
     */
    @TableField("period")
    private Integer period;

    /**
     * 学分
     */
    @TableField("credit")
    private Double credit;

    /**
     * 课程状态（0：未启用；1：启用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 教学班级（如：201班）
     */
    @TableField("class_name")
    private String className;

    /**
     * 学年学期（如：2022-2023 第二学期）
     */
    @TableField("term")
    private String term;

    /**
     * 学期ID（关联 term.id），新建课程时通过下拉选择得到，存入此字段
     * 冗余字段 term（varchar）保留作为历史快照，便于历史数据回显
     */
    @TableField("term_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long termId;

    /**
     * 学院ID（关联 department.id），新建课程时通过下拉选择得到，存入此字段
     * 前端不允许再通过文本输入学院名
     */
    @TableField("department_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long departmentId;

    /**
     * 6位唯一加课码
     */
    @TableField("join_code")
    private String joinCode;

    /**
     * 课程封面背景图 URL
     */
    @TableField("cover")
    private String cover;

    /**
     * 课程大纲（JSON 数组，每项 {week, title, content, type}）
     * 由 CourseDetail.vue 的"课程信息"Tab 渲染，允许教师编辑
     */
    @TableField("syllabus")
    private String syllabus;

    /**
     * 课程介绍（长文本，纯文本即可）
     * 由 CourseDetail.vue 的"课程介绍"卡片渲染，允许教师编辑
     */
    @TableField("intro")
    private String intro;

    /**
     * 创建课堂的教师ID
     */
    @TableField("creator_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;

    /**
     * 授课教师姓名（建课时从 user.name 快照，避免后续教师改名导致历史课程显示变化）
     */
    @TableField("teacher_name")
    private String teacherName;

}
