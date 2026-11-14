package com.example.skyserver.service.impl;

import com.example.skyserver.cache.DishLocalCacheManager;
import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.mapper.DishMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DishServiceImplCacheTest {
    private DishServiceImpl service;
    private DishMapper mapper;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> values;
    private DishLocalCacheManager local;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        local = new DishLocalCacheManager(Caffeine.newBuilder().build());
        service = new DishServiceImpl(CircuitBreaker.ofDefaults("test"), local);
        mapper = mock(DishMapper.class);
        redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        ReflectionTestUtils.setField(service, "dishMapper", mapper);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
        ReflectionTestUtils.setField(service, "jsonMapper", JsonMapper.builder().build());
    }

    @Test
    void ownerWritesEmptyResultWithVersionAndShortTtl() {
        when(values.setIfAbsent(eq("lock:sky:dish1"), anyString(), eq(Duration.ofSeconds(30))))
                .thenReturn(true);
        when(values.get("sky:dish:version:1")).thenReturn("7");
        when(mapper.listByCategoryId(1L)).thenReturn(List.of());
        when(redis.execute(any(), eq(List.of("sky:dish:version:1", "sky:dish1")),
                eq("7"), eq("[]"), anyString())).thenReturn(1L);
        assertEquals(List.of(), service.listByCategoryId(1L));
        var ttl = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(redis).execute(any(), eq(List.of("sky:dish:version:1", "sky:dish1")),
                eq("7"), eq("[]"), ttl.capture());
        long seconds = Long.parseLong(ttl.getValue());
        assertTrue(seconds >= 45 && seconds <= 75);
        verify(redis).execute(any(), eq(List.of("lock:sky:dish1")), anyString());
    }

    @Test
    void waiterReturnsRedisFillWithoutMysqlQuery() {
        when(values.setIfAbsent(eq("lock:sky:dish1"), anyString(), any(Duration.class)))
                .thenReturn(false);
        when(values.get("sky:dish1")).thenReturn(null, "[]");
        assertEquals(List.of(), service.listByCategoryId(1L));
        verifyNoInteractions(mapper);
    }

    @Test
    void ownerUsesValueFilledBeforeLockAcquisition() {
        when(values.get("sky:dish1")).thenReturn(null, "[]");
        when(values.setIfAbsent(eq("lock:sky:dish1"), anyString(), any(Duration.class)))
                .thenReturn(true);
        assertEquals(List.of(), service.listByCategoryId(1L));
        verifyNoInteractions(mapper);
        verify(redis).execute(any(), eq(List.of("lock:sky:dish1")), anyString());
    }

    @Test
    void versionConflictRetriesQueryInsteadOfAcceptingOldFill() {
        when(values.setIfAbsent(eq("lock:sky:dish1"), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(values.get("sky:dish:version:1")).thenReturn("7", "8");
        when(mapper.listByCategoryId(1L)).thenReturn(List.of());
        when(redis.execute(any(), eq(List.of("sky:dish:version:1", "sky:dish1")),
                anyString(), eq("[]"), anyString())).thenReturn(0L, 1L);
        assertEquals(List.of(), service.listByCategoryId(1L));
        verify(mapper, times(2)).listByCategoryId(1L);
    }

    @Test
    void redisFailureFallsBackToMysql() {
        when(values.get("sky:dish1")).thenThrow(new DataAccessResourceFailureException("unavailable"));
        when(mapper.listByCategoryId(1L)).thenReturn(List.of());
        assertEquals(List.of(), service.listByCategoryId(1L));
        verify(mapper).listByCategoryId(1L);
    }

    @Test
    void invalidationDuringLoadRejectsOldLocalFill() {
        AtomicInteger calls = new AtomicInteger();
        when(values.get("sky:dish1")).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) local.invalidate(1L);
            return "[]";
        });
        assertEquals(List.of(), service.listByCategoryId(1L));
        verify(values, times(2)).get("sky:dish1");
        verifyNoInteractions(mapper);
    }

    @Test
    void invalidCategoryDoesNotReachDependencies() {
        assertThrows(BusinessException.class, () -> service.listByCategoryId(0L));
        verifyNoInteractions(redis, mapper);
    }
}
