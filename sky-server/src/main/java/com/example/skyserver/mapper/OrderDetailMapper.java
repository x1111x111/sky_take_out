package com.example.skyserver.mapper;

import com.example.skyserver.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    List<OrderDetail> getByOrderId(Long orderId);
    List<Map<String,Object>> getSalesTop10(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime
    );
    void insert(OrderDetail orderDetail);
}