package com.example.skyserver.dto;

import lombok.Data;

@Data
public class EmployeePageQueryDTO {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String name;
}