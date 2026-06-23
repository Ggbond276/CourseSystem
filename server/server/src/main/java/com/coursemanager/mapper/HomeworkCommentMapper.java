package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.HomeworkComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业评论持久层
 */
@Mapper
public interface HomeworkCommentMapper extends BaseMapper<HomeworkComment> {
}
