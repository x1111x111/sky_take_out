package com.example.skyserver.service;

import com.example.skyserver.dto.ShoppingCartDTO;
import com.example.skyserver.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    // 加入购物车
    void add(ShoppingCartDTO dto);

    // 查询当前用户购物车
    List<ShoppingCart> list();

    // 减少商品数量
     void sub(ShoppingCartDTO dto);

    // 清空当前用户购物车
     void clean() ;
}