package com.example.skyserver.config;

import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginCheckInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        // 1. 获取 Token
        String token = request.getHeader("token");

        if (token == null || token.isBlank()) {
            throw new BusinessException("NOT_LOGIN");
        }

        // 2. 解析 Token
        Claims claims;

        try {
            claims = jwtUtil.parseJWT(token);
        } catch (Exception e) {
            throw new BusinessException("TOKEN_INVALID");
        }

        // 3. 检查角色
        Object role = claims.get(JwtUtil.CLAIM_ROLE);

        if (role == null) {
            throw new BusinessException("TOKEN_INVALID");
        }

        if (!JwtUtil.ROLE_ADMIN.equals(role)) {
            throw new BusinessException("NO_PERMISSION");
        }

        // 4. 检查管理员 ID
        Object empId = claims.get("empId");

        if (empId == null) {
            throw new BusinessException("TOKEN_INVALID");
        }

        try {
            Long.parseLong(empId.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException("TOKEN_INVALID");
        }

        return true;
    }
}