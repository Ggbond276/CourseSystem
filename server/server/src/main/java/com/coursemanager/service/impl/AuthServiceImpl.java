package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.UserMapper;
import com.coursemanager.pojo.User;
import com.coursemanager.service.IAuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户/认证业务层实现类
 */
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements IAuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String account, String password) {
        // 1. 根据账号查询用户记录
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. TODO: 生成 JWT token 并返回用户信息
        return user;
    }
}
