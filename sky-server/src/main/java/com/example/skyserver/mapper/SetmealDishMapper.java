package com.example.skyserver.mapper;

import com.example.skyserver.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    void insert(SetmealDish setmealDish);

    List<SetmealDish> getBySetmealId(Long setmealId);
    void deleteBySetmealId(Long setmealId);
}