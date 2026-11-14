package com.example.skyserver.dto;

import com.example.skyserver.entity.OrderDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdersDetailDTO {

    private Long id;
    private String number;
    private Integer status;

    private Long userId;
    private Long addressBookId;

    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;

    private Integer payMethod;
    private Integer payStatus;

    private BigDecimal amount;

    private String remark;
    private String phone;
    private String address;
    private String userName;
    private String consignee;

    private String cancelReason;
    private String rejectionReason;

    private LocalDateTime cancelTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveryTime;

    // 当前订单中的菜品/套餐
    private List<OrderDetail> orderDetails;
}