package com.example.skyserver.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface WorkspaceMapper {


    //今日营业额
    BigDecimal todayTurnover();


    //今日订单数量
    Integer todayOrderCount();


    //今日有效订单
    Integer validOrderCount();


    //今日新增用户
    Integer todayUserCount();


    //待接单
    Integer waitingOrders();


    //配送中
    Integer deliveryOrders();

}