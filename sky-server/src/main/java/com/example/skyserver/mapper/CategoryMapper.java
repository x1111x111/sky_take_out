package com.example.skyserver.mapper;


import com.example.skyserver.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import java.util.List;


@Mapper
public interface CategoryMapper {

    Long count(
            @Param("name") String name,
            @Param("type") Integer type
    );

    List<Category> pageQuery(
            @Param("name") String name,
            @Param("type") Integer type,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );


    @Insert("""
    insert into category
    (name,type,sort,status,create_time,update_time)
    values
    (#{name},#{type},#{sort},1,now(),now())
    """)
    void insert(Category category);

    void update(Category category);

    void deleteById(Long id);

    List<Category> listByType(
            @Param("type") Integer type
    );
}