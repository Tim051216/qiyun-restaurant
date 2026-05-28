package com.qiyun.order.mapper;

import com.qiyun.order.dto.RealtimeOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderQueryMapper {
    
    /**
     * 查询实时订单列表（最近的订单）
     */
    @Select("SELECT o.id, o.order_no as orderNo, o.table_no as tableNo, o.amount, o.status, o.create_time as createTime " +
            "FROM orders o " +
            "WHERE o.deleted = 0 " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<RealtimeOrderResponse> selectRealtimeOrders(int offset, int size);
    
    /**
     * 条件查询订单列表
     */
    @Select("<script>" +
            "SELECT o.id, o.order_no as orderNo, o.table_no as tableNo, o.amount, o.status, o.create_time as createTime " +
            "FROM orders o " +
            "WHERE o.deleted = 0 " +
            "<if test='status != null'> AND o.status = #{status} </if>" +
            "<if test='tableId != null'> AND o.table_id = #{tableId} </if>" +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<RealtimeOrderResponse> selectOrdersWithCondition(
            @Param("offset") int offset, 
            @Param("size") int size, 
            @Param("status") Integer status, 
            @Param("tableId") Long tableId);
    
    /**
     * 统计订单总数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE deleted = 0")
    Long countOrders();
    
    /**
     * 条件统计订单总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM orders " +
            "WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='tableId != null'> AND table_id = #{tableId} </if>" +
            "</script>")
    Long countOrdersWithCondition(@Param("status") Integer status, @Param("tableId") Long tableId);
}
