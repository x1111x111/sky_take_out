package com.example.skyserver.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrdersRejectionDTO {

    @NotNull @Positive
    private Long id;

    @NotBlank
    private String rejectionReason;
}
