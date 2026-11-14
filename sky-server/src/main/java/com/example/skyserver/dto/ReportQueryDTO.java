package com.example.skyserver.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportQueryDTO {


    private LocalDate begin;

    private LocalDate end;

}
