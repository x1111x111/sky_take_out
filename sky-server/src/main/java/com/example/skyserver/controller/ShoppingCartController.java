package com.example.skyserver.controller;

import com.example.skyserver.dto.ShoppingCartDTO;
import com.example.skyserver.entity.ShoppingCart;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "购物车相关接口")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/user/shoppingCart")
@RequiredArgsConstructor
public class ShoppingCartController {

        private final ShoppingCartService service;

        @PostMapping("/add")
        public Result add(
                @RequestBody ShoppingCartDTO dto){

            service.add(dto);

            return Result.success();

        }

        @GetMapping("/list")
        public Result<List<ShoppingCart>> list(){

            return Result.success(
                    service.list()
            );

        }
    @PostMapping("/sub")
    public Result sub(
            @RequestBody ShoppingCartDTO dto) {

        service.sub(dto);

        return Result.success();
    }
    @DeleteMapping("/clean")
    public Result clean() {

        service.clean();

        return Result.success();
    }
    }
