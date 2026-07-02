package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.TermMapper;
import com.coursemanager.pojo.Term;
import com.coursemanager.service.ITermService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学期字典 Service 实现
 */
@Service
public class TermServiceImpl extends ServiceImpl<TermMapper, Term> implements ITermService {

    @Override
    public List<Term> listAllSorted() {
        // 按学年倒序 + 学期正序：最新的学年在最前面，同一学年下"第一学期"在前
        return this.list(
                new LambdaQueryWrapper<Term>()
                        .orderByDesc(Term::getSchoolYear)
                        .orderByAsc(Term::getSemester)
        );
    }

    @Override
    public Term getCurrentTerm() {
        return this.getOne(
                new LambdaQueryWrapper<Term>()
                        .eq(Term::getIsCurrent, 1)
                        .last("LIMIT 1")
        );
    }
}