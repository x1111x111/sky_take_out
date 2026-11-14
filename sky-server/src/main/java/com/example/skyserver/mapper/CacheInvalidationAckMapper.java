
package com.example.skyserver.mapper;

import com.example.skyserver.entity.CacheInvalidationEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CacheInvalidationAckMapper {

    /**
     * 查询某个服务器尚未确认的失效事件
     */
    @Select("""
        SELECT e.id, e.event_type, e.category_id, e.created_at
        FROM cache_invalidation_event e
        WHERE e.event_type = 'DISH_INVALIDATE'
          AND NOT EXISTS (
              SELECT 1
              FROM cache_invalidation_ack a
              WHERE a.event_id = e.id
                AND a.instance_id = #{instanceId}
          )
        ORDER BY e.id ASC
        LIMIT #{batchSize}
        """)
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "event_type", property = "eventType"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<CacheInvalidationEvent> findUnacked(
            @Param("instanceId") String instanceId,
            @Param("batchSize") int batchSize
    );

    /**
     * 消费完成后写入 ACK
     * 重复确认同一事件时保持幂等
     */
    @Insert("""
        INSERT INTO cache_invalidation_ack
            (event_id, instance_id)
        VALUES
            (#{eventId}, #{instanceId})
        ON DUPLICATE KEY UPDATE
            processed_at = processed_at
        """)
    int acknowledge(
            @Param("eventId") Long eventId,
            @Param("instanceId") String instanceId
    );
}
