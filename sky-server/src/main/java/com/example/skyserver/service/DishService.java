package com.example.skyserver.service;

import com.example.skyserver.dto.DishDTO;
import com.example.skyserver.dto.DishPageQueryDTO;
import com.example.skyserver.dto.DishSaveDTO;
import com.example.skyserver.dto.DishUpdateDTO;
import com.example.skyserver.entity.Dish;
import com.example.skyserver.result.PageResult;

import java.util.List;

public interface DishService {

    PageResult<Dish> pageQuery(DishPageQueryDTO dto);

    void save(DishDTO dto);;

    void update(DishDTO dto);

    void delete(Long id);

    void updateStatus(Integer status, Long id);

    DishDTO getById(Long id);

    List<DishDTO> listByCategoryId(Long categoryId);
}