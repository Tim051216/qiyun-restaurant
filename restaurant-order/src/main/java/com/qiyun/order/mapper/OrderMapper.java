package com.qiyun.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
