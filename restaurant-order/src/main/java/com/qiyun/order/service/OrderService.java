package com.qiyun.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务接口
 * 
 * @author qiyun
 * @since 2026-02-08
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    Order createOrder(Order order);
    
    /**
     * 处理订单创建消息
     */
    void processOrderCreate(Order order);
    
    /**
     * 处理订单支付消息
     */
    void processOrderPaid(Order order);
    
    /**
     * 处理订单取消消息
     */
    void processOrderCancel(Order order);
    
    /**
     * 根据用户ID和订单ID查询订单（单分片查询）
     * 使用分片键user_id，可以精确路由到特定分片
     * 
     * @param userId 用户ID（分片键）
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getOrderByUserIdAndOrderId(Long userId, Long orderId);
    
    /**
     * 根据时间范围查询订单（跨分片查询）
     * 可能需要查询多个分片并聚合结果
     * 
     * @param start 开始时间
     * @param end 结束时间
     * @return 订单列表
     */
    List<Order> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据用户ID查询订单列表（单分片查询）
     * 使用分片键user_id，可以精确路由到特定分片
     * 
     * @param userId 用户ID（分片键）
     * @return 订单列表
     */
    List<Order> getOrdersByUserId(Long userId);
    
    /**
     * 关闭超时未支付订单
     * 使用乐观锁防止并发冲突
     * 
     * @param orderId 订单ID
     * @return 是否关闭成功
     */
    boolean closeTimeoutOrder(Long orderId);
    
    /**
     * 查询超时未支付订单列表
     * 
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时订单列表
     */
    List<Order> getTimeoutUnpaidOrders(int timeoutMinutes);
}
