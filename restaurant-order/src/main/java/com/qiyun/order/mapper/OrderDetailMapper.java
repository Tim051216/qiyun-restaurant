package com.qiyun.order.mapper;

import com.qiyun.order.dto.RealtimeOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    
    /**
     * 查询订单明细
     */
    @Select("SELECT dish_name as dishName, quantity, price " +
            "FROM order_detail " +
            "WHERE order_id = #{orderId}")
    List<RealtimeOrderResponse.OrderDetailDTO> selectByOrderId(Long orderId);
}
