package com.example.skyserver.controller;

import com.example.skyserver.dto.SetmealDTO;
import com.example.skyserver.dto.SetmealPageQueryDTO;
import com.example.skyserver.entity.Setmeal;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.SetmealService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="套餐与菜品")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/admin/setmeal")
@RequiredArgsConstructor
public class SetmealController {

    private final SetmealService setmealService;

    @PostMapping
    public Result save(
            @RequestBody SetmealDTO dto) {

        setmealService.save(dto);

        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<Setmeal>> page(
            SetmealPageQueryDTO dto) {

        PageResult<Setmeal> pageResult =
                setmealService.pageQuery(dto);

        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result<SetmealDTO> getById(
            @PathVariable Long id) {

        SetmealDTO setmealDTO =
                setmealService.getById(id);

        return Result.success(setmealDTO);
    }
    @PutMapping
    public Result update(
            @RequestBody SetmealDTO dto) {

        setmealService.update(dto);

        return Result.success();
    }
    @PostMapping("/status/{status}")
    public Result updateStatus(
            @PathVariable Integer status,
            @RequestParam Long id) {

        setmealService.updateStatus(status, id);

        return Result.success();
    }
    @DeleteMapping
    public Result delete(
            @RequestParam Long id) {

        setmealService.delete(id);

        return Result.success();
    }
}