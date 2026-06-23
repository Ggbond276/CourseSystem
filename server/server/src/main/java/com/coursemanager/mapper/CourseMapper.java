package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.CourseCondition;
import com.coursemanager.pojo.Course;

import java.util.List;

/**
 * @author hhl
 * @date 2024/06/06 17:17
 * @description 课程持久层
 */
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 条件查询
     * @param condition 条件
     * @return list
     */
    List<Course> selectCondition(CourseCondition condition);

}
