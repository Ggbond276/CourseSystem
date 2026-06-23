package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.HomeworkComment;

import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业讨论区业务层接口
 */
public interface IHomeworkCommentService extends IService<HomeworkComment> {

    /**
     * 获取某作业下的所有评论列表
     * @param homeworkId 作业ID
     * @return 评论列表
     */
    List<Map<String, Object>> getCommentList(Long homeworkId);

    /**
     * 发表评论
     * @param homeworkId 作业ID
     * @param userId 评论者用户ID
     * @param content 评论内容
     * @param isAnonymous 是否匿名（0=实名，1=匿名）
     * @return 发表成功返回评论ID
     */
    Long addComment(Long homeworkId, Long userId, String content, Integer isAnonymous);
}
