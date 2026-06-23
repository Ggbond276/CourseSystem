package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.UserCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户-课程关联持久层
 */
@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {
}
