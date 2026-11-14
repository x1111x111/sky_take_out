package com.example.skyserver.service.impl;


import com.example.skyserver.dto.CategoryPageQueryDTO;
import com.example.skyserver.dto.CategorySaveDTO;
import com.example.skyserver.dto.CategoryUpdateDTO;
import com.example.skyserver.dto.SetmealPageQueryDTO;
import com.example.skyserver.entity.Category;
import com.example.skyserver.entity.Setmeal;
import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.mapper.CategoryMapper;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {


    private final CategoryMapper categoryMapper;


    @Override
    public void save(CategorySaveDTO dto) {


        Category category = new Category();


        BeanUtils.copyProperties(dto, category);


        categoryMapper.insert(category);

    }
    @Override
    public PageResult<Category> pageQuery(CategoryPageQueryDTO dto) {


        Integer offset =
                (dto.getPage() - 1) * dto.getPageSize();


        Long total = categoryMapper.count(
                dto.getName(),
                dto.getType()
        );


        List<Category> records =
                categoryMapper.pageQuery(
                        dto.getName(),
                        dto.getType(),
                        offset,
                        dto.getPageSize()
                );


        return new PageResult<>(total, records);
    }

    @Override
    public void update(CategoryUpdateDTO dto) {


        Category category = new Category();


        BeanUtils.copyProperties(dto, category);


        categoryMapper.update(category);

    }

    @Override
    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> listByType(Integer type) {

        if (type == null || (type != 1 && type != 2)) {
            throw new BusinessException("分类类型不合法");
        }

        return categoryMapper.listByType(type);
    }

}