package com.example.skyserver.controller;


import com.example.skyserver.dto.DishDTO;
import com.example.skyserver.dto.DishPageQueryDTO;
import com.example.skyserver.dto.DishSaveDTO;
import com.example.skyserver.dto.DishUpdateDTO;
import com.example.skyserver.entity.Dish;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.DishService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "token")
@Tag(name="菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
public class DishController {


    private final DishService dishService;


    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(
            @ParameterObject DishPageQueryDTO dto
    ){

        PageResult<Dish> pageResult =
                dishService.pageQuery(dto);

        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> save(@RequestBody @Valid DishDTO dto) {
        dishService.save(dto);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody DishDTO dto) {

        dishService.update(dto);

        return Result.success();
    }

    @DeleteMapping
    public Result delete(Long id){

        dishService.delete(id);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result status(
            @PathVariable Integer status,
            Long id
    ){

        dishService.updateStatus(status,id);

        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<DishDTO> getById(
            @PathVariable Long id) {

        DishDTO dishDTO =
                dishService.getById(id);

        return Result.success(dishDTO);
    }
}