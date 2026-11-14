package com.example.skyserver.vo;

import lombok.Data;

import java.util.List;

@Data
public class SalesTop10ReportVO {

    // 商品名称
    private List<String> nameList;

    // 销售数量
    private List<Integer> numberList;

}