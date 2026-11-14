
package com.example.skyserver.mapper;

import com.example.skyserver.entity.CacheInvalidationEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface CacheInvalidationEventMapper {

    /**
     * 插入一条缓存失效事件
     *
     * @param event 缓存失效事件
     * @return 影响的数据库行数
     */
    int insert(CacheInvalidationEvent event);

    /**
     * 清理已经被所有预期实例确认的历史事件
     *
     * @param instanceIds 预期实例ID列表
     * @param expectedCount 预期实例数量
     * @param retentionDays 保留天数
     */
    @Delete("""
    <script>
    DELETE e
    FROM cache_invalidation_event e
    WHERE e.created_at &lt;
          TIMESTAMPADD(
              DAY,
              -#{retentionDays},
              NOW(3)
          )
    AND (
        SELECT COUNT(*)
        FROM cache_invalidation_ack a
        WHERE a.event_id = e.id
          AND a.instance_id IN
          <foreach collection="instanceIds"
                   item="instanceId"
                   open="("
                   separator=","
                   close=")">
              #{instanceId}
          </foreach>
    ) = #{expectedCount}
    </script>
    """)
    int deleteFullyAcknowledged(
            @Param("instanceIds") List<String> instanceIds,
            @Param("expectedCount") int expectedCount,
            @Param("retentionDays") int retentionDays
    );

}
