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

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户-课程多对多关联实体类
 *
 * 字段映射说明（以数据库实际结构为准）：
 * 实际数据库 user_course 表完整字段：
 *   id, user_id, course_id, role, is_top, sort_weight, join_time,
 *   create_time, update_time
 *
 * 注：虽然 schema.sql 中最初未包含 create_time / update_time，
 *     但运行时数据库已手动 ALTER 增加了这两列；
 *     因此本类继承 BaseBean，create_time / update_time 按正常列进行映射。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("user_course")
public class UserCourse extends BaseBean<UserCourse> {

    @Serial
    private static final long serialVersionUID = 2234567890123456789L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 课堂角色：1=任课教师，2=正式学生，3=助教
     */
    @TableField("role")
    private Integer role;

    /**
     * 个人层面的置顶状态：0=未置顶，1=已置顶
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 拖拽排序权重值，越大越靠前
     */
    @TableField("sort_weight")
    private Integer sortWeight;

    /**
     * 加入课堂时间
     */
    @TableField("join_time")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date joinTime;
}