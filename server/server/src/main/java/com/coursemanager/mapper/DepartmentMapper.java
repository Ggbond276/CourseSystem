package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院字典 Mapper（仅暴露 MyBatis-Plus 自带 CRUD，不写自定义 SQL）
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}