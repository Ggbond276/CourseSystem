package com.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursemanager.pojo.User;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户/认证业务层接口
 */
public interface IAuthService extends IService<User> {

    /**
     * 账号密码登录
     * @param account 账号
     * @param password 密码
     * @return 登录成功返回用户信息（含 token），失败返回 null
     */
    User login(String account, String password);
}
