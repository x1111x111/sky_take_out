
package com.example.skyserver.cache;

import com.example.skyserver.dto.DishDTO;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class DishLocalCacheManager {

    // 使用项目中已经存在的 Caffeine 缓存 Bean
    private final Cache<Long, List<DishDTO>> dishLocalCache;

    // 每个分类对应一个本地缓存状态
    private final ConcurrentMap<Long, LocalCacheState> states =
            new ConcurrentHashMap<>();

    /**
     * 获取当前缓存内容和版本快照
     */
    public Snapshot getSnapshot(Long categoryId) {

        LocalCacheState state = getState(categoryId);

        synchronized (state) {

            return new Snapshot(
                    state.generation,
                    dishLocalCache.getIfPresent(categoryId)
            );
        }
    }

    /**
     * 版本没有变化时才允许写入 Caffeine
     */
    public boolean putIfGenerationUnchanged(
            Long categoryId,
            long expectedGeneration,
            List<DishDTO> value) {

        LocalCacheState state = getState(categoryId);

        synchronized (state) {

            // 版本发生变化，拒绝旧数据写入
            if (state.generation != expectedGeneration) {
                return false;
            }

            dishLocalCache.put(categoryId, value);

            return true;
        }
    }

    /**
     * 收到失效通知后：
     * 本地版本递增 + 删除 Caffeine
     */
    public void invalidate(Long categoryId) {

        LocalCacheState state = getState(categoryId);

        synchronized (state) {

            state.generation++;

            dishLocalCache.invalidate(categoryId);
        }
    }

    /**
     * 先执行分布式缓存失效，再递增本地版本并清理 Caffeine。
     *
     * 同一个分类的本地快照读取和缓存写入会等待此操作完成。
     */
    public void invalidateAfter(
            Long categoryId,
            Runnable beforeLocalInvalidate) {

        LocalCacheState state = getState(categoryId);

        synchronized (state) {

            try {

                beforeLocalInvalidate.run();

            } finally {

                state.generation++;

                dishLocalCache.invalidate(categoryId);
            }
        }
    }

    /**
     * 同一分类的本地缓存重建只允许一个线程执行。
     * 避免改成手动缓存后失去原来 Caffeine get() 的单飞效果。
     */
    public <T> T withLoadLock(
            Long categoryId,
            Supplier<T> action) {

        LocalCacheState state = getState(categoryId);

        state.loadLock.lock();

        try {

            return action.get();

        } finally {

            state.loadLock.unlock();
        }
    }

    private LocalCacheState getState(Long categoryId) {

        return states.computeIfAbsent(
                categoryId,
                id -> new LocalCacheState()
        );
    }

    /**
     * 缓存快照
     */
    public static class Snapshot {

        private final long generation;

        private final List<DishDTO> cachedValue;

        public Snapshot(
                long generation,
                List<DishDTO> cachedValue) {

            this.generation = generation;
            this.cachedValue = cachedValue;
        }

        public long getGeneration() {
            return generation;
        }

        public List<DishDTO> getCachedValue() {
            return cachedValue;
        }
    }

    /**
     * 每个分类独立维护状态
     */
    private static class LocalCacheState {

        // 本地缓存代际版本
        private long generation = 0L;

        // 同一分类的缓存重建锁
        private final ReentrantLock loadLock =
                new ReentrantLock();
    }
}
