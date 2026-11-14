package com.example.skyserver.service.impl;

import com.example.skyserver.cache.CacheInvalidationPublisher;
import com.example.skyserver.cache.DishLocalCacheManager;
import com.example.skyserver.dto.DishDTO;
import com.example.skyserver.dto.DishPageQueryDTO;
import com.example.skyserver.entity.CacheInvalidationEvent;
import com.example.skyserver.entity.Dish;
import com.example.skyserver.entity.DishFlavor;
import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.mapper.CacheInvalidationEventMapper;
import com.example.skyserver.mapper.DishFlavorMapper;
import com.example.skyserver.mapper.DishMapper;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.service.DishService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.dao.DataAccessException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    /**
     * MySQL 降级查询并发限制
     * 单个 Spring Boot 实例最多允许 5 个降级查询同时执行
     */
    private final Semaphore dishDbSemaphore = new Semaphore(5);

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CacheInvalidationEventMapper cacheInvalidationEventMapper;

    @Autowired
    private CacheInvalidationPublisher cacheInvalidationPublisher;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String DISH_CACHE_PREFIX="sky:dish";
    // 菜品缓存版本号前缀
    private static final String DISH_CACHE_VERSION_PREFIX =
            "sky:dish:version:";

    // 缓存重建最多重试次数
    private static final int CACHE_REBUILD_MAX_RETRIES = 3;
    private static final Duration DISH_LOCK_TTL = Duration.ofSeconds(30);
    private static final int DISH_LOCK_RETRIES = 40;
    private static final long DISH_LOCK_RETRY_DELAY_MS = 50;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    /**
     * 缓存失效：
     * 1. 原子增加版本号
     * 2. 删除 Redis 缓存
     */
    private static final DefaultRedisScript<Long> INVALIDATE_DISH_CACHE_SCRIPT =
            new DefaultRedisScript<>(
                    "local version = redis.call('INCR', KEYS[1]); " +
                            "redis.call('DEL', KEYS[2]); " +
                            "return version;",
                    Long.class
            );

    /**
     * 只有版本号一致，才允许写入 Redis 缓存
     */
    private static final DefaultRedisScript<Long> WRITE_DISH_CACHE_SCRIPT =
            new DefaultRedisScript<>(
                    "local currentVersion = redis.call('GET', KEYS[1]); " +
                            "if currentVersion == ARGV[1] then " +
                            "   redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3]); " +
                            "   return 1; " +
                            "end; " +
                            "return 0;",
                    Long.class
            );
    private final CircuitBreaker redisCircuitBreaker;
    private final DishLocalCacheManager dishLocalCacheManager;
    private static final int LOCAL_CACHE_REBUILD_MAX_RETRIES = 3;
    @Autowired
    private ThreadPoolTaskScheduler cacheEvictionTaskScheduler;

    // 延迟二次失效的初始实验时间：3秒
    private static final long CACHE_SECOND_EVICT_DELAY_MS = 3000L;


    @Override
    public PageResult<Dish> pageQuery(DishPageQueryDTO dto) {

        Integer offset =
                (dto.getPage() - 1) * dto.getPageSize();

        Long total = dishMapper.count(
                dto.getName(),
                dto.getCategoryId(),
                dto.getStatus()
        );

        List<Dish> records = dishMapper.pageQuery(
                dto.getName(),
                dto.getCategoryId(),
                dto.getStatus(),
                offset,
                dto.getPageSize()
        );

        return new PageResult<>(total, records);
    }

    @Override
    @Transactional
    public void save(DishDTO dto) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);

        LocalDateTime now = LocalDateTime.now();
        dish.setCreateTime(now);
        dish.setUpdateTime(now);

        if (dish.getStatus() == null) {
            dish.setStatus(0);
        }

        // 1. 保存菜品
        dishMapper.insert(dish);

        // 2. 保存口味
        List<DishFlavor> flavors = dto.getFlavors();

        if (flavors != null) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
                dishFlavorMapper.insert(flavor);
            }
        }

        // 3. 事务提交后清理分类缓存
        evictDishCacheAfterCommit(dish.getCategoryId());
    }



    @Override
    @Transactional
    public void update(DishDTO dto) {

        // 1. 查询修改前的菜品，记录原分类
        Dish oldDish = dishMapper.getById(dto.getId());

        // 2. 修改菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);

        dishMapper.update(dish);

        // 3. 删除原来的口味
        dishFlavorMapper.deleteByDishId(dto.getId());

        // 4. 保存最新口味
        List<DishFlavor> flavors = dto.getFlavors();

        if (flavors != null) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dto.getId());
                dishFlavorMapper.insert(flavor);
            }
        }

        // 5. 清理原分类和新分类缓存
        evictDishCacheAfterCommit(
                oldDish == null ? null : oldDish.getCategoryId(),
                dto.getCategoryId()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {

        // 1. 删除前查询原菜品
        Dish oldDish = dishMapper.getById(id);

        // 2. 删除菜品口味
        dishFlavorMapper.deleteByDishId(id);

        // 3. 删除菜品
        dishMapper.delete(id);

        // 4. 清理原分类缓存
        evictDishCacheAfterCommit(
                oldDish == null ? null : oldDish.getCategoryId()
        );
    }

    @Override
    @Transactional
    public void updateStatus(Integer status, Long id) {

        // 1. 查询原菜品
        Dish oldDish = dishMapper.getById(id);

        // 2. 更新售卖状态
        dishMapper.updateStatus(status, id);

        // 3. 清理分类缓存
        evictDishCacheAfterCommit(
                oldDish == null ? null : oldDish.getCategoryId()
        );
    }
    @Override
    public DishDTO getById(Long id) {

        // 1. 查询菜品基本信息
        Dish dish = dishMapper.getById(id);

        if (dish == null) {
            return null;
        }

        // 2. 查询菜品口味
        List<DishFlavor> flavors =
                dishFlavorMapper.getByDishId(id);

        // 3. 组装成 DishDTO
        DishDTO dishDTO = new DishDTO();

        BeanUtils.copyProperties(dish, dishDTO);

        dishDTO.setFlavors(flavors);

        return dishDTO;
    }
    /**
     * 用户端：根据分类查询起售菜品
     */

    /**
     * 用户端：根据分类查询起售菜品（Redis 缓存）
     */


    @Override
    public List<DishDTO> listByCategoryId(Long categoryId) {

        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException("菜品分类ID不合法");
        }

        // 1. 先读取本地缓存和版本快照
        DishLocalCacheManager.Snapshot firstSnapshot =
                dishLocalCacheManager.getSnapshot(categoryId);

        if (firstSnapshot.getCachedValue() != null) {
            return firstSnapshot.getCachedValue();
        }

        // 2. 同一分类只允许一个本地线程重建缓存
        return dishLocalCacheManager.withLoadLock(
                categoryId,
                () -> {

                    for (int attempt = 1;
                         attempt <= LOCAL_CACHE_REBUILD_MAX_RETRIES;
                         attempt++) {

                        // 3. 双重检查
                        DishLocalCacheManager.Snapshot snapshot =
                                dishLocalCacheManager.getSnapshot(categoryId);

                        if (snapshot.getCachedValue() != null) {
                            return snapshot.getCachedValue();
                        }

                        // 4. 查询 Redis / MySQL
                        List<DishDTO> result =
                                loadDishCategory(categoryId);

                        // 5. 检查本地版本后再写入 Caffeine
                        boolean accepted =
                                dishLocalCacheManager.putIfGenerationUnchanged(
                                        categoryId,
                                        snapshot.getGeneration(),
                                        result
                                );

                        if (accepted) {

                            log.info(
                                    "Caffeine缓存写入成功，categoryId={}，generation={}",
                                    categoryId,
                                    snapshot.getGeneration()
                            );

                            return result;
                        }

                        log.warn(
                                "本地缓存版本发生变化，拒绝旧数据写入，categoryId={}，attempt={}",
                                categoryId,
                                attempt
                        );
                    }

                    throw new BusinessException(
                            "菜品数据正在更新，请稍后重试"
                    );
                }
        );
    }



    private List<DishDTO> loadDishCategory(Long categoryId) {

        String key = DISH_CACHE_PREFIX + categoryId;
        String lockKey = "lock:" + key;

        // 1. 首次读取 Redis
        try {
            List<DishDTO> cached = readDishCache(key);

            if (cached != null) {
                return cached;
            }

        } catch (DataAccessException | CallNotPermittedException e) {

            log.warn("Redis不可用或熔断，进入MySQL限流降级", e);

            return queryDishCategoryWithLimit(categoryId);
        }

        // 2. 尝试获取分布式锁
        for (int attempt = 0; attempt < DISH_LOCK_RETRIES; attempt++) {

            String lockValue = UUID.randomUUID().toString();

            boolean locked;

            try {
                locked = Boolean.TRUE.equals(
                        redisCircuitBreaker.executeSupplier(
                                () -> stringRedisTemplate.opsForValue()
                                        .setIfAbsent(lockKey, lockValue, DISH_LOCK_TTL)
                        )
                );

            } catch (DataAccessException | CallNotPermittedException e) {

                log.warn("Redis获取锁失败或熔断，进入限流降级", e);

                return queryDishCategoryWithLimit(categoryId);
            }

            // 3. 获取锁成功
            if (locked) {

                try {

                    // 双重检查 Redis
                    List<DishDTO> cached;

                    try {
                        cached = readDishCache(key);

                    } catch (DataAccessException | CallNotPermittedException e) {

                        log.warn("Redis二次读取失败，进入限流降级", e);

                        return queryDishCategoryWithLimit(categoryId);
                    }

                    if (cached != null) {
                        return cached;
                    }


// 4. 获取版本号
                    String versionKey = DISH_CACHE_VERSION_PREFIX + categoryId;

                    for (int rebuildAttempt = 1;
                         rebuildAttempt <= CACHE_REBUILD_MAX_RETRIES;
                         rebuildAttempt++) {

                        String expectedVersion;

                        try {

                            expectedVersion =
                                    getOrInitializeDishCacheVersion(versionKey);

                        } catch (DataAccessException | CallNotPermittedException e) {

                            log.warn(
                                    "获取缓存版本失败，进入MySQL限流降级，categoryId={}",
                                    categoryId,
                                    e
                            );

                            return queryDishCategoryWithLimit(categoryId);
                        }

                        // 5. 查询 MySQL
                        List<DishDTO> result = queryDishCategory(categoryId);

                        // 6. 版本校验后写入 Redis
                        boolean accepted = writeDishCache(
                                key,
                                versionKey,
                                expectedVersion,
                                result
                        );

                        if (accepted) {
                            return result;
                        }

                        log.warn(
                                "菜品缓存重建检测到版本变化，准备重新查询，categoryId={}，attempt={}",
                                categoryId,
                                rebuildAttempt
                        );
                    }

// 多次检测到版本变化，拒绝继续缓存重建
                    throw new BusinessException("菜品数据正在更新，请稍后重试");




                } finally {

                    // 6. 释放锁
                    releaseDishLock(lockKey, lockValue);
                }
            }

            // 7. 没获取到锁，检查其他实例是否已经完成缓存重建
            try {

                List<DishDTO> cached = readDishCache(key);

                if (cached != null) {
                    return cached;
                }

            } catch (DataAccessException | CallNotPermittedException e) {

                log.warn("Redis轮询读取失败，进入限流降级", e);

                return queryDishCategoryWithLimit(categoryId);
            }

            // 8. 等待其他实例完成缓存重建
            if (attempt < DISH_LOCK_RETRIES - 1) {

                try {
                    Thread.sleep(DISH_LOCK_RETRY_DELAY_MS);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    throw new BusinessException("等待菜品缓存时被中断");
                }
            }
        }

        throw new BusinessException("菜品数据正在加载，请稍后重试");
    }

    /**
     * 限制 MySQL 降级查询并发
     */
    private List<DishDTO> queryDishCategoryWithLimit(Long categoryId) {

        // 尝试获取通行证，不等待
        if (!dishDbSemaphore.tryAcquire()) {

            log.warn("MySQL降级查询达到并发上限，categoryId={}",
                    categoryId);

            throw new BusinessException(
                    "系统繁忙，菜品数据暂不可用，请稍后重试"
            );
        }

        try {

            log.warn("执行MySQL降级查询，categoryId={}", categoryId);

            return queryDishCategory(categoryId);

        } finally {

            // 无论查询成功还是失败，都释放通行证
            dishDbSemaphore.release();
        }
    }

    /**
     * 获取或初始化菜品缓存版本号
     */
    private String getOrInitializeDishCacheVersion(String versionKey) {

        String version = redisCircuitBreaker.executeSupplier(
                () -> stringRedisTemplate.opsForValue().get(versionKey)
        );

        if (version != null) {
            return version;
        }

        // 不存在时初始化为0
        redisCircuitBreaker.executeSupplier(
                () -> stringRedisTemplate.opsForValue()
                        .setIfAbsent(versionKey, "0")
        );

        // 再次读取，避免并发初始化或更新造成读取错误
        version = redisCircuitBreaker.executeSupplier(
                () -> stringRedisTemplate.opsForValue().get(versionKey)
        );

        if (version == null) {
            throw new BusinessException("获取菜品缓存版本失败");
        }

        return version;
    }
    private List<DishDTO> readDishCache(String key) {

        // 1. 通过熔断器执行 Redis 读取
        String cacheJson = redisCircuitBreaker.executeSupplier(
                () -> stringRedisTemplate.opsForValue().get(key)
        );

        if (cacheJson == null) {
            return null;
        }

        // 2. JSON 解析不属于 Redis 连接操作
        try {
            return jsonMapper.readValue(
                    cacheJson,
                    new TypeReference<List<DishDTO>>() {}
            );

        } catch (JacksonException e) {

            log.warn("Redis JSON解析失败，key={}", key, e);

            // 3. 尝试删除损坏的缓存
            try {
                redisCircuitBreaker.executeSupplier(
                        () -> stringRedisTemplate.delete(key)
                );
            } catch (DataAccessException | CallNotPermittedException ex) {
                log.warn("删除损坏缓存失败，key={}", key, ex);
            }

            return null;
        }
    }


    private List<DishDTO> queryDishCategory(Long categoryId) {
        List<Dish> dishes = dishMapper.listByCategoryId(categoryId);
        List<DishDTO> result = new ArrayList<>();
        if (dishes != null) {
            for (Dish dish : dishes) {
                DishDTO dishDTO = new DishDTO();
                BeanUtils.copyProperties(dish, dishDTO);
                dishDTO.setFlavors(dishFlavorMapper.getByDishId(dish.getId()));
                result.add(dishDTO);
            }
        }
        return result;
    }


    /**
     * 根据版本号校验后写入Redis缓存
     *
     * @return true：允许结束重建流程
     *         false：版本发生变化，需要重新查询
     */
    private boolean writeDishCache(
            String key,
            String versionKey,
            String expectedVersion,
            List<DishDTO> result) {

        Duration ttl = result.isEmpty()
                ? getEmptyCacheTTL()
                : getDishCacheTTL();

        try {

            String json = jsonMapper.writeValueAsString(result);

            Long writeResult = redisCircuitBreaker.executeSupplier(
                    () -> stringRedisTemplate.execute(
                            WRITE_DISH_CACHE_SCRIPT,
                            Arrays.asList(versionKey, key),
                            expectedVersion,
                            json,
                            String.valueOf(ttl.getSeconds())
                    )
            );

            if (Long.valueOf(1).equals(writeResult)) {

                log.info(
                        "菜品缓存写入成功，key={}，version={}，TTL={}秒",
                        key,
                        expectedVersion,
                        ttl.getSeconds()
                );

                return true;
            }

            log.warn(
                    "菜品缓存版本已变化，拒绝旧数据写入，key={}，expectedVersion={}",
                    key,
                    expectedVersion
            );

            return false;

        } catch (JacksonException e) {

            log.warn("菜品缓存序列化失败，key={}", key, e);

            return true;

        } catch (DataAccessException | CallNotPermittedException e) {

            log.warn("Redis缓存写入失败或熔断，key={}", key, e);

            // Redis不可用时不阻止当前请求返回数据库结果
            return true;
        }
    }


    private void releaseDishLock(String lockKey, String lockValue) {

        try {
            redisCircuitBreaker.executeSupplier(
                    () -> stringRedisTemplate.execute(
                            UNLOCK_SCRIPT,
                            Collections.singletonList(lockKey),
                            lockValue
                    )
            );

        } catch (DataAccessException | CallNotPermittedException e) {
            log.warn("Redis锁释放失败，key={}", lockKey, e);
        }
    }

    /**
     * 事务提交后清理菜品分类缓存
     *
     * 支持同时清理多个分类
     */

    /**
     * 清理指定分类的两级缓存，并通知其他实例。
     *
     * @param categoryId 菜品分类ID
     * @param stage       清理阶段，用于日志观察
     */

    private void evictDishCacheOnce(Long categoryId, String stage) {

        String key = DISH_CACHE_PREFIX + categoryId;

        String versionKey =
                DISH_CACHE_VERSION_PREFIX + categoryId;

        try {

            // 1. 先执行 Redis 版本递增和缓存删除
            // 2. 再递增本地版本并清理 Caffeine
            dishLocalCacheManager.invalidateAfter(
                    categoryId,
                    () -> {

                        Long newVersion = stringRedisTemplate.execute(
                                INVALIDATE_DISH_CACHE_SCRIPT,
                                Arrays.asList(versionKey, key)
                        );

                        log.info(
                                "{}：Redis缓存失效完成，key={}，newVersion={}",
                                stage,
                                key,
                                newVersion
                        );
                    }
            );

            log.info(
                    "{}：本地缓存版本递增并清理成功，categoryId={}",
                    stage,
                    categoryId
            );

        } catch (DataAccessException e) {

            // 即使 Redis 删除失败，invalidateAfter 的 finally
            // 仍然会清理当前 JVM 的 Caffeine 缓存
            log.error(
                    "{}：Redis缓存失效失败，key={}",
                    stage,
                    key,
                    e
            );
        }

        // 3. 通知其他实例清理 Caffeine
        try {

            cacheInvalidationPublisher
                    .publishCategoryInvalidation(categoryId);

            log.info(
                    "{}：已尝试发布缓存失效通知，categoryId={}",
                    stage,
                    categoryId
            );

        } catch (RuntimeException e) {

            log.error(
                    "{}：发布缓存失效通知异常，categoryId={}",
                    stage,
                    categoryId,
                    e
            );
        }
    }



    /**
     * 事务提交后清理缓存，并执行延迟二次失效。
     */
    private void evictDishCacheAfterCommit(Long... categoryIds) {

        // 1. 去重，过滤非法分类ID
        Set<Long> categoryIdSet = new LinkedHashSet<>();

        for (Long categoryId : categoryIds) {
            if (categoryId != null && categoryId > 0) {
                categoryIdSet.add(categoryId);
            }
        }

        if (categoryIdSet.isEmpty()) {
            return;
        }

        /*
         * 缓存失效事件必须在业务事务中写入。
         * 如果保存事件失败，让当前业务事务回滚。
         */
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {

            throw new IllegalStateException(
                    "缓存失效事件必须在有效的业务事务中保存"
            );
        }

// 为每个受影响的菜品分类保存一条失效事件
        for (Long categoryId : categoryIdSet) {

            CacheInvalidationEvent event =
                    new CacheInvalidationEvent();

            event.setEventType("DISH_INVALIDATE");
            event.setCategoryId(categoryId);

            // 通过 MyBatis 写入 MySQL
            int rows = cacheInvalidationEventMapper.insert(event);

            // 插入失败时抛出异常，触发事务回滚
            if (rows != 1 || event.getId() == null) {

                throw new IllegalStateException(
                        "缓存失效事件保存失败，categoryId=" + categoryId
                );
            }

            log.info(
                    "缓存失效事件已写入当前事务，eventId={}，categoryId={}",
                    event.getId(),
                    categoryId
            );
        }


        // 2. 定义事务提交后的失效任务
        Runnable evictionTask = () -> {

            // 第一次：立即清理缓存
            for (Long categoryId : categoryIdSet) {

                try {
                    evictDishCacheOnce(categoryId, "第一次失效");

                } catch (Exception e) {

                    log.error(
                            "第一次缓存失效异常，categoryId={}",
                            categoryId,
                            e
                    );
                }
            }

            // 3. 安排第二次：延迟清理缓存
            try {

                Instant executeTime = Instant.now().plusMillis(
                        CACHE_SECOND_EVICT_DELAY_MS
                );

                cacheEvictionTaskScheduler.schedule(() -> {

                    for (Long categoryId : categoryIdSet) {

                        try {
                            evictDishCacheOnce(categoryId, "第二次失效");
                        } catch (Exception e) {
                            log.error(
                                    "第二次缓存失效任务异常，categoryId={}",
                                    categoryId,
                                    e
                            );
                        }
                    }

                }, executeTime);

                log.info(
                        "已安排延迟二次失效，categoryIds={}，delay={}ms",
                        categoryIdSet,
                        CACHE_SECOND_EVICT_DELAY_MS
                );

            } catch (RuntimeException e) {

                log.error(
                        "延迟缓存失效任务提交失败，categoryIds={}",
                        categoryIdSet,
                        e
                );
            }
        };

        // 4. 有事务：提交成功后执行
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            evictionTask.run();
                        }
                    }
            );

        } else {

            // 无事务时直接执行
            evictionTask.run();
        }
    }




    /**
     * 获取正常菜品缓存随机过期时间
     * 范围：480～720秒
     */
    private Duration getDishCacheTTL() {
        long seconds = ThreadLocalRandom.current()
                .nextLong(480, 721);

        return Duration.ofSeconds(seconds);
    }

    /**
     * 获取空结果缓存随机过期时间
     * 范围：45～75秒
     */
    private Duration getEmptyCacheTTL() {
        long seconds = ThreadLocalRandom.current()
                .nextLong(45, 76);

        return Duration.ofSeconds(seconds);
    }
}
