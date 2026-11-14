package com.example.skyserver.dto;


import lombok.Data;

import java.math.BigDecimal;


@Data
public class ShoppingCartDTO {


    private Long dishId;


    private Long setmealId;


    private String dishFlavor;


    private Integer number;


    private String name;


    private String image;


    private BigDecimal amount;

}