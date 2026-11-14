
package com.example.skyserver.cache;

import com.example.skyserver.entity.CacheInvalidationEvent;
import com.example.skyserver.mapper.CacheInvalidationAckMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationPoller {

    private final CacheInvalidationAckMapper ackMapper;

    private final DishLocalCacheManager localCacheManager;

    private final StringRedisTemplate redisTemplate;

    @Value("${sky.cache.instance-id}")
    private String instanceId;

    private static final int BATCH_SIZE = 100;

    private static final String CACHE_PREFIX = "sky:dish";

    private static final String VERSION_PREFIX =
            "sky:dish:version:";

    /**
     * 原子递增 Redis 版本并删除菜品缓存
     */
    private static final DefaultRedisScript<Long>
            INVALIDATE_SCRIPT = new DefaultRedisScript<>(
            "local version = redis.call('INCR', KEYS[1]); " +
                    "redis.call('DEL', KEYS[2]); " +
                    "return version;",
            Long.class
    );

    /**
     * 每轮结束后等待2秒，再开始下一轮
     */
    @Scheduled(
            fixedDelayString =
                    "${sky.cache.poll-interval-ms:2000}",
            initialDelayString =
                    "${sky.cache.poll-initial-delay-ms:5000}"
    )
    public void poll() {

        List<CacheInvalidationEvent> events;

        // 1. 查询当前实例尚未确认的事件
        try {

            events = ackMapper.findUnacked(
                    instanceId,
                    BATCH_SIZE
            );

        } catch (RuntimeException e) {

            log.error("查询缓存失效事件失败", e);
            return;
        }

        if (events.isEmpty()) {
            return;
        }

        log.info("本轮发现{}条待处理失效事件", events.size());

        // 2. 逐条处理
        for (CacheInvalidationEvent event : events) {

            Long categoryId = event.getCategoryId();

            if (categoryId == null || categoryId <= 0) {
                log.error("非法缓存失效事件，eventId={}",
                        event.getId());
                continue;
            }

            try {

                String cacheKey =
                        CACHE_PREFIX + categoryId;

                String versionKey =
                        VERSION_PREFIX + categoryId;

                // 3. Redis版本递增并删除共享缓存
                Long newVersion = redisTemplate.execute(
                        INVALIDATE_SCRIPT,
                        Arrays.asList(versionKey, cacheKey)
                );

                if (newVersion == null) {
                    throw new IllegalStateException(
                            "Redis失效脚本未返回结果"
                    );
                }

                // 4. 递增本地代际版本并清除Caffeine
                localCacheManager.invalidate(categoryId);

                // 5. 全部成功后才能写入ACK
                ackMapper.acknowledge(
                        event.getId(),
                        instanceId
                );

                log.info(
                        "缓存失效事件处理成功，eventId={}，" +
                                "instanceId={}，categoryId={}，version={}",
                        event.getId(),
                        instanceId,
                        categoryId,
                        newVersion
                );

            } catch (DataAccessException e) {

                // Redis或MySQL故障：保留未确认事件
                log.warn(
                        "缓存失效依赖服务异常，停止本轮处理，eventId={}",
                        event.getId(),
                        e
                );

                break;

            } catch (RuntimeException e) {

                log.error(
                        "缓存失效事件处理失败，等待下次重试，eventId={}",
                        event.getId(),
                        e
                );
            }
        }
    }
}
