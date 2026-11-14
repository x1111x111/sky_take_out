package com.example.skyserver.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CategorySaveDTO {


    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;


    /**
     * 类型
     * 1 菜品分类
     * 2 套餐分类
     */
    private Integer type;


    /**
     * 排序
     */
    private Integer sort;

}