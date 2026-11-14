package com.example.skyserver.config;

import com.example.skyserver.context.UserContext;
import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserLoginCheckInterceptor
        implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

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

        // 3. 检查用户角色
        Object role = claims.get(JwtUtil.CLAIM_ROLE);

        if (role == null) {
            throw new BusinessException("TOKEN_INVALID");
        }

        if (!JwtUtil.ROLE_USER.equals(role)) {
            throw new BusinessException("NO_PERMISSION");
        }

        // 4. 获取用户 ID
        Object id = claims.get("id");

        if (id == null) {
            throw new BusinessException("TOKEN_INVALID");
        }

        Long userId;

        try {
            userId = Long.valueOf(id.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException("TOKEN_INVALID");
        }

        // 5. 保存当前登录用户
        UserContext.setUserId(userId);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        UserContext.remove();
    }
}