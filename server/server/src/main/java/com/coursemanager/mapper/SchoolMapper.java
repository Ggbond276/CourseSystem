package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/25
 * @description 学校持久层
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
}
