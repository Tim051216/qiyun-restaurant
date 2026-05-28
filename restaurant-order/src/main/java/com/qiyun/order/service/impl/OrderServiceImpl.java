package com.qiyun.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Trace
    @Tag(key = "order.userId", value = "arg[0].userId")
    @Tag(key = "order.amount", value = "arg[0].totalAmount")
    public Order createOrder(Order order) {
        // 获取TraceId用于日志关联
        String traceId = TraceContext.traceId();
        log.info("创建订单: userId={}, amount={}, traceId={}", 
            order.getUserId(), order.getTotalAmount(), traceId);
        
        // 添加自定义标签
        ActiveSpan.tag("business.type", "order-create");
        ActiveSpan.tag("user.id", String.valueOf(order.getUserId()));
        
        try {
            // 保存订单
            this.save(order);
            
            // 添加成功日志到Span
            ActiveSpan.info("订单创建成功: orderId=" + order.getId());
            log.info("订单创建成功: orderId={}, traceId={}", order.getId(), traceId);
            
            return order;
        } catch (Exception e) {
            // 记录异常到Span
            ActiveSpan.error(e);
            log.error("订单创建失败: userId={}, traceId={}", order.getUserId(), traceId, e);
            throw e;
        }
    }
    
    @Override
    @Trace
    @Tag(key = "order.id", value = "arg[0].id")
    public void processOrderCreate(Order order) {
        String traceId = TraceContext.traceId();
        log.info("处理订单创建消息: orderId={}, traceId={}", order.getId(), traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-message-processing");
        ActiveSpan.tag("message.type", "order-create");
        ActiveSpan.tag("order.id", String.valueOf(order.getId()));
        
        // 订单创建后的业务逻辑
        // 例如：发送通知、更新库存、记录日志等
        
        // TODO: 实现具体业务逻辑
        // 1. 发送订单创建通知给用户
        // 2. 通知商家有新订单
        // 3. 更新相关统计数据
        
        ActiveSpan.info("订单创建消息处理完成");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Trace
    @Tag(key = "order.id", value = "arg[0].id")
    public void processOrderPaid(Order order) {
        String traceId = TraceContext.traceId();
        log.info("处理订单支付消息: orderId={}, traceId={}", order.getId(), traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-payment-processing");
        ActiveSpan.tag("message.type", "order-paid");
        ActiveSpan.tag("order.id", String.valueOf(order.getId()));
        
        try {
            // 更新订单状态为已支付
            Order existingOrder = this.getById(order.getId());
            if (existingOrder != null) {
                existingOrder.setStatus(1); // 1=已支付
                this.updateById(existingOrder);
                log.info("订单状态更新为已支付: orderId={}, traceId={}", order.getId(), traceId);
                
                ActiveSpan.info("订单支付状态更新成功");
            } else {
                ActiveSpan.error("订单不存在: orderId=" + order.getId());
            }
            
            // TODO: 实现具体业务逻辑
            // 1. 发送支付成功通知
            // 2. 触发发货流程
            // 3. 更新会员积分
            // 4. 更新销售统计
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单支付处理失败: orderId={}, traceId={}", order.getId(), traceId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Trace
    @Tag(key = "order.id", value = "arg[0].id")
    public void processOrderCancel(Order order) {
        String traceId = TraceContext.traceId();
        log.info("处理订单取消消息: orderId={}, traceId={}", order.getId(), traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-cancel-processing");
        ActiveSpan.tag("message.type", "order-cancel");
        ActiveSpan.tag("order.id", String.valueOf(order.getId()));
        
        try {
            // 更新订单状态为已取消
            Order existingOrder = this.getById(order.getId());
            if (existingOrder != null) {
                existingOrder.setStatus(4); // 4=已取消
                this.updateById(existingOrder);
                log.info("订单状态更新为已取消: orderId={}, traceId={}", order.getId(), traceId);
                
                ActiveSpan.info("订单取消状态更新成功");
            } else {
                ActiveSpan.error("订单不存在: orderId=" + order.getId());
            }
            
            // TODO: 实现具体业务逻辑
            // 1. 恢复库存
            // 2. 退还优惠券
            // 3. 处理退款（如果已支付）
            // 4. 发送取消通知
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单取消处理失败: orderId={}, traceId={}", order.getId(), traceId, e);
            throw e;
        }
    }
    
    @Override
    @Trace
    @Tag(key = "query.userId", value = "arg[0]")
    @Tag(key = "query.orderId", value = "arg[1]")
    public Order getOrderByUserIdAndOrderId(Long userId, Long orderId) {
        String traceId = TraceContext.traceId();
        log.info("根据用户ID和订单ID查询订单: userId={}, orderId={}, traceId={}", userId, orderId, traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-query-single-shard");
        ActiveSpan.tag("query.type", "single-shard");
        ActiveSpan.tag("shard.key", "user_id");
        
        try {
            // 使用分片键user_id进行查询，ShardingSphere会自动路由到正确的分片
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                   .eq(Order::getId, orderId);
            
            Order order = this.getOne(wrapper);
            
            if (order != null) {
                ActiveSpan.info("订单查询成功: orderId=" + orderId);
                log.info("订单查询成功: orderId={}, traceId={}", orderId, traceId);
            } else {
                ActiveSpan.info("订单不存在: orderId=" + orderId);
                log.info("订单不存在: orderId={}, traceId={}", orderId, traceId);
            }
            
            return order;
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单查询失败: userId={}, orderId={}, traceId={}", userId, orderId, traceId, e);
            throw e;
        }
    }
    
    @Override
    @Trace
    @Tag(key = "query.startTime", value = "arg[0]")
    @Tag(key = "query.endTime", value = "arg[1]")
    public List<Order> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end) {
        String traceId = TraceContext.traceId();
        log.info("根据时间范围查询订单: start={}, end={}, traceId={}", start, end, traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-query-cross-shard");
        ActiveSpan.tag("query.type", "cross-shard");
        ActiveSpan.tag("shard.key", "create_time");
        
        try {
            // 跨分片查询：ShardingSphere会自动查询所有相关分片并聚合结果
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Order::getCreateTime, start, end)
                   .orderByDesc(Order::getCreateTime);
            
            List<Order> orders = this.list(wrapper);
            
            ActiveSpan.info("跨分片查询完成: count=" + orders.size());
            log.info("跨分片查询完成: count={}, traceId={}", orders.size(), traceId);
            
            return orders;
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("跨分片查询失败: start={}, end={}, traceId={}", start, end, traceId, e);
            throw e;
        }
    }
    
    @Override
    @Trace
    @Tag(key = "query.userId", value = "arg[0]")
    public List<Order> getOrdersByUserId(Long userId) {
        String traceId = TraceContext.traceId();
        log.info("根据用户ID查询订单列表: userId={}, traceId={}", userId, traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-query-single-shard");
        ActiveSpan.tag("query.type", "single-shard");
        ActiveSpan.tag("shard.key", "user_id");
        
        try {
            // 使用分片键user_id进行查询，ShardingSphere会自动路由到正确的分片
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                   .orderByDesc(Order::getCreateTime);
            
            List<Order> orders = this.list(wrapper);
            
            ActiveSpan.info("用户订单查询完成: userId=" + userId + ", count=" + orders.size());
            log.info("用户订单查询完成: userId={}, count={}, traceId={}", userId, orders.size(), traceId);
            
            return orders;
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("用户订单查询失败: userId={}, traceId={}", userId, traceId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Trace
    @Tag(key = "order.id", value = "arg[0]")
    public boolean closeTimeoutOrder(Long orderId) {
        String traceId = TraceContext.traceId();
        log.info("关闭超时订单: orderId={}, traceId={}", orderId, traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-timeout-close");
        ActiveSpan.tag("order.id", String.valueOf(orderId));
        
        try {
            // 查询订单
            Order order = this.getById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}, traceId={}", orderId, traceId);
                ActiveSpan.info("订单不存在: orderId=" + orderId);
                return false;
            }
            
            // 检查订单状态，只关闭未支付的订单（status=0）
            if (order.getStatus() != 0) {
                log.info("订单状态不是未支付，无需关闭: orderId={}, status={}, traceId={}", 
                    orderId, order.getStatus(), traceId);
                ActiveSpan.info("订单状态不是未支付: status=" + order.getStatus());
                return false;
            }
            
            // 使用乐观锁更新订单状态为已关闭（status=5）
            order.setStatus(5); // 5=已关闭
            boolean success = this.updateById(order);
            
            if (success) {
                log.info("订单关闭成功: orderId={}, traceId={}", orderId, traceId);
                ActiveSpan.info("订单关闭成功: orderId=" + orderId);
                
                // TODO: 释放库存
                // 1. 调用菜品服务恢复库存
                // 2. 如果使用了优惠券，恢复优惠券
                // 3. 发送订单关闭通知
                
            } else {
                log.warn("订单关闭失败（乐观锁冲突）: orderId={}, traceId={}", orderId, traceId);
                ActiveSpan.info("订单关闭失败（乐观锁冲突）: orderId=" + orderId);
            }
            
            return success;
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单关闭异常: orderId={}, traceId={}", orderId, traceId, e);
            throw e;
        }
    }
    
    @Override
    @Trace
    @Tag(key = "query.timeoutMinutes", value = "arg[0]")
    public List<Order> getTimeoutUnpaidOrders(int timeoutMinutes) {
        String traceId = TraceContext.traceId();
        log.info("查询超时未支付订单: timeoutMinutes={}, traceId={}", timeoutMinutes, traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-timeout-query");
        ActiveSpan.tag("timeout.minutes", String.valueOf(timeoutMinutes));
        
        try {
            // 计算超时时间点
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
            
            // 查询未支付且创建时间早于超时时间的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, 0) // 0=未支付
                   .lt(Order::getCreateTime, timeoutTime)
                   .orderByAsc(Order::getCreateTime);
            
            List<Order> orders = this.list(wrapper);
            
            log.info("查询到超时未支付订单: count={}, traceId={}", orders.size(), traceId);
            ActiveSpan.info("查询到超时未支付订单: count=" + orders.size());
            
            return orders;
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("查询超时订单失败: timeoutMinutes={}, traceId={}", timeoutMinutes, traceId, e);
            throw e;
        }
    }
}
