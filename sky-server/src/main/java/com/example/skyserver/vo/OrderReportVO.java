package com.example.skyserver.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderReportVO {

    // 日期列表
    private List<String> dateList;

    // 每天订单总数
    private List<Integer> orderCountList;

    // 每天有效订单数
    private List<Integer> validOrderCountList;

    // 时间段订单总数
    private Integer totalOrderCount;

    // 时间段有效订单总数
    private Integer validOrderCount;

    // 订单完成率
    private Double orderCompletionRate;
}