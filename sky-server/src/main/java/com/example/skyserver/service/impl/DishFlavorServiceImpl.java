package com.example.skyserver.service.impl;


import com.example.skyserver.entity.DishFlavor;
import com.example.skyserver.mapper.DishFlavorMapper;
import com.example.skyserver.service.DishFlavorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DishFlavorServiceImpl
        implements DishFlavorService {


    private final DishFlavorMapper dishFlavorMapper;



    @Override
    public List<DishFlavor> getByDishId(Long dishId){

        return dishFlavorMapper.getByDishId(dishId);

    }



    @Override
    public void save(DishFlavor dishFlavor){

        dishFlavorMapper.insert(dishFlavor);

    }



    @Override
    public void delete(Long id){

        dishFlavorMapper.deleteById(id);

    }

}