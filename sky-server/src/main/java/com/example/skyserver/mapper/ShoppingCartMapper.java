package com.example.skyserver.mapper;

import com.example.skyserver.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public  interface ShoppingCartMapper {


     void insert(
            ShoppingCart shoppingCart
    );


     ShoppingCart getByUserIdAndDish(
            ShoppingCart shoppingCart
    );


     void updateNumber(
            ShoppingCart shoppingCart
    ) ;


     List<ShoppingCart> list(
            Long userId
    ) ;
     List<ShoppingCart> listForUpdate(Long userId);
     int deleteSelected(@Param("userId") Long userId, @Param("ids") List<Long> ids);


    /*void clean(
           Long userId
    );*/

    //删除
     void deleteByUserId(
            Long userId
    ) ;

    void deleteById(Long id);



}
