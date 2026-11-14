package com.example.skyserver.dto;


import com.example.skyserver.entity.DishFlavor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class DishDTO {


    private Long id;


    @NotBlank(message = "菜品名称不能为空")
    private String name;

    @NotNull(message = "菜品分类不能为空")
    private Long categoryId;

    @NotNull(message = "菜品价格不能为空")
    @DecimalMin(value = "0.01", message = "菜品价格必须大于0")
    private BigDecimal price;


    private String image;


    private String description;


    private Integer status;


    /**
     * 菜品口味
     */
    private List<DishFlavor> flavors;

}