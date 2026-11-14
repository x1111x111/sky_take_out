package com.example.skyserver.cache;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener implements MessageListener {

    private final DishLocalCacheManager dishLocalCacheManager;

    /**
     * 接收 Redis 发布的缓存失效消息
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {

        String body = new String(
                message.getBody(),
                StandardCharsets.UTF_8
        );

        try {

            Long categoryId = Long.valueOf(body);

            if (categoryId <= 0) {
                return;
            }

            // 删除当前服务器的 Caffeine 缓存
            dishLocalCacheManager.invalidate(categoryId);

            log.info(
                    "收到缓存失效通知，已递增本地版本并清理Caffeine，categoryId={}",
                    categoryId
            );


        } catch (NumberFormatException e) {

            log.warn("收到非法缓存失效消息，body={}", body);
        }
    }
}