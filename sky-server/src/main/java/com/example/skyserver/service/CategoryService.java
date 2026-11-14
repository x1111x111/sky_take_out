package com.example.skyserver.service;


import com.example.skyserver.dto.CategorySaveDTO;
import com.example.skyserver.dto.CategoryPageQueryDTO;
import com.example.skyserver.entity.Category;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.dto.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {


    void save(CategorySaveDTO dto);

    PageResult<Category> pageQuery(CategoryPageQueryDTO dto);

    void update(CategoryUpdateDTO dto);

    void deleteById(Long id);

    List<Category> listByType(Integer type);
}