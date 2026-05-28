package com.qiyun.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qiyun.order.client.DishServiceClient;
import com.qiyun.order.common.Result;
import com.qiyun.order.dto.PageResponse;
import com.qiyun.order.dto.RealtimeOrderResponse;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderDetailMapper;
import com.qiyun.order.mapper.OrderQueryMapper;
import com.qiyun.order.metrics.OrderMetrics;
import com.qiyun.order.mq.OrderMessageProducer;
import com.qiyun.order.service.OrderService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderMessageProducer messageProducer;
    
    @Autowired
    private OrderMetrics orderMetrics;
    
    @Autowired
    private DishServiceClient dishServiceClient;
    
    @Autowired
    private OrderQueryMapper orderQueryMapper;
    
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    
    /**
     * 获取实时订单列表
     */
    @GetMapping("/realtime")
    public Result<PageResponse<RealtimeOrderResponse>> getRealtimeOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        log.info("获取实时订单列表: page={}, size={}", page, size);
        
        // 从数据库查询真实数据
        int offset = (page - 1) * size;
        List<RealtimeOrderResponse> orders = orderQueryMapper.selectRealtimeOrders(offset, size);
        
        // 查询每个订单的明细
        for (RealtimeOrderResponse order : orders) {
            List<RealtimeOrderResponse.OrderDetailDTO> details = orderDetailMapper.selectByOrderId(order.getId());
            order.setDetails(details);
        }
        
        // 查询总数
        Long total = orderQueryMapper.countOrders();
        
        PageResponse<RealtimeOrderResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setRecords(orders);
        response.setCurrent(page);
        response.setSize(size);
        
        return Result.success(response);
    }
    
    /**
     * 分页查询订单列表
     */
    @GetMapping("/page")
    public Result<PageResponse<RealtimeOrderResponse>> pageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long tableId) {
        log.info("分页查询订单: page={}, pageSize={}, status={}, tableId={}", 
                page, pageSize, status, tableId);
        
        // 从数据库查询真实数据
        int offset = (page - 1) * pageSize;
        List<RealtimeOrderResponse> orders = orderQueryMapper.selectOrdersWithCondition(offset, pageSize, status, tableId);
        
        // 查询每个订单的明细
        for (RealtimeOrderResponse order : orders) {
            List<RealtimeOrderResponse.OrderDetailDTO> details = orderDetailMapper.selectByOrderId(order.getId());
            order.setDetails(details);
        }
        
        // 查询总数
        Long total = orderQueryMapper.countOrdersWithCondition(status, tableId);
        
        PageResponse<RealtimeOrderResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setRecords(orders);
        response.setCurrent(page);
        response.setSize(pageSize);
        
        return Result.success(response);
    }
    
    /**
     * 查询菜品（用于热点参数限流测试）
     * 
     * 使用热点参数限流保护热门菜品
     */
    @GetMapping("/dish/{dishId}")
    @SentinelResource(
        value = "getDish",
        blockHandler = "handleGetDishBlock",
        fallback = "handleGetDishFallback"
    )
    public Result<?> getDish(@PathVariable Long dishId) {
        log.info("查询菜品: dishId={}", dishId);
        return dishServiceClient.getDishById(dishId);
    }
    
    /**
     * 创建订单
     * 
     * 使用Sentinel进行限流和降级保护
     * 订单创建成功后发送消息到RabbitMQ
     */
    @PostMapping
    @SentinelResource(
        value = "orderCreate",
        blockHandler = "handleCreateOrderBlock",
        fallback = "handleCreateOrderFallback"
    )
    public Result<Order> createOrder(@RequestBody Order order) {
        log.info("接收创建订单请求: userId={}", order.getUserId());
        
        // 开始计时
        Timer.Sample sample = orderMetrics.startOrderProcessTimer();
        
        try {
            // 创建订单
            Order created = orderService.createOrder(order);
            
            // 记录订单创建指标
            orderMetrics.recordOrderCreate();
            
            // 发送订单创建消息
            try {
                String messageId = messageProducer.sendOrderCreateMessage(created);
                log.info("订单创建消息已发送: orderId={}, messageId={}", created.getId(), messageId);
            } catch (Exception e) {
                log.error("订单创建消息发送失败: orderId={}", created.getId(), e);
                // 消息发送失败不影响订单创建，可以通过补偿机制重试
            }
            
            return Result.success(created);
        } finally {
            // 停止计时
            orderMetrics.stopOrderProcessTimer(sample);
        }
    }
    
    /**
     * 查询订单
     */
    @GetMapping("/{id}")
    @SentinelResource(
        value = "orderQuery",
        blockHandler = "handleQueryOrderBlock",
        fallback = "handleQueryOrderFallback"
    )
    public Result<Order> getOrder(@PathVariable Long id) {
        log.info("查询订单: orderId={}", id);
        Order order = orderService.getById(id);
        return Result.success(order);
    }
    
    /**
     * 支付订单
     */
    @PutMapping("/{id}/pay")
    @SentinelResource(
        value = "orderPay",
        blockHandler = "handlePayOrderBlock",
        fallback = "handlePayOrderFallback"
    )
    public Result<Order> payOrder(@PathVariable Long id) {
        log.info("支付订单: orderId={}", id);
        
        // 开始计时
        Timer.Sample sample = orderMetrics.startOrderProcessTimer();
        
        try {
            // 更新订单状态
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            order.setStatus(1); // 1=已支付
            orderService.updateById(order);
            
            // 记录订单支付指标
            orderMetrics.recordOrderPaid();
            
            // 发送订单支付消息
            try {
                String messageId = messageProducer.sendOrderPaidMessage(order);
                log.info("订单支付消息已发送: orderId={}, messageId={}", order.getId(), messageId);
            } catch (Exception e) {
                log.error("订单支付消息发送失败: orderId={}", order.getId(), e);
            }
            
            return Result.success(order);
        } finally {
            // 停止计时
            orderMetrics.stopOrderProcessTimer(sample);
        }
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @SentinelResource(
        value = "orderCancel",
        blockHandler = "handleCancelOrderBlock",
        fallback = "handleCancelOrderFallback"
    )
    public Result<Order> cancelOrder(@PathVariable Long id) {
        log.info("取消订单: orderId={}", id);
        
        // 开始计时
        Timer.Sample sample = orderMetrics.startOrderProcessTimer();
        
        try {
            // 更新订单状态
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            order.setStatus(4); // 4=已取消
            orderService.updateById(order);
            
            // 记录订单取消指标
            orderMetrics.recordOrderCancel();
            
            // 发送订单取消消息
            try {
                String messageId = messageProducer.sendOrderCancelMessage(order);
                log.info("订单取消消息已发送: orderId={}, messageId={}", order.getId(), messageId);
            } catch (Exception e) {
                log.error("订单取消消息发送失败: orderId={}", order.getId(), e);
            }
            
            return Result.success(order);
        } finally {
            // 停止计时
            orderMetrics.stopOrderProcessTimer(sample);
        }
    }
    
    // ========== 限流处理方法 ==========
    
    /**
     * 创建订单限流处理
     */
    public Result<Order> handleCreateOrderBlock(Order order, BlockException ex) {
        log.warn("订单创建被限流: userId={}, exception={}", order.getUserId(), ex.getClass().getSimpleName());
        return Result.error("系统繁忙，请稍后再试");
    }
    
    /**
     * 查询订单限流处理
     */
    public Result<Order> handleQueryOrderBlock(Long id, BlockException ex) {
        log.warn("订单查询被限流: orderId={}, exception={}", id, ex.getClass().getSimpleName());
        return Result.error("系统繁忙，请稍后再试");
    }
    
    /**
     * 支付订单限流处理
     */
    public Result<Order> handlePayOrderBlock(Long id, BlockException ex) {
        log.warn("订单支付被限流: orderId={}, exception={}", id, ex.getClass().getSimpleName());
        return Result.error("系统繁忙，请稍后再试");
    }
    
    /**
     * 取消订单限流处理
     */
    public Result<Order> handleCancelOrderBlock(Long id, BlockException ex) {
        log.warn("订单取消被限流: orderId={}, exception={}", id, ex.getClass().getSimpleName());
        return Result.error("系统繁忙，请稍后再试");
    }
    
    /**
     * 查询菜品限流处理
     */
    public Result<?> handleGetDishBlock(Long dishId, BlockException ex) {
        log.warn("菜品查询被限流: dishId={}, exception={}", dishId, ex.getClass().getSimpleName());
        return Result.error("该菜品访问量过大，请稍后再试");
    }
    
    // ========== 降级处理方法 ==========
    
    /**
     * 创建订单降级处理
     */
    public Result<Order> handleCreateOrderFallback(Order order, Throwable ex) {
        log.error("订单创建异常降级: userId={}, exception={}", order.getUserId(), ex.getMessage(), ex);
        return Result.error("服务暂时不可用，请稍后再试");
    }
    
    /**
     * 查询订单降级处理
     */
    public Result<Order> handleQueryOrderFallback(Long id, Throwable ex) {
        log.error("订单查询异常降级: orderId={}, exception={}", id, ex.getMessage(), ex);
        return Result.error("服务暂时不可用，请稍后再试");
    }
    
    /**
     * 支付订单降级处理
     */
    public Result<Order> handlePayOrderFallback(Long id, Throwable ex) {
        log.error("订单支付异常降级: orderId={}, exception={}", id, ex.getMessage(), ex);
        return Result.error("服务暂时不可用，请稍后再试");
    }
    
    /**
     * 取消订单降级处理
     */
    public Result<Order> handleCancelOrderFallback(Long id, Throwable ex) {
        log.error("订单取消异常降级: orderId={}, exception={}", id, ex.getMessage(), ex);
        return Result.error("服务暂时不可用，请稍后再试");
    }
    
    /**
     * 查询菜品降级处理
     */
    public Result<?> handleGetDishFallback(Long dishId, Throwable ex) {
        log.error("菜品查询异常降级: dishId={}, exception={}", dishId, ex.getMessage(), ex);
        return Result.error("菜品服务暂时不可用，请稍后再试");
    }
}
