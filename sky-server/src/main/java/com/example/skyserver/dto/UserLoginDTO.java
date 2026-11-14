package com.example.skyserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {


    @NotBlank(
            message="登录凭证不能为空"
    )
    private String code;

}