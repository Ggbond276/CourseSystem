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
 * @description 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@TableName("user")
public class User extends BaseBean<User> {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    @TableId(type = IdType.ASSIGN_ID, value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 学号/工号/唯一登录账号
     */
    @TableField("account")
    private String account;

    /**
     * 加密后的密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("name")
    private String name;

    /**
     * 绑定的手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像托管URL
     */
    @TableField("avatar")
    private String avatar;
}
