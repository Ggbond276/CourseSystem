package com.coursemanager.service;

import com.coursemanager.pojo.Department;

import java.util.List;

/**
 * 学院字典 Service 接口
 */
public interface IDepartmentService extends com.baomidou.mybatisplus.extension.service.IService<Department> {

    /**
     * 查询所有学院（按 ID 升序），前端下拉用
     * @return 学院列表
     */
    List<Department> listAllSorted();
}