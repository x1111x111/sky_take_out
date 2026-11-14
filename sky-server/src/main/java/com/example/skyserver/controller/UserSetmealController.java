package com.example.skyserver.controller;

import com.example.skyserver.entity.Setmeal;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.SetmealService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端套餐接口")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/user/setmeal")
@RequiredArgsConstructor
public class UserSetmealController {

    private final SetmealService setmealService;

    /**
     * 用户端：根据分类查询起售套餐
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(
            @RequestParam Long categoryId) {

        List<Setmeal> list =
                setmealService.listByCategoryId(categoryId);

        return Result.success(list);
    }
}