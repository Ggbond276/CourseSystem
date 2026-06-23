package com.coursemanager.dto;

import com.coursemanager.pojo.Course;
import com.coursemanager.utils.RandomData;
import lombok.Data;

import java.io.Serial;

/**
 * @author hhl
 * @date 2024/06/07 15:23
 * @description 课程新增传输类
 */
@Data
public class CourseAppendDto implements DtoTrans<Course> {

    @Serial
    private static final long serialVersionUID = -6980180427930408286L;

    private String courseName;

    private Integer period;

    private Double credit;

    private Integer status;

    public Course toModelCreate() {
        return this.toModel(Course.class, m ->
                m.setCourseNum(RandomData.generateCourseNum())
                .setCreateTime());
    }
}
