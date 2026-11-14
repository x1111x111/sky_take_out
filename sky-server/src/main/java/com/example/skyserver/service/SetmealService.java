package com.example.skyserver.service;

import com.example.skyserver.dto.SetmealDTO;
import com.example.skyserver.dto.SetmealPageQueryDTO;
import com.example.skyserver.entity.Setmeal;
import com.example.skyserver.result.PageResult;

import java.util.List;

public interface SetmealService {

    void save(SetmealDTO dto);

    PageResult<Setmeal> pageQuery(
            SetmealPageQueryDTO dto
    );
    SetmealDTO getById(Long id);
    void update(SetmealDTO dto);
    void updateStatus(Integer status, Long id);
    void delete(Long id);
    List<Setmeal> listByCategoryId(Long categoryId);
}