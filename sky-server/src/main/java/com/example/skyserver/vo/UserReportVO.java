package com.example.skyserver.vo;


import lombok.Data;

import java.util.List;


@Data
public class UserReportVO {


    // 日期集合
    private List<String> dateList;


    // 新增用户数量
    private List<Integer> newUserList;

}