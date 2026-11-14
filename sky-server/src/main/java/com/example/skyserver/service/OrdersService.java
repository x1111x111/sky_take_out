package com.example.skyserver.service;

import com.example.skyserver.dto.*;
import com.example.skyserver.entity.Orders;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface OrdersService {

    PageResult<Orders> pageQuery(
            OrdersPageQueryDTO dto
    );
    OrdersDetailDTO getById(Long id);
    void confirm(Long id);
    void delivery(Long id);
    void complete(Long id);
    void rejection(OrdersRejectionDTO dto);
    OrderStatisticsVO statistics();
    void submit(OrdersSubmitDTO dto);
    PageResult<Orders> historyOrders(
            UserOrderPageQueryDTO dto
    );
    OrdersDetailDTO userOrderDetail(Long id);
    void userCancel(Long id);
    void repetition(Long id);
    /**
     * 本地模拟支付成功
     */
    void mockPay(Long orderId);
}