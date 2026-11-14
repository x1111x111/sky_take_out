package com.example.skyserver.mapper;


import com.example.skyserver.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DishFlavorMapper {


    /**
     * 根据菜品id查询口味
     */
    List<DishFlavor> getByDishId(Long dishId);


    /**
     * 新增口味
     */
    void insert(DishFlavor dishFlavor);


    /**
     * 删除口味
     */
    void deleteById(Long id);

    void deleteByDishId(@Param("dishId") Long dishId);



}