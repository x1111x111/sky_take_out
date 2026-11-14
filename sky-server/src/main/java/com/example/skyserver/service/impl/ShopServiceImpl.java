package com.example.skyserver.service.impl;

import com.example.skyserver.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.example.skyserver.exception.BusinessException;
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    // Redis Key
    private static final String SHOP_STATUS_KEY =
            "sky:shop:status";

    // 0：打烊
    // 1：营业
    private static final String CLOSED = "0";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查询店铺营业状态
     */
    @Override
    public Integer getStatus() {

        try {

            // 1. 从 Redis 获取营业状态
            String status =
                    stringRedisTemplate.opsForValue()
                            .get(SHOP_STATUS_KEY);

            // 2. 如果没有数据，初始化为打烊
            if (status == null) {

                stringRedisTemplate.opsForValue()
                        .setIfAbsent(
                                SHOP_STATUS_KEY,
                                CLOSED
                        );

                // 重新读取，防止并发设置时读到旧值
                status = stringRedisTemplate.opsForValue()
                        .get(SHOP_STATUS_KEY);
            }

            // 3. 检查状态是否合法
            if (status == null) {
                throw new BusinessException(
                        "店铺营业状态初始化失败"
                );
            }

            if (!"0".equals(status)
                    && !"1".equals(status)) {

                throw new BusinessException(
                        "店铺营业状态数据异常"
                );
            }

            return Integer.valueOf(status);

        } catch (DataAccessException e) {

            throw new BusinessException(
                    "Redis服务暂不可用，无法查询营业状态"

            );
        }
    }

    /**
     * 设置店铺营业状态
     */
    @Override
    public void setStatus(Integer status) {

        // 1. 参数校验
        if (status == null
                || (status != 0 && status != 1)) {

            throw new RuntimeException(
                    "营业状态只能设置为0或1"
            );
        }

        try {

            // 2. 写入 Redis
            stringRedisTemplate.opsForValue()
                    .set(
                            SHOP_STATUS_KEY,
                            String.valueOf(status)
                    );

        } catch (DataAccessException e) {

            throw new RuntimeException(
                    "Redis服务暂不可用，无法修改营业状态",
                    e
            );
        }
    }
}