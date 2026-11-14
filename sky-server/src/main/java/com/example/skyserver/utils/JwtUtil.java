package com.example.skyserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    // JWT 身份标识
    public static final String CLAIM_ROLE = "role";

    // 管理员角色
    public static final String ROLE_ADMIN = "ADMIN";

    // 用户角色
    public static final String ROLE_USER = "USER";

    @Value("${sky.jwt.admin-secret-key}")
    private String secretKey;

    @Value("${sky.jwt.admin-ttl}")
    private Long ttl;

    public String createJWT(Map<String, Object> claims) {

        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(key)
                .compact();
    }

    public Claims parseJWT(String token) {

        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}