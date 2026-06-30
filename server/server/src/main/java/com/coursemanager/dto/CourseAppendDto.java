package com.coursemanager.dto;

import com.coursemanager.pojo.Course;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;

/**
 * @author 架构师
 * @date 2024/06/07 15:23
 * @description 课程新增传输类
 *
 * 字段说明：与前端"创建课程"表单一一对应。
 *   teacherId   教师ID（必填）
 *   courseNum   课程编号（必填）
 *   courseName  课程名称（必填）
 *   className   教学班级（必填）
 *   term        学年学期（必填）
 *   period      学时（必填）
 *   credit      学分（必填）
 *   cover       课程封面 URL（可选）
 */
@Data
public class CourseAppendDto implements DtoTrans<Course> {

    @Serial
    private static final long serialVersionUID = -6980180427930408286L;

    /**
     * 教师ID（创建者）
     */
    @NotNull(message = "教师ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teacherId;

    /**
     * 课程编号
     */
    @NotBlank(message = "课程编号不能为空")
    private String courseNum;

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    /**
     * 教学班级
     */
    @NotBlank(message = "教学班级不能为空")
    private String className;

    /**
     * 学年学期
     */
    @NotBlank(message = "学年学期不能为空")
    private String term;

    /**
     * 学时
     */
    @NotNull(message = "学时不能为空")
    private Integer period;

    /**
     * 学分
     */
    @NotNull(message = "学分不能为空")
    private Double credit;

    /**
     * 课程封面 URL（可选）
     */
    private String cover;

    /**
     * 把本 DTO 转成 Course 实体（创建场景）
     * 不在这里生成加课码（加课码由 Service 层统一生成，避免重复逻辑）
     * 注：DtoTrans 接口本身没有 toModelCreate 方法，因此不再加 @Override。
     */
    public Course toModelCreate() {
        return this.toModel(Course.class, m -> m.setCreateTime());
    }
}