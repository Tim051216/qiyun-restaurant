package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.vo.OrderVO;

/**
 * 订单Service
 */
public interface OrderService {
    
    /**
     * 分页查询订单
     */
    Page<OrderVO> getOrderPage(Integer page, Integer size, String orderNo, String tableNo, String status, String startDate, String endDate);
    
    /**
     * 根据ID获取订单详情
     */
    OrderVO getOrderById(Long id);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long id, String status);
    
    /**
     * 获取实时订单（待接单和制作中的订单）
     */
    Page<OrderVO> getRealtimeOrders(Integer page, Integer size);
}
