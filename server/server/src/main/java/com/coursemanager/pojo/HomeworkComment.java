package com.coursemanager.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.Date;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业评论/讨论实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("homework_comment")
public class HomeworkComment extends BaseBean<HomeworkComment> {

    @Serial
    private static final long serialVersionUID = 5234567890123456789L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 作业ID
     */
    @TableField("homework_id")
    private Long homeworkId;

    /**
     * 发表评论的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论正文
     */
    @TableField("content")
    private String content;

    /**
     * 是否匿名发表：0=实名，1=匿名
     */
    @TableField("is_anonymous")
    private Integer isAnonymous;

    /**
     * 发表时间
     */
    @TableField("create_time")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
