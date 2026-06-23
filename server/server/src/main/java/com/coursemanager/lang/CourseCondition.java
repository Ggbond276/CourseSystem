package com.coursemanager.lang;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author hhl
 * @date 2024/06/07 17:15
 * @description 课程动态查询条件类
 */
@Data
@Builder
public class CourseCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 7881680099400476483L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String courseNum;

    private String courseName;

    private Integer period;

    private Double credit;

    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
