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

    @Override
    public List<Map<String, Object>> getCommentList(Long homeworkId) {
        // TODO(teacher-dev or student-dev): 实现评论列表查询
        // 提示：查询 homework_comment 表，关联 user 表，若 is_anonymous=1 则隐藏真实姓名
        return new ArrayList<>();
    }

    @Override
    public Long addComment(Long homeworkId, Long userId, String content, Integer isAnonymous) {
        // TODO(teacher-dev or student-dev): 实现发表评论
        // 提示：插入 homework_comment 表，返回评论ID
        HomeworkComment comment = new HomeworkComment();
        comment.setHomeworkId(homeworkId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setIsAnonymous(isAnonymous);
        this.save(comment);
        return comment.getId();
    }
}
