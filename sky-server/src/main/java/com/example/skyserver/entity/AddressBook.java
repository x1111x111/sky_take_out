package com.example.skyserver.entity;

import lombok.Data;

@Data
public class AddressBook {

    private Long id;
    private Long userId;

    private String consignee;
    private String phone;

    private String provinceName;
    private String cityName;
    private String districtName;
    private String detail;
}