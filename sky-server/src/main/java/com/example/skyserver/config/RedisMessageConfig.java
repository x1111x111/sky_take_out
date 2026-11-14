package com.example.skyserver.config;

import com.example.skyserver.cache.CacheInvalidationListener;
import com.example.skyserver.cache.CacheInvalidationPublisher;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisMessageConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    private final CacheInvalidationListener cacheInvalidationListener;

    /**
     * 创建 Redis 消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {

        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        // 设置 Redis 连接
        container.setConnectionFactory(redisConnectionFactory);

        // 订阅菜品缓存失效频道
        container.addMessageListener(
                cacheInvalidationListener,
                new ChannelTopic(
                        CacheInvalidationPublisher
                                .DISH_CACHE_INVALIDATION_TOPIC
                )
        );

        return container;
    }
}