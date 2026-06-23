package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.lang.HomeworkSubmitCondition;
import com.coursemanager.pojo.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业提交记录持久层
 */
@Mapper
public interface HomeworkSubmitMapper extends BaseMapper<HomeworkSubmit> {

    /**
     * 条件查询作业提交列表
     * @param condition 查询条件
     * @return 提交记录列表
     */
    java.util.List<HomeworkSubmit> selectCondition(HomeworkSubmitCondition condition);
}
