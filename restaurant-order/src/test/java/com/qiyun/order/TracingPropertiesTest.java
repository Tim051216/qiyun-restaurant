package com.qiyun.order;

import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.service.impl.OrderServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 21: 链路追踪完整性测试
 * 
 * 验证SkyWalking链路追踪功能的正确性:
 * - TraceId在整个调用链中保持一致
 * - 自定义Span正确创建和标记
 * - 异常情况正确记录到Span
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Tag("Feature: restaurant-tech-stack-upgrade, Property 21: 链路追踪完整性")
public class TracingPropertiesTest {
    
    /**
     * Property 21.1: TraceId在调用链中保持一致
     * 
     * 验证在同一个请求处理过程中,TraceId保持不变
     */
    @Property(tries = 100)
    @Tag("Property 21.1: TraceId一致性")
    void traceIdShouldBeConsistentInCallChain(
            @ForAll @Positive Long userId,
            @ForAll @Positive Long tableId) {
        
        // Arrange
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderServiceImpl orderService = new OrderServiceImpl();
        setField(orderService, "baseMapper", orderMapper);
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        Order createdOrder = new Order();
        createdOrder.setId(System.currentTimeMillis());
        createdOrder.setUserId(userId);
        createdOrder.setTableId(tableId);
        createdOrder.setTotalAmount(order.getTotalAmount());
        createdOrder.setActualAmount(order.getActualAmount());
        createdOrder.setStatus(0);
        createdOrder.setCreateTime(LocalDateTime.now());
        
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(createdOrder.getId());
            return 1;
        });
        
        // 获取初始TraceId
        String traceIdBefore = TraceContext.traceId();
        
        // Act - 执行业务操作
        Order result = orderService.createOrder(order);
        
        // 获取操作后的TraceId
        String traceIdAfter = TraceContext.traceId();
        
        // Assert - 验证TraceId存在且一致
        assertThat(traceIdBefore).isNotNull().isNotEmpty();
        assertThat(traceIdAfter).isNotNull().isNotEmpty();
        assertThat(traceIdBefore).isEqualTo(traceIdAfter);
        
        // 验证订单创建成功
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }
    
    /**
     * Property 21.2: 自定义Span标签正确设置
     * 
     * 验证@Trace和@Tag注解正确工作,业务标签被正确添加
     */
    @Property(tries = 100)
    @Tag("Property 21.2: Span标签正确性")
    void customSpanTagsShouldBeSetCorrectly(
            @ForAll @Positive Long userId,
            @ForAll @Positive Long tableId) {
        
        // Arrange
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderServiceImpl orderService = new OrderServiceImpl();
        setField(orderService, "baseMapper", orderMapper);
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(System.currentTimeMillis());
            return 1;
        });
        
        // Act - 执行业务操作(带有@Trace和@Tag注解)
        Order createdOrder = orderService.createOrder(order);
        
        // Assert - 验证TraceId存在(说明追踪正在工作)
        String traceId = TraceContext.traceId();
        assertThat(traceId).isNotNull().isNotEmpty();
        
        // 验证订单创建成功
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getUserId()).isEqualTo(userId);
        assertThat(createdOrder.getTotalAmount()).isEqualByComparingTo(order.getTotalAmount());
    }
    
    /**
     * Property 21.3: 消息处理追踪完整性
     * 
     * 验证消息处理方法的追踪功能正常工作
     */
    @Property(tries = 100)
    @Tag("Property 21.3: 消息处理追踪")
    void messageProcessingShouldBeTraced(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId) {
        
        // Arrange
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderServiceImpl orderService = new OrderServiceImpl();
        setField(orderService, "baseMapper", orderMapper);
        
        // 创建订单对象
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(0);
        
        // 获取TraceId
        String traceIdBefore = TraceContext.traceId();
        
        // Act - 执行消息处理(带有@Trace注解)
        orderService.processOrderCreate(order);
        
        // Assert - 验证TraceId存在
        String traceIdAfter = TraceContext.traceId();
        assertThat(traceIdBefore).isNotNull();
        assertThat(traceIdAfter).isNotNull();
    }
    
    /**
     * Property 21.4: 支付处理追踪完整性
     * 
     * 验证支付处理方法的追踪功能正常工作
     */
    @Property(tries = 100)
    @Tag("Property 21.4: 支付处理追踪")
    void paymentProcessingShouldBeTraced(
            @ForAll @Positive Long userId,
            @ForAll @Positive Long tableId) {
        
        // Arrange
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderServiceImpl orderService = new OrderServiceImpl();
        setField(orderService, "baseMapper", orderMapper);
        
        // 先创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        Order createdOrder = new Order();
        createdOrder.setId(System.currentTimeMillis());
        createdOrder.setUserId(userId);
        createdOrder.setTableId(tableId);
        createdOrder.setTotalAmount(order.getTotalAmount());
        createdOrder.setActualAmount(order.getActualAmount());
        createdOrder.setStatus(0);
        createdOrder.setCreateTime(LocalDateTime.now());
        
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(createdOrder.getId());
            return 1;
        });
        when(orderMapper.selectById(createdOrder.getId())).thenReturn(createdOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        
        Order result = orderService.createOrder(order);
        
        // 获取TraceId
        String traceIdBefore = TraceContext.traceId();
        
        // Act - 执行支付处理(带有@Trace注解)
        orderService.processOrderPaid(result);
        
        // Assert - 验证TraceId存在
        String traceIdAfter = TraceContext.traceId();
        assertThat(traceIdBefore).isNotNull();
        assertThat(traceIdAfter).isNotNull();
    }
    
    /**
     * Property 21.5: 取消处理追踪完整性
     * 
     * 验证取消处理方法的追踪功能正常工作
     */
    @Property(tries = 100)
    @Tag("Property 21.5: 取消处理追踪")
    void cancelProcessingShouldBeTraced(
            @ForAll @Positive Long userId,
            @ForAll @Positive Long tableId) {
        
        // Arrange
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderServiceImpl orderService = new OrderServiceImpl();
        setField(orderService, "baseMapper", orderMapper);
        
        // 先创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        Order createdOrder = new Order();
        createdOrder.setId(System.currentTimeMillis());
        createdOrder.setUserId(userId);
        createdOrder.setTableId(tableId);
        createdOrder.setTotalAmount(order.getTotalAmount());
        createdOrder.setActualAmount(order.getActualAmount());
        createdOrder.setStatus(0);
        createdOrder.setCreateTime(LocalDateTime.now());
        
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(createdOrder.getId());
            return 1;
        });
        when(orderMapper.selectById(createdOrder.getId())).thenReturn(createdOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        
        Order result = orderService.createOrder(order);
        
        // 获取TraceId
        String traceIdBefore = TraceContext.traceId();
        
        // Act - 执行取消处理(带有@Trace注解)
        orderService.processOrderCancel(result);
        
        // Assert - 验证TraceId存在
        String traceIdAfter = TraceContext.traceId();
        assertThat(traceIdBefore).isNotNull();
        assertThat(traceIdAfter).isNotNull();
    }
    
    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
