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
import java.time.LocalDate;

/**
 * 学年学期字典表 POJO
 *
 * 设计说明：
 * 学年学期属于"学院统一发布"的字典数据，本实体只承载结构，
 * 不允许前端通过自由文本输入——必须通过下拉选择 term.id 引用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("term")
public class Term extends BaseBean<Term> {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 学期ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /** 学年（如 "2024-2025"） */
    @TableField("school_year")
    private String schoolYear;

    /** 学期：1=第一学期 / 2=第二学期 */
    @TableField("semester")
    private Integer semester;

    /** 完整显示名（如 "2024-2025 第一学期"） */
    @TableField("display_name")
    private String displayName;

    /** 学期开始日期 */
    @TableField("start_date")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 学期结束日期 */
    @TableField("end_date")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 是否当前学期（0=否，1=是） */
    @TableField("is_current")
    private Integer isCurrent;
}