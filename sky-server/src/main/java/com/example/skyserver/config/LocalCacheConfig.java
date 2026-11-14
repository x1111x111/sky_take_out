package com.example.skyserver.config;

import com.example.skyserver.dto.DishDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class LocalCacheConfig {

    @Bean
    public Cache<Long, List<DishDTO>> dishLocalCache() {
        return Caffeine.<Long, List<DishDTO>>newBuilder()
                .initialCapacity(20)
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
}