package com.example.skyserver.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;


@Data
public class CategoryUpdateDTO {


    /**
     * 分类id
     */
    @NotNull(message = "分类id不能为空")
    private Long id;


    /**
     * 分类名称
     */
    private String name;


    /**
     * 类型
     */
    private Integer type;


    /**
     * 排序
     */
    private Integer sort;

}