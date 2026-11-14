package com.example.skyserver.service;

import com.example.skyserver.entity.DishFlavor;

import java.util.List;

public interface DishFlavorService {

    List<DishFlavor> getByDishId(Long dishId);

    void save(DishFlavor dishFlavor);

    void delete(Long id);
}