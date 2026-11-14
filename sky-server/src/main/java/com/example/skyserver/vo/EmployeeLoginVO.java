package com.example.skyserver.vo;

import lombok.Data;

@Data
public class EmployeeLoginVO {

    private Long id;

    private String username;

    private String name;

    private String token;
}