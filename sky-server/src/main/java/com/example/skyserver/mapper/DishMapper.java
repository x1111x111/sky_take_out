package com.example.skyserver.mapper;

import com.example.skyserver.entity.Dish;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    Long count(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status
    );

    List<Dish> pageQuery(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );
    List<Dish>listByCategoryId(
            @Param("categoryId")Long categoryId
    );

    @Insert("""
    insert into dish
    (
        name,
        category_id,
        price,
        image,
        description,
        status,
        create_time,
        update_time,
        create_user,
        update_user
    )
    values
    (
        #{name},
        #{categoryId},
        #{price},
        #{image},
        #{description},
        #{status},
        #{createTime},
        #{updateTime},
        #{createUser},
        #{updateUser}
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    void update(Dish dish);

    void delete(Long id);

    void updateStatus(
            @Param("status") Integer status,
            @Param("id") Long id
    );
    void deleteByDishId(Long dishId);

    Dish getById(Long id);
}