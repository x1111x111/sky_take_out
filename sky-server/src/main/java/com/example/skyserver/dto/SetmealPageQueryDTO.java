package com.example.skyserver.dto;

import lombok.Data;

@Data
public class SetmealPageQueryDTO {

    private Integer page=1;

    private Integer pageSize=10;

    private String name;

    private Long categoryId;

    private Integer status;
}