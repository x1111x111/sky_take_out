package com.example.skyserver.controller;

import com.example.skyserver.result.Result;
import com.example.skyserver.service.ShopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="店铺相关接口")
@SecurityRequirement(name="token")
@RestController
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    /**
     * 管理端：设置营业状态
     */
    @PutMapping("/admin/shop/status/{status}")
    public Result setStatus(
            @PathVariable Integer status) {

        shopService.setStatus(status);

        return Result.success();
    }

    /**
     * 管理端：查询营业状态
     */
    @GetMapping("/admin/shop/status")
    public Result<Integer> getAdminStatus() {

        return Result.success(
                shopService.getStatus()
        );
    }

    /**
     * 用户端：查询营业状态
     */
    @GetMapping("/user/shop/status")
    public Result<Integer> getUserStatus() {

        return Result.success(
                shopService.getStatus()
        );
    }
}
