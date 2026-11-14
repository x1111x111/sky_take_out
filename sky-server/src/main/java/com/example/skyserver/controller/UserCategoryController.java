package com.example.skyserver.controller;

import com.example.skyserver.entity.Category;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/category")
@RequiredArgsConstructor
public class UserCategoryController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam Integer type) {
        if (type != 1 && type != 2) return Result.error("分类类型不合法");
        return Result.success(categoryService.listByType(type));
    }
}
