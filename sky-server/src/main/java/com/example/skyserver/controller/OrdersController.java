package com.example.skyserver.controller;

import com.example.skyserver.dto.OrdersConfirmDTO;
import com.example.skyserver.dto.OrdersDetailDTO;
import com.example.skyserver.dto.OrdersPageQueryDTO;
import com.example.skyserver.dto.OrdersRejectionDTO;
import com.example.skyserver.entity.Orders;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.OrdersService;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.OrderStatisticsVO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;

@Tag(name="订单相关接口")
@SecurityRequirement(name="token")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping("/conditionSearch")
    public Result<PageResult<Orders>> conditionSearch(
            @Valid OrdersPageQueryDTO dto) {

        PageResult<Orders> pageResult =
                ordersService.pageQuery(dto);

        return Result.success(pageResult);
    }
    @GetMapping("/details/{id}")
    public Result<OrdersDetailDTO> details(
            @PathVariable Long id) {

        OrdersDetailDTO dto =
                ordersService.getById(id);

        return Result.success(dto);
    }
    @PutMapping("/confirm")
    public Result confirm(
            @Valid @RequestBody OrdersConfirmDTO dto) {

        ordersService.confirm(dto.getId());

        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    public Result delivery(
            @PathVariable Long id) {


        ordersService.delivery(id);


        return Result.success();

    }
    @PutMapping("/complete/{id}")
    public Result complete(
            @PathVariable Long id) {


        ordersService.complete(id);


        return Result.success();

    }
    @PutMapping("/rejection")
    public Result rejection(
            @Valid @RequestBody OrdersRejectionDTO dto) {

        ordersService.rejection(dto);

        return Result.success();
    }
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {

        OrderStatisticsVO vo =
                ordersService.statistics();

        return Result.success(vo);
    }

}
