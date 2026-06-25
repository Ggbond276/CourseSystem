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
 * @date 2024/06/25
 * @description 学校实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("school")
public class School extends BaseBean<School> {

    @Serial
    private static final long serialVersionUID = 87654321987654321L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 学校名称
     */
    @TableField("name")
    private String name;

    /**
     * 学校代码
     */
    @TableField("code")
    private String code;

    /**
     * 所属地区
     */
    @TableField("region")
    private String region;
}
