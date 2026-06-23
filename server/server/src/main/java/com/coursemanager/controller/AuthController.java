package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 认证与通行模块：处理账号登录等认证相关接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 账号密码登录接口
     * POST /auth/login
     * 请求体：{ account: "学号/工号", password: "密码" }
     * 响应：{ code, msg, data: { token, userId, name, avatar, phone } }
     */
    @PostMapping("/login")
    public CommonResult<?> login(@RequestBody LoginRequest request) {
        // TODO(zhuyihang): 实现登录逻辑
        // 1. 根据 account 从 user 表查询用户记录
        // 2. 使用 BCryptPasswordEncoder 验证密码
        // 3. 生成 JWT token（或使用其他 token 方案）
        // 4. 返回 token 和用户基本信息
        return CommonResult.success("登录成功");
    }

    /**
     * 登录请求参数内部类（占位）
     */
    public static class LoginRequest {
        private String account;
        private String password;

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
