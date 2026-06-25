package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.User;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户/认证业务层接口
 */
public interface IAuthService extends IService<User> {

    /**
     * 账号密码登录
     * @param account 账号
     * @param password 密码（明文，会与 BCrypt 加密串比对）
     * @return 登录成功返回用户信息（含 token），失败抛 RuntimeException
     */
    Map<String, Object> login(String account, String password);

    /**
     * 用户注册
     * @param account 账号（学号/工号）
     * @param password 密码（明文，方法内会用 BCrypt 加密后存储）
     * @param name 真实姓名
     * @param phone 手机号（可选）
     * @return 注册成功返回新用户 ID，失败抛 RuntimeException
     */
    Long register(String account, String password, String name, String phone);
}
