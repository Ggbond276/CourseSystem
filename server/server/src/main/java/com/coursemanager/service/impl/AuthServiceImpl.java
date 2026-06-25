package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.UserMapper;
import com.coursemanager.pojo.User;
import com.coursemanager.service.IAuthService;
import com.coursemanager.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户/认证业务层实现类
 */
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements IAuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 账号密码登录
     * 1. 根据账号查用户
     * 2. BCrypt 比对明文密码
     * 3. 生成 JWT token
     * 4. 组装返回数据（含 token + schoolId）
     */
    @Override
    public Map<String, Object> login(String account, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("name", user.getName());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("schoolId", user.getSchoolId());

        return result;
    }

    /**
     * 用户注册
     * 1. 账号查重（已存在则抛异常）
     * 2. BCrypt 加密密码
     * 3. 雪花 ID 由 MyBatis-Plus 自动生成
     * 4. 插入 user 表（含 schoolId）
     */
    @Override
    public Long register(String account, String password, String name, String phone, Long schoolId) {
        LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(User::getAccount, account);
        Long existsCount = this.count(checkWrapper);
        if (existsCount > 0) {
            throw new RuntimeException("账号已存在，请换一个账号注册");
        }

        User newUser = new User();
        newUser.setAccount(account);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setName(name);
        newUser.setPhone(phone);
        newUser.setSchoolId(schoolId);

        this.save(newUser);
        return newUser.getId();
    }

    /**
     * 获取所有学校列表
     * TODO(student-dev): 实现学校列表查询，从 school 表读取
     */
    @Override
    public List<Map<String, Object>> listSchools() {
        // TODO(teacher-dev): 从 school 表查询所有学校
        // 示例返回：
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> school1 = new HashMap<>();
        school1.put("id", 1856321478523691000L);
        school1.put("name", "北京大学");
        school1.put("code", "PKU");
        list.add(school1);
        Map<String, Object> school2 = new HashMap<>();
        school2.put("id", 1856321478523691001L);
        school2.put("name", "清华大学");
        school2.put("code", "THU");
        list.add(school2);
        return list;
    }
}
