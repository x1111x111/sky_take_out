package com.example.skyserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessDataVO {


    //今日营业额
    private BigDecimal turnover;


    //有效订单数量
    private Integer validOrderCount;


    //订单总数
    private Integer orderCount;


    //新增用户数量
    private Integer newUserCount;


    //待接单数量
    private Integer waitingOrders;


    //派送中数量
    private Integer deliveryOrders;

}