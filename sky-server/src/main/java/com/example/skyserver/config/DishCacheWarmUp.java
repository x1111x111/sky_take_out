package com.example.skyserver.config;

import com.example.skyserver.dto.DishDTO;
import com.example.skyserver.entity.Category;
import com.example.skyserver.mapper.CategoryMapper;
import com.example.skyserver.service.DishService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishCacheWarmUp {

    /**
     * 菜品分类类型
     * 1：菜品分类
     * 2：套餐分类
     */
    private static final Integer DISH_CATEGORY_TYPE = 1;

    private final CategoryMapper categoryMapper;

    private final DishService dishService;

    /**
     * Spring Boot 启动完成后执行缓存预热
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {

        log.info("========== 开始菜品缓存预热 ==========");

        List<Category> categories;

        // 1. 查询所有启用的菜品分类
        try {
            categories = categoryMapper.listByType(
                    DISH_CATEGORY_TYPE
            );
        } catch (Exception e) {
            log.error("查询菜品分类失败，本次缓存预热跳过", e);
            return;
        }

        if (categories == null || categories.isEmpty()) {
            log.warn("没有找到启用的菜品分类");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // 2. 遍历分类，逐个预热
        for (Category category : categories) {

            if (category == null || category.getId() == null) {
                failCount++;
                continue;
            }

            try {

                // 3. 调用已有业务方法
                // 自动经过 Caffeine + Redis + MySQL 缓存流程
                List<DishDTO> dishes =
                        dishService.listByCategoryId(
                                category.getId()
                        );

                successCount++;

                log.info(
                        "分类缓存预热成功：分类ID={}，分类名称={}，菜品数量={}",
                        category.getId(),
                        category.getName(),
                        dishes == null ? 0 : dishes.size()
                );

            } catch (Exception e) {

                failCount++;

                log.error(
                        "分类缓存预热失败：分类ID={}，分类名称={}",
                        category.getId(),
                        category.getName(),
                        e
                );
            }
        }

        // 4. 输出预热统计
        log.info(
                "========== 菜品缓存预热结束：总数={}，成功={}，失败={} ==========",
                categories.size(),
                successCount,
                failCount
        );
    }
}