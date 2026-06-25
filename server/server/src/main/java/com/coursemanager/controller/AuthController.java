package com.coursemanager.controller;

import com.coursemanager.service.IAuthService;
import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 认证与通行模块：处理账号登录、注册等认证相关接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 账号密码登录
     * POST /auth/login
     * 请求体：{ account: "学号/工号", password: "密码" }
     * 响应：{ code, msg, data: { token, userId, name, avatar, phone } }
     */
    @PostMapping("/login")
    public CommonResult<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> data = authService.login(request.getAccount(), request.getPassword());
        return CommonResult.success(data, "登录成功");
    }

    /**
     * 用户注册
     * POST /auth/register
     * 请求体：{ account, password, name, phone }
     * 响应：{ code, msg, data: { userId } }
     */
    @PostMapping("/register")
    public CommonResult<?> register(@RequestBody RegisterRequest request) {
        Long userId = authService.register(
                request.getAccount(),
                request.getPassword(),
                request.getName(),
                request.getPhone()
        );
        return CommonResult.success(Map.of("userId", userId), "注册成功");
    }

    // ---------- 内部请求类 ----------

    public static class LoginRequest {
        private String account;
        private String password;

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String account;
        private String password;
        private String name;
        private String phone;

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
