
package com.example.skyserver.config;

import com.example.skyserver.mapper.CacheInvalidationEventMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationEventCleanupJob {

    private final CacheInvalidationEventMapper eventMapper;

    @Value("${sky.cache.expected-instance-ids}")
    private String expectedInstanceIds;

    @Value("${sky.cache.event-retention-days:7}")
    private int retentionDays;

    /**
     * 每隔指定时间清理一次历史事件
     */
    @Scheduled(
            fixedDelayString =
                    "${sky.cache.cleanup-interval-ms:3600000}",
            initialDelay = 60000
    )
    public void cleanup() {

        // 1. 获取预期实例列表
        List<String> instanceIds =
                Arrays.stream(expectedInstanceIds.split(","))
                        .map(String::trim)
                        .filter(id -> !id.isEmpty())
                        .distinct()
                        .toList();

        if (instanceIds.isEmpty()) {
            log.error("预期实例列表为空，取消事件清理");
            return;
        }

        // 2. 校验保留时间
        if (retentionDays < 1) {
            log.error("事件保留天数必须大于0");
            return;
        }

        try {

            // 3. 清理所有实例均已ACK的历史事件
            int deleted = eventMapper.deleteFullyAcknowledged(
                    instanceIds,
                    instanceIds.size(),
                    retentionDays
            );

            log.info(
                    "历史缓存失效事件清理完成，删除数量：{}",
                    deleted
            );

        } catch (Exception e) {

            log.error("历史缓存失效事件清理失败", e);
        }
    }
}
