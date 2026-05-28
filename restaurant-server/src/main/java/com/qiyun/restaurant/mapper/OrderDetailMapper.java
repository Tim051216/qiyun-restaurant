package com.qiyun.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.restaurant.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细Mapper
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
