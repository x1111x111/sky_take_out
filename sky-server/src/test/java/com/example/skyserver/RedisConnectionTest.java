package com.example.skyserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes= SkyServerApplication.class)
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisConnection() {

        // 1. 写入 Redis
        stringRedisTemplate.opsForValue().set(
                "sky:test:connection",
                "hello-redis",
                Duration.ofMinutes(5)
        );

        // 2. 从 Redis 读取
        String value = stringRedisTemplate
                .opsForValue()
                .get("sky:test:connection");

        // 3. 验证结果
        assertEquals("hello-redis", value);

        System.out.println("Redis连接成功！");
        System.out.println("读取结果：" + value);
    }
}
