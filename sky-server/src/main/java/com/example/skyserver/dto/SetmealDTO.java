package com.example.skyserver.dto;

import com.example.skyserver.entity.SetmealDish;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealDTO {

    private Long id;

    private Long categoryId;

    private String name;

    private BigDecimal price;

    private Integer status;

    private String description;

    private String image;

    private List<SetmealDish> setmealDishes;
}