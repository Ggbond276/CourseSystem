package com.coursemanager.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

/**
 * @author 架构师（组长代建）
 * @date 2024/06/25
 * @description JWT 令牌工具类：生成 token、解析 userId
 */
@Component
public class JwtUtil {

    /**
     * 32 位以上的密钥字符串。
     * 生产环境建议放 application.yml 的配置文件里。
     */
    private static final String SECRET_KEY = "CourseSystemSecretKeyForJwtToken20240625ZhuiBang";

    /**
     * 令牌过期时间：1 小时（单位：毫秒）
     */
    private static final long EXPIRE_TIME = 3600_000L;

    /**
     * 根据用户 ID 生成 JWT token
     *
     * @param userId 用户主键
     * @return JWT 字符串
     */
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 token 中解析出用户 ID（供鉴权拦截器使用）
     *
     * @param token JWT 字符串
     * @return 用户主键
     */
    public Long parseUserId(String token) {
        String subject = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(subject);
    }

    /**
     * 校验 token 是否过期
     *
     * @param token JWT 字符串
     * @return true=未过期，false=已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().after(new java.util.Date());
        } catch (Exception e) {
            return false;
        }
    }
}
