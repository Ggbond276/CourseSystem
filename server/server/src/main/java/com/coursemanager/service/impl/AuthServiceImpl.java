package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.mapper.SchoolMapper;
import com.coursemanager.mapper.UserMapper;
import com.coursemanager.pojo.School;
import com.coursemanager.pojo.User;
import com.coursemanager.service.IAuthService;
import com.coursemanager.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private SchoolMapper schoolMapper;

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
     * 3. 插入 user 表（含 schoolId）
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
     * 从 school 表查询所有学校，按 ID 升序排列
     */
    @Override
    public List<Map<String, Object>> listSchools() {
        List<School> schoolList = schoolMapper.selectList(null);
        return schoolList.stream().map(school -> {
            // 这里手动转 Map 而非直接返回 School 实体，
            // 是为了控制 JSON 输出字段。关键：id 必须以字符串形式输出，
            // 因为 Snowflake 19 位 Long 在 JS Number 上会丢精度，浏览器 JSON.parse
            // 会把 5 所学校的 id 全部截成同一个数字，导致下拉框只能选中最后一项。
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(school.getId()));
            map.put("name", school.getName());
            map.put("code", school.getCode());
            map.put("region", school.getRegion());
            return map;
        }).collect(Collectors.toList());
    }
}
