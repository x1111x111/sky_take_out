
package com.example.skyserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class CacheEvictionSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler cacheEvictionTaskScheduler() {

        ThreadPoolTaskScheduler scheduler =
                new ThreadPoolTaskScheduler();

        // 延迟缓存清理使用独立线程池
        scheduler.setPoolSize(2);

        // 设置线程名称
        scheduler.setThreadNamePrefix("dish-cache-evict-");

        // 应用关闭时等待正在执行的任务
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(5);

        return scheduler;
    }
}
