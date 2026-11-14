package com.example.skyserver.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
@Slf4j
@Configuration
public class RedisCircuitBreakerConfig {

    @Bean
    public CircuitBreaker redisCircuitBreaker() {

        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()

                        // 失败率达到50%触发熔断
                        .failureRateThreshold(50)

                        // 最近10次调用
                        .slidingWindowSize(10)

                        // 至少5次调用后才计算失败率
                        .minimumNumberOfCalls(5)

                        // 熔断等待15秒
                        .waitDurationInOpenState(
                                Duration.ofSeconds(15)
                        )

                        // 半开状态只允许1次探测
                        .permittedNumberOfCallsInHalfOpenState(1)

                        // 自动从OPEN进入HALF_OPEN
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)

                        // 只将Spring数据访问异常计入失败
                        .recordExceptions(DataAccessException.class)

                        .build();

        CircuitBreaker breaker = CircuitBreaker.of("redis", config);

// 监听熔断器状态变化
        breaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Redis熔断状态变化：{}", event)
                )
                .onFailureRateExceeded(event ->
                        log.warn("Redis失败率超标：{}", event)
                )
                .onCallNotPermitted(event ->
                        log.warn("Redis请求被熔断器拒绝：{}", event)
                );

        log.info("Redis熔断器初始化完成，当前状态：{}",
                breaker.getState());

        return breaker;
    }
}