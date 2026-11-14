package com.example.skyserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishSaveDTO {

    private String name;

    private Long categoryId;

    private BigDecimal price;

    private String image;

    private String description;

    private Integer status;

}