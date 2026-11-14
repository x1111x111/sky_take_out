package com.example.skyserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import com.example.skyserver.entity.DishFlavor;
import java.util.List;

@Data
public class DishUpdateDTO {

    @NotNull(message = "菜品ID不能为空")
    private Long id;

    private String name;

    private Long categoryId;

    private BigDecimal price;

    private String image;

    private String description;

    private Integer status;

    private List<DishFlavor> flavors;
}
