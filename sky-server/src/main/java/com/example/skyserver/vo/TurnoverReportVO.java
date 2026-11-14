package com.example.skyserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TurnoverReportVO {

    // 日期，例如 ["2026-09-15","2026-09-16"]
    private List<String> dateList;

    // 每天营业额
    private List<BigDecimal> turnoverList;
}