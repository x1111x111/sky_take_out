package com.example.skyserver.controller;

import com.example.skyserver.dto.OrdersDetailDTO;
import com.example.skyserver.dto.OrdersSubmitDTO;
import com.example.skyserver.dto.UserOrderPageQueryDTO;
import com.example.skyserver.entity.Orders;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.OrdersService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.skyserver.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;

@Tag(name = "用户提交订单相关接口")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrdersService ordersService;
    @Value("${sky.payment.mock-enabled:false}")
    private boolean mockPaymentEnabled;

    @PostMapping("/submit")
    public Result<Void> submit(
            @Valid @RequestBody OrdersSubmitDTO dto) {

        ordersService.submit(dto);

        return Result.success();
    }
    @GetMapping("/historyOrders")
    public Result<PageResult<Orders>> historyOrders(
            @Valid UserOrderPageQueryDTO dto) {

        return Result.success(
                ordersService.historyOrders(dto)
        );
    }
    @GetMapping("/orderDetail/{id}")
    public Result<OrdersDetailDTO> orderDetail(
            @PathVariable Long id) {

        return Result.success(
                ordersService.userOrderDetail(id)
        );
    }
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(
            @PathVariable Long id) {

        ordersService.userCancel(id);

        return Result.success();
    }
    @PostMapping("/repetition/{id}")
    public Result<Void> repetition(
            @PathVariable Long id) {

        ordersService.repetition(id);

        return Result.success();
    }
    /**
     * 仅供本地联调使用的模拟支付接口
     */
    @PostMapping("/mock-pay/{id}")
    public Result<Void> mockPay(@PathVariable Long id) {

        // 默认关闭，防止非测试环境误用
        if (!mockPaymentEnabled) {
            throw new BusinessException("模拟支付功能未开启");
        }

        ordersService.mockPay(id);

        return Result.success();
    }
}
