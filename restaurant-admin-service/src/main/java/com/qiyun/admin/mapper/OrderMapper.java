package com.qiyun.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.admin.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(*) FROM orders WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    Integer countTodayOrders();

    @Select("SELECT COUNT(*) FROM orders WHERE create_time BETWEEN #{start} AND #{end} AND deleted = 0")
    Integer countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("""
            SELECT COALESCE(SUM(amount), 0)
            FROM orders
            WHERE DATE(create_time) = CURDATE()
              AND status != 'cancelled'
              AND deleted = 0
            """)
    java.math.BigDecimal sumTodaySales();

    @Select("""
            SELECT id, order_no, member_id, table_id, table_no, diner_count, amount, status, remark, create_time, update_time, deleted
            FROM orders
            WHERE create_time BETWEEN #{start} AND #{end}
              AND deleted = 0
            """)
    List<Order> selectOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM orders WHERE status IN ('pending', 'cooking') AND deleted = 0")
    Integer countOnlineOrders();
}
