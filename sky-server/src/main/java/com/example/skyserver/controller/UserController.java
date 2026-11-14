package com.example.skyserver.controller;


import com.example.skyserver.dto.UserLoginDTO;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="用户相关接口")
@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;



    @PostMapping("/login")
    public Result<String> login(
            @Valid @RequestBody UserLoginDTO dto){


        String token =
                userService.login(dto);


        return Result.success(token);

    }

}