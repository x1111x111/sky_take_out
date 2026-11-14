package com.example.skyserver.controller;


import com.example.skyserver.dto.CategorySaveDTO;
import com.example.skyserver.dto.CategoryUpdateDTO;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.skyserver.dto.CategoryPageQueryDTO;
import com.example.skyserver.entity.Category;
import com.example.skyserver.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="套餐相关接口")
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@SecurityRequirement(name = "token")
public class CategoryController {


    private final CategoryService categoryService;


    @PostMapping
    public Result save(
            @RequestBody
            @Valid CategorySaveDTO dto
    ){

        categoryService.save(dto);

        return Result.success();

    }

    @GetMapping("/page")
    public Result<PageResult<Category>> page(CategoryPageQueryDTO dto){

        PageResult<Category> pageResult =
                categoryService.pageQuery(dto);

        return Result.success(pageResult);
    }

    @PutMapping
    public Result update(
            @RequestBody @Valid CategoryUpdateDTO dto
    ){

        categoryService.update(dto);

        return Result.success();

    }

    @DeleteMapping
    public Result delete(Long id){

        categoryService.deleteById(id);

        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>> list(
            @RequestParam Integer type) {

        List<Category> categories =
                categoryService.listByType(type);

        return Result.success(categories);
    }
}