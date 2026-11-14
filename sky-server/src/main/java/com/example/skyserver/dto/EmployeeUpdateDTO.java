package com.example.skyserver.dto;

import lombok.Data;

@Data
public class EmployeeUpdateDTO {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;
}