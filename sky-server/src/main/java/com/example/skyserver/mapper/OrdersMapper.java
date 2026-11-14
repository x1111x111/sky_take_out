package com.example.skyserver.mapper;

import com.example.skyserver.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    Long count(
            @Param("number") String number,
            @Param("phone") String phone,
            @Param("status") Integer status
    );

    List<Orders> pageQuery(
            @Param("number") String number,
            @Param("phone") String phone,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );
    Orders getById(Long id);
    int updateStatus(
            @Param("id") Long id,
            @Param("expectedStatus") Integer expectedStatus,
            @Param("status") Integer status
    );
    int rejection(
            @Param("id") Long id,
            @Param("rejectionReason") String rejectionReason
    );
    Integer countByStatus(Integer status);
    BigDecimal sumByDate(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime
    );
    Integer countByDate(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") Integer status
    );
    void insert(Orders orders);
    Long countByUser(
            @Param("userId") Long userId,
            @Param("status") Integer status
    );

    List<Orders> pageQueryByUser(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );
    int cancel(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("cancelReason") String cancelReason
    );
    /**
     * 本地模拟支付：满足订单归属和待支付条件才能更新
     *
     * @return 成功更新的行数
     */
    @Update("""
    UPDATE orders
    SET status = 2,
        pay_status = 1,
        checkout_time = now()
    WHERE id = #{orderId}
      AND user_id = #{userId}
      AND status = 1
      AND pay_status = 0
    """)
    int mockPaySuccess(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );
}
