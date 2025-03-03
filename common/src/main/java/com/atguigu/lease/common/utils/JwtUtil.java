package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    public static final SecretKey secretKey = Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyzabcdef".getBytes(StandardCharsets.UTF_8));

    /**
     * 创建一个JWT令牌
     *
     * @param id       用户ID，用于令牌中的claims部分
     * @param username 用户名，用于令牌中的claims部分
     * @return 生成的JWT令牌字符串
     */
    public static String createToken(Long id, String username) {
        // 使用Jwts.builder()创建JWT令牌构建器
        return Jwts.builder()
                // 设置令牌的主题为"LOGIN_USER"
                .setSubject("LOGIN_USER")
                // 设置令牌的过期时间为当前时间加上60分钟
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000L))
                // 添加自定义claims，包含用户ID和用户名
                .addClaims(Map.of("id", id, "username", username))
                // 使用HS256算法和秘密密钥对令牌进行签名
                .signWith(secretKey, SignatureAlgorithm.HS256)
                // 将令牌构建为紧凑的字符串形式并返回
                .compact();
    }

    /**
     * 解析JWT token并返回其中的claims信息
     *
     * @param token 待解析的JWT token字符串
     * @return 解析后的Claims对象，包含token中的各种信息
     * @throws LeaseException 当token为空、已过期或无效时抛出的自定义异常
     */
    public static Claims parseToken(String token) {
        // 检查token是否为空，如果为空则抛出异常，表示用户未登录或token未提供
        if (token == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        // 创建JwtParser对象，用于解析JWT token
        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();

        Claims claims;
        try {
            // 尝试解析token中的claims信息
            claims = jwtParser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // 如果token已过期，捕获异常并抛出自定义异常
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            // 如果token无效或解析失败，捕获异常并抛出自定义异常
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

        // 返回解析后的claims信息
        return claims;
    }

    public static void main(String[] args) {
        String adminToken = createToken(3L, "alice");
        System.out.println(adminToken);

        String appToken = createToken(2L, "18771803413");
        System.out.println(appToken);
    }
}
