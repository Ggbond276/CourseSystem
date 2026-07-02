package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.DepartmentMapper;
import com.coursemanager.pojo.Department;
import com.coursemanager.service.IDepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学院字典 Service 实现
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

    @Override
    public List<Department> listAllSorted() {
        // 按 code 升序：CS / INFO / MATH / SE 这种代码序展示更整齐
        return this.list(
                new LambdaQueryWrapper<Department>()
                        .orderByAsc(Department::getCode)
        );
    }
}