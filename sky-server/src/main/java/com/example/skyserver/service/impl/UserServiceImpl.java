package com.example.skyserver.service.impl;


import com.example.skyserver.dto.UserLoginDTO;
import com.example.skyserver.entity.User;
import com.example.skyserver.mapper.UserMapper;
import com.example.skyserver.service.UserService;
import com.example.skyserver.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserServiceImpl
        implements UserService {


    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;


    @Override
    public String login(UserLoginDTO dto) {

        // 1. 模拟微信返回 openid
        String openid = dto.getCode();

        // 2. 查询用户
        User user = userMapper.getByOpenid(openid);

        // 3. 第一次登录自动注册
        if (user == null) {

            user = new User();

            user.setOpenid(openid);

            user.setName("微信用户");

            userMapper.insert(user);
        }

        // 4. 生成 JWT 声明
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", user.getId());

        // 增加普通用户身份
        claims.put(JwtUtil.CLAIM_ROLE, JwtUtil.ROLE_USER);

        // 5. 生成 Token
        String token = jwtUtil.createJWT(claims);

        return token;
    }

}