package com.example.skyserver.service.impl;

import com.example.skyserver.dto.SetmealDTO;
import com.example.skyserver.dto.SetmealPageQueryDTO;
import com.example.skyserver.entity.Setmeal;
import com.example.skyserver.entity.SetmealDish;
import com.example.skyserver.mapper.SetmealDishMapper;
import com.example.skyserver.mapper.SetmealMapper;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.service.SetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.skyserver.exception.BusinessException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JsonMapper jsonMapper;
    private  static final String SETMEAL_CACHE_PREFIX="sky:setmeal";
    private static final Duration SETMEAL_CACHE_TTL=Duration.ofMinutes(10);

    @Override
    @Transactional
    public void save(SetmealDTO dto) {

        // 保存套餐基本信息
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(dto, setmeal);

        setmealMapper.insert(setmeal);


        // 获取套餐中的菜品
        List<SetmealDish> setmealDishes =
                dto.getSetmealDishes();

        if (setmealDishes != null) {

            for (SetmealDish setmealDish : setmealDishes) {

                // 设置刚生成的套餐id
                setmealDish.setSetmealId(setmeal.getId());

                setmealDishMapper.insert(setmealDish);
            }
        }
        // 事务提交后清理所属分类缓存
        evictSetmealCacheAfterCommit(dto.getCategoryId());
    }

    @Override
    public PageResult<Setmeal> pageQuery(
            SetmealPageQueryDTO dto) {

        // 计算分页起始位置
        Integer offset =
                (dto.getPage() - 1)
                        * dto.getPageSize();

        // 查询符合条件的总数量
        Long total = setmealMapper.count(
                dto.getName(),
                dto.getCategoryId(),
                dto.getStatus()
        );

        // 查询当前页
        List<Setmeal> records =
                setmealMapper.pageQuery(
                        dto.getName(),
                        dto.getCategoryId(),
                        dto.getStatus(),
                        offset,
                        dto.getPageSize()
                );

        return new PageResult<>(
                total,
                records
        );
    }

    @Override
    public SetmealDTO getById(Long id) {

        // 查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);

        if (setmeal == null) {
            return null;
        }

        // 查询套餐中的菜品
        List<SetmealDish> setmealDishes =
                setmealDishMapper.getBySetmealId(id);

        // 组装DTO
        SetmealDTO setmealDTO = new SetmealDTO();

        BeanUtils.copyProperties(setmeal, setmealDTO);

        setmealDTO.setSetmealDishes(setmealDishes);

        return setmealDTO;
    }


    /**
     * 用户端：根据分类查询起售套餐（Redis缓存）
     */
    @Override
    public List<Setmeal> listByCategoryId(Long categoryId) {

        // 1. 校验参数
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException("套餐分类ID不合法");
        }

        // 2. 生成缓存Key
        String key = SETMEAL_CACHE_PREFIX + categoryId;

        // 3. 查询Redis
        try {
            String cacheJson =
                    stringRedisTemplate.opsForValue().get(key);

            if (cacheJson != null) {

                try {
                    List<Setmeal> cachedList =
                            jsonMapper.readValue(
                                    cacheJson,
                                    new TypeReference<List<Setmeal>>() {}
                            );

                    if (cachedList != null) {
                        log.info("套餐缓存命中 HIT，key={}", key);
                        return cachedList;
                    }

                } catch (JacksonException e) {
                    log.warn("套餐缓存JSON解析失败，key={}", key, e);
                }
            }

        } catch (DataAccessException e) {
            log.warn("Redis读取失败，改查MySQL，key={}", key, e);
        }

        // 4. 缓存未命中，查询MySQL
        log.info("套餐缓存未命中 MISS，查询MySQL，key={}", key);

        List<Setmeal> result =
                setmealMapper.listByCategoryId(categoryId);

        // 5. 写入Redis
        try {
            String json = jsonMapper.writeValueAsString(result);

            stringRedisTemplate.opsForValue()
                    .set(key, json, SETMEAL_CACHE_TTL);

            log.info("套餐列表写入Redis，key={}，数量={}",
                    key, result.size());

        } catch (DataAccessException | JacksonException e) {
            log.warn("套餐缓存写入失败，key={}", key, e);
        }

        // 6. 返回结果
        return result;
    }


    @Override
    @Transactional
    public void update(SetmealDTO dto) {
        // 获取修改前的套餐信息
        Setmeal oldSetmeal = setmealMapper.getById(dto.getId());
        // 1. 修改套餐基本信息
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(dto, setmeal);

        setmealMapper.update(setmeal);


        // 2. 删除原来的套餐菜品关系
        setmealDishMapper.deleteBySetmealId(dto.getId());


        // 3. 保存最新套餐菜品
        List<SetmealDish> setmealDishes =
                dto.getSetmealDishes();

        if (setmealDishes != null) {

            for (SetmealDish setmealDish : setmealDishes) {

                // 设置当前套餐id
                setmealDish.setSetmealId(dto.getId());

                setmealDishMapper.insert(setmealDish);
            }
        }
        // 同时清理原分类和新分类缓存
        evictSetmealCacheAfterCommit(
                oldSetmeal == null ? null : oldSetmeal.getCategoryId(),
                dto.getCategoryId()
        );
    }

    @Override
    public void updateStatus(Integer status, Long id) {

        // 获取原套餐分类
        Setmeal oldSetmeal = setmealMapper.getById(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("套餐状态不合法");
        }

        Setmeal setmeal = setmealMapper.getById(id);

        if (setmeal == null) {
            throw new RuntimeException("套餐不存在");
        }

        setmealMapper.updateStatus(status, id);
        // 清理所属分类缓存
        evictSetmealCacheAfterCommit(
                oldSetmeal == null ? null : oldSetmeal.getCategoryId()
        );
    }
    @Override
    @Transactional
    public void delete(Long id) {
        // 删除前获取原分类
        Setmeal oldSetmeal = setmealMapper.getById(id);
        // 1. 查询套餐是否存在
        Setmeal setmeal = setmealMapper.getById(id);

        if (setmeal == null) {
            throw new RuntimeException("套餐不存在");
        }

        // 2. 起售中的套餐禁止删除
        if (setmeal.getStatus() != null
                && setmeal.getStatus() == 1) {

            throw new RuntimeException(
                    "起售中的套餐不能删除，请先停售"
            );
        }

        // 3. 删除套餐与菜品之间的关联
        setmealDishMapper.deleteBySetmealId(id);

        // 4. 删除套餐本身
        setmealMapper.deleteById(id);
        // 清理原分类缓存
        evictSetmealCacheAfterCommit(
                oldSetmeal == null ? null : oldSetmeal.getCategoryId()
        );
    }

    /**
     * 事务提交后清理套餐分类缓存
     */
    private void evictSetmealCacheAfterCommit(Long... categoryIds) {

        // 1. 分类ID去重
        Set<Long> categoryIdSet = new LinkedHashSet<>();

        for (Long categoryId : categoryIds) {
            if (categoryId != null && categoryId > 0) {
                categoryIdSet.add(categoryId);
            }
        }

        if (categoryIdSet.isEmpty()) {
            return;
        }

        // 2. 定义缓存清理任务
        Runnable evictionTask = () -> {

            for (Long categoryId : categoryIdSet) {

                // 必须与查询缓存时使用的Key保持一致
                String key = SETMEAL_CACHE_PREFIX + categoryId;

                try {
                    Boolean deleted = stringRedisTemplate.delete(key);

                    log.info(
                            "套餐缓存清理，key={}，删除结果={}",
                            key,
                            deleted
                    );

                } catch (DataAccessException e) {

                    log.warn(
                            "套餐Redis缓存清理失败，key={}",
                            key,
                            e
                    );
                }
            }
        };

        // 3. 有事务时，在事务提交后清理
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

            // 没有事务时直接执行
            evictionTask.run();
        }
    }


}