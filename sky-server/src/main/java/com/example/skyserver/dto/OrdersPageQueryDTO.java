package com.example.skyserver.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

@Data
public class OrdersPageQueryDTO {

    @NotNull @Min(1)
    private Integer page=1;

    @NotNull @Min(1) @Max(100)
    private Integer pageSize=10;

    private String number;

    private String phone;

    private Integer status;
}
