package com.example.skyserver.controller;


import com.example.skyserver.entity.DishFlavor;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.DishFlavorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@SecurityRequirement(name = "token")
@Tag(name = "菜品口味相关接口")
@RestController
@RequestMapping("/admin/dishFlavor")
@RequiredArgsConstructor
public class DishFlavorController {


    private final DishFlavorService dishFlavorService;



    /**
     * 根据菜品查询口味
     */
    @GetMapping("/{dishId}")
    public Result<List<DishFlavor>> getByDishId(
            @PathVariable Long dishId){

        return Result.success(
                dishFlavorService.getByDishId(dishId)
        );

    }



    /**
     * 新增口味
     */
    @PostMapping
    public Result save(
            @RequestBody DishFlavor dishFlavor){

        dishFlavorService.save(dishFlavor);

        return Result.success();

    }



    /**
     * 删除口味
     */
    @DeleteMapping("/{id}")
    public Result delete(
            @PathVariable Long id){

        dishFlavorService.delete(id);

        return Result.success();

    }

}