
package com.example.skyserver.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 缓存失效事件实体类
 */
@Data
public class CacheInvalidationEvent {

    /**
     * 事件唯一ID
     */
    private Long id;

    /**
     * 事件类型
     * 例如：DISH_INVALIDATE
     */
    private String eventType;

    /**
     * 需要失效的菜品分类ID
     */
    private Long categoryId;

    /**
     * 事件创建时间
     */
    private LocalDateTime createdAt;
}
