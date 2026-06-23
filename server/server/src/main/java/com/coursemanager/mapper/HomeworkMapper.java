package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.HomeworkCondition;
import com.coursemanager.pojo.Homework;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业持久层
 */
@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    /**
     * 条件查询作业列表
     * @param condition 查询条件
     * @return 作业列表
     */
    java.util.List<Homework> selectCondition(HomeworkCondition condition);
}
