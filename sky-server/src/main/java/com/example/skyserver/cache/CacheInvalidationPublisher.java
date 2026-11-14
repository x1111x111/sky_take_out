package com.example.skyserver.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationPublisher {

    /**
     * 菜品缓存失效通知频道
     */
    public static final String DISH_CACHE_INVALIDATION_TOPIC =
            "sky:cache:invalidate:dish";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发布分类缓存失效消息
     */
    public void publishCategoryInvalidation(Long categoryId) {

        if (categoryId == null || categoryId <= 0) {
            return;
        }

        try {

            Long receivers = stringRedisTemplate.convertAndSend(
                    DISH_CACHE_INVALIDATION_TOPIC,
                    categoryId.toString()
            );

            log.info(
                    "发布菜品缓存失效通知成功，categoryId={}，接收订阅者数量={}",
                    categoryId,
                    receivers
            );

        } catch (DataAccessException e) {

            log.error(
                    "发布菜品缓存失效通知失败，categoryId={}",
                    categoryId,
                    e
            );
        }
    }
}