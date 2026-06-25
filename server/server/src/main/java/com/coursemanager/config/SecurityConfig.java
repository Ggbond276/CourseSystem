package com.coursemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author 架构师（组长代建）
 * @date 2024/06/25
 * @description 关闭 Spring Security 默认行为
 *
 * 【为什么需要这个类】
 * 引入 spring-boot-starter-security 是为了拿到 BCryptPasswordEncoder 给密码加密，
 * 但 Spring Security 会自动给所有请求加一道过滤器链（CSRF、表单登录、HTTP Basic 等），
 * 导致前后端联调时被它拦在前面返回 401，根本到不了我们的 Controller。
 *
 * 【处理策略】
 * 关闭 CSRF、表单登录、HTTP Basic；所有请求一律 permitAll（放行）。
 * 真实的鉴权逻辑由我们手写的 JWT 拦截器承担，与 Spring Security 无关。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF 校验（前后端分离用 token 鉴权，不需要这个）
                .csrf().disable()
                // 关闭 Spring Security 自带的表单登录页
                .formLogin().disable()
                // 关闭 HTTP Basic 弹窗
                .httpBasic().disable()
                // 关闭 logout 过滤器（不需要它的默认 /logout）
                .logout().disable()
                // 所有 HTTP 请求一律放行
                .authorizeRequests()
                .anyRequest().permitAll();
        return http.build();
    }
}
