package com.coursemanager.dto;

import com.coursemanager.pojo.Course;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;

/**
 * @author hhl
 * @date 2024/06/07 15:33
 * @description 课程修改传输类
 */
@Data
public class CourseChangeDto implements DtoTrans<Course> {

    @Serial
    private static final long serialVersionUID = 8715004453156462064L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "id不可为空")
    private Long id;

    private String courseName;

    private Integer period;

    private Double credit;

    private Integer status;

    public Course toModelUpdate() {
        return this.toModelDefaultUpdateTime(Course.class);
    }
}
