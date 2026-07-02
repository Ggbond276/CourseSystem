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
 * 学院字典表 POJO
 *
 * 设计说明：
 * 学院名称属于"学校机构数据"，本实体只承载结构，
 * 不允许前端通过自由文本输入——必须通过下拉选择 department.id 引用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("department")
public class Department extends BaseBean<Department> {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 学院ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /** 所属学校ID（关联 school.id） */
    @TableField("school_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long schoolId;

    /** 学院名称（如 "计算机学院"） */
    @TableField("name")
    private String name;

    /** 学院代码（如 "CS"） */
    @TableField("code")
    private String code;
}