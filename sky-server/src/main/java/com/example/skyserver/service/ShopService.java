package com.example.skyserver.service;

import io.swagger.v3.oas.models.security.SecurityScheme;

public interface ShopService {
    Integer getStatus();

    void setStatus(Integer status);
}
