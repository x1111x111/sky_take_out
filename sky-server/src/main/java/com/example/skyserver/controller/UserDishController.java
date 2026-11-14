package com.example.skyserver.controller;

import com.example.skyserver.dto.DishDTO;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.DishService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端菜品接口")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/user/dish")
@RequiredArgsConstructor
public class UserDishController {

    private final DishService dishService;

    /**
     * 用户端：根据分类查询菜品
     */
    @GetMapping("/list")
    public Result<List<DishDTO>> list(
            @RequestParam Long categoryId) {

        List<DishDTO> list =
                dishService.listByCategoryId(categoryId);

        return Result.success(list);
    }
}