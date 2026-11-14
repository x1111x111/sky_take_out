package com.example.skyserver.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class OrdersConfirmDTO {

    @NotNull @Positive
    private Long id;
}
