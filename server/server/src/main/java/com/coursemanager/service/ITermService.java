package com.coursemanager.service;

import com.coursemanager.pojo.Term;

import java.util.List;

/**
 * 学期字典 Service 接口
 */
public interface ITermService extends com.baomidou.mybatisplus.extension.service.IService<Term> {

    /**
     * 按学年倒序查询所有学期（前端下拉用）
     * @return 学期列表（已按 school_year DESC, semester ASC 排序）
     */
    List<Term> listAllSorted();

    /**
     * 查询当前学期（is_current=1）
     * @return 当前学期，可能为 null（数据库没设默认学期时）
     */
    Term getCurrentTerm();
}