package com.example.skyserver.mapper;

import com.example.skyserver.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper {

    void insert(Setmeal setmeal);

    Long count(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status
    );

    List<Setmeal> pageQuery(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    Setmeal getById(Long id);

    List<Setmeal>listByCategoryId(
            @Param("categoryId")Long categoryId
    );

    void update(Setmeal setmeal);

    void updateStatus(
            @Param("status") Integer status,
            @Param("id") Long id
    );
    void deleteById(Long id);
}