package com.example.skyserver.service.impl;

import com.example.skyserver.context.UserContext;
import com.example.skyserver.dto.ShoppingCartDTO;
import com.example.skyserver.entity.ShoppingCart;
import com.example.skyserver.mapper.ShoppingCartMapper;
import com.example.skyserver.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl
        implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;


    @Override
    public void add(ShoppingCartDTO dto) {

        Long userId =
                UserContext.getUserId();

        ShoppingCart cart =
                new ShoppingCart();

        BeanUtils.copyProperties(dto, cart);

        cart.setUserId(userId);

        ShoppingCart old =
                shoppingCartMapper
                        .getByUserIdAndDish(cart);

        if (old != null) {

            old.setNumber(
                    old.getNumber() + 1
            );

            shoppingCartMapper
                    .updateNumber(old);

        } else {

            cart.setNumber(1);

            shoppingCartMapper.insert(cart);
        }
    }


    @Override
    public List<ShoppingCart> list() {

        Long userId =
                UserContext.getUserId();

        return shoppingCartMapper.list(userId);
    }


    @Override
    public void sub(ShoppingCartDTO dto) {

        Long userId =
                UserContext.getUserId();

        ShoppingCart cart =
                new ShoppingCart();

        BeanUtils.copyProperties(dto, cart);

        cart.setUserId(userId);

        ShoppingCart old =
                shoppingCartMapper
                        .getByUserIdAndDish(cart);

        if (old == null) {
            throw new RuntimeException(
                    "购物车中不存在该商品"
            );
        }

        if (old.getNumber() > 1) {

            old.setNumber(
                    old.getNumber() - 1
            );

            shoppingCartMapper
                    .updateNumber(old);

        } else {

            shoppingCartMapper
                    .deleteById(old.getId());
        }
    }


    @Override
    public void clean() {

        Long userId =
                UserContext.getUserId();

        shoppingCartMapper
                .deleteByUserId(userId);
    }
}

