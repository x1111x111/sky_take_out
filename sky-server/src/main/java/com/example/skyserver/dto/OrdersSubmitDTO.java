package com.example.skyserver.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class OrdersSubmitDTO {

    @NotNull @Positive
    private Long addressBookId;
    @NotNull @Min(1) @Max(2)
    private Integer payMethod;

    private String remark;
}
