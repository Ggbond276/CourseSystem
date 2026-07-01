package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.HomeworkCommentMapper;
import com.coursemanager.pojo.HomeworkComment;
import com.coursemanager.service.IHomeworkCommentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业讨论区业务层实现类
 */
@Service
public class HomeworkCommentServiceImpl extends ServiceImpl<HomeworkCommentMapper, HomeworkComment>
        implements IHomeworkCommentService {

    /**
     * 作业评论列表查询实现思路：
     *   1) 参数防御检查
     *   2) 调用 HomeworkCommentMapper.selectCommentList 联表查询评论和用户信息
     *   3) 直接返回 List<Map>，字段已在 SQL 中处理完毕（匿名逻辑、userAvatar 等）
     */
    @Override
    public List<Map<String, Object>> getCommentList(Long homeworkId) {
        // 1. 参数防御检查
        if (homeworkId == null) {
            return new ArrayList<>();
        }
        // 2. 联表查询评论列表
        List<Map<String, Object>> commentList = baseMapper.selectCommentList(homeworkId);
        if (commentList == null) {
            return new ArrayList<>();
        }
        return commentList;
    }

    /**
     * 发表评论实现思路：
     *   1) 参数防御检查：homeworkId、userId、content 不能为空
     *   2) isAnonymous 为 null 时默认设为 0（实名发表）
     *   3) 插入 homework_comment 表，填充 createTime
     *   4) 返回评论主键 ID
     */
    @Override
    public Long addComment(Long homeworkId, Long userId, String content, Integer isAnonymous) {
        // 1. 参数防御检查
        if (homeworkId == null) {
            throw new RuntimeException("作业ID不能为空");
        }
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        // 2. isAnonymous 默认为 0（实名发表）
        if (isAnonymous == null) {
            isAnonymous = 0;
        }

        // 3. 构造评论实体并插入数据库
        HomeworkComment comment = new HomeworkComment();
        comment.setHomeworkId(homeworkId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setIsAnonymous(isAnonymous);
        comment.setCreateTime(new java.util.Date());

        // 4. 保存并返回评论 ID
        this.save(comment);
        return comment.getId();
    }
}
