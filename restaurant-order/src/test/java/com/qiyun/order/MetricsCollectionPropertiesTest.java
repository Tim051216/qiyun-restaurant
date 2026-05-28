package com.qiyun.order;

import com.qiyun.order.common.Result;
import com.qiyun.order.controller.OrderController;
import com.qiyun.order.entity.Order;
import com.qiyun.order.metrics.OrderMetrics;
import com.qiyun.order.mq.OrderMessageProducer;
import com.qiyun.order.service.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.junit.jupiter.api.Tag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 20: 指标采集完整性
 * 
 * For any 运行中的服务，监控系统应该能够采集到CPU、内存、JVM、QPS、响应时间等关键指标
 * 
 * Validates: Requirements 5.1
 */
@Tag("Feature: restaurant-tech-stack-upgrade, Property 20: 指标采集完整性")
public class MetricsCollectionPropertiesTest {
    
    /**
     * Property 20.1: 订单创建指标采集
     * 
     * 验证订单创建时能够正确记录指标
     */
    @Property(tries = 100)
    void shouldCollectOrderCreateMetrics(
        @ForAll @LongRange(min = 1, max = 1000000) long userId,
        @ForAll @LongRange(min = 1, max = 100) long tableId
    ) {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderMetrics orderMetrics = new OrderMetrics(registry, rabbitTemplate);
        
        OrderService orderService = mock(OrderService.class);
        OrderMessageProducer messageProducer = mock(OrderMessageProducer.class);
        
        OrderController controller = new OrderController();
        setField(controller, "orderService", orderService);
        setField(controller, "messageProducer", messageProducer);
        setField(controller, "orderMetrics", orderMetrics);
        
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        
        Order createdOrder = new Order();
        createdOrder.setId(System.currentTimeMillis());
        createdOrder.setUserId(userId);
        createdOrder.setTableId(tableId);
        createdOrder.setTotalAmount(order.getTotalAmount());
        createdOrder.setActualAmount(order.getActualAmount());
        createdOrder.setStatus(0);
        createdOrder.setCreateTime(LocalDateTime.now());
        
        when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);
        when(messageProducer.sendOrderCreateMessage(any(Order.class))).thenReturn("msg-" + System.currentTimeMillis());
        
        double beforeCount = orderMetrics.getOrderCreateTotal();
        
        // Act
        Result<Order> result = controller.createOrder(order);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        
        // 验证订单创建计数器增加
        double afterCount = orderMetrics.getOrderCreateTotal();
        assertThat(afterCount).isEqualTo(beforeCount + 1);
        
        // 验证指标可以从注册表中获取
        assertThat(registry.find("order.create.total").counter()).isNotNull();
        assertThat(registry.find("order.process.duration").timer()).isNotNull();
    }
    
    /**
     * Property 20.2: 订单支付指标采集
     * 
     * 验证订单支付时能够正确记录指标
     */
    @Property(tries = 100)
    void shouldCollectOrderPaidMetrics(
        @ForAll @LongRange(min = 1, max = 1000000) long orderId
    ) {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderMetrics orderMetrics = new OrderMetrics(registry, rabbitTemplate);
        
        OrderService orderService = mock(OrderService.class);
        OrderMessageProducer messageProducer = mock(OrderMessageProducer.class);
        
        OrderController controller = new OrderController();
        setField(controller, "orderService", orderService);
        setField(controller, "messageProducer", messageProducer);
        setField(controller, "orderMetrics", orderMetrics);
        
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setTableId(10L);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        when(orderService.getById(orderId)).thenReturn(order);
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        when(messageProducer.sendOrderPaidMessage(any(Order.class))).thenReturn("msg-" + System.currentTimeMillis());
        
        double beforeCount = orderMetrics.getOrderPaidTotal();
        
        // Act
        Result<Order> result = controller.payOrder(orderId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        
        // 验证订单支付计数器增加
        double afterCount = orderMetrics.getOrderPaidTotal();
        assertThat(afterCount).isEqualTo(beforeCount + 1);
        
        // 验证指标可以从注册表中获取
        assertThat(registry.find("order.paid.total").counter()).isNotNull();
    }
    
    /**
     * Property 20.3: 订单取消指标采集
     * 
     * 验证订单取消时能够正确记录指标
     */
    @Property(tries = 100)
    void shouldCollectOrderCancelMetrics(
        @ForAll @LongRange(min = 1, max = 1000000) long orderId
    ) {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderMetrics orderMetrics = new OrderMetrics(registry, rabbitTemplate);
        
        OrderService orderService = mock(OrderService.class);
        OrderMessageProducer messageProducer = mock(OrderMessageProducer.class);
        
        OrderController controller = new OrderController();
        setField(controller, "orderService", orderService);
        setField(controller, "messageProducer", messageProducer);
        setField(controller, "orderMetrics", orderMetrics);
        
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setTableId(10L);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        when(orderService.getById(orderId)).thenReturn(order);
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        when(messageProducer.sendOrderCancelMessage(any(Order.class))).thenReturn("msg-" + System.currentTimeMillis());
        
        double beforeCount = orderMetrics.getOrderCancelTotal();
        
        // Act
        Result<Order> result = controller.cancelOrder(orderId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        
        // 验证订单取消计数器增加
        double afterCount = orderMetrics.getOrderCancelTotal();
        assertThat(afterCount).isEqualTo(beforeCount + 1);
        
        // 验证指标可以从注册表中获取
        assertThat(registry.find("order.cancel.total").counter()).isNotNull();
    }
    
    /**
     * Property 20.4: 处理耗时指标采集
     * 
     * 验证订单处理耗时能够正确记录
     */
    @Property(tries = 100)
    void shouldCollectProcessDurationMetrics(
        @ForAll @LongRange(min = 1, max = 1000000) long userId,
        @ForAll @LongRange(min = 1, max = 100) long tableId
    ) {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderMetrics orderMetrics = new OrderMetrics(registry, rabbitTemplate);
        
        OrderService orderService = mock(OrderService.class);
        OrderMessageProducer messageProducer = mock(OrderMessageProducer.class);
        
        OrderController controller = new OrderController();
        setField(controller, "orderService", orderService);
        setField(controller, "messageProducer", messageProducer);
        setField(controller, "orderMetrics", orderMetrics);
        
        Order order = new Order();
        order.setUserId(userId);
        order.setTableId(tableId);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        
        Order createdOrder = new Order();
        createdOrder.setId(System.currentTimeMillis());
        createdOrder.setUserId(userId);
        createdOrder.setTableId(tableId);
        createdOrder.setTotalAmount(order.getTotalAmount());
        createdOrder.setActualAmount(order.getActualAmount());
        createdOrder.setStatus(0);
        createdOrder.setCreateTime(LocalDateTime.now());
        
        when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);
        when(messageProducer.sendOrderCreateMessage(any(Order.class))).thenReturn("msg-" + System.currentTimeMillis());
        
        // Act
        Result<Order> result = controller.createOrder(order);
        
        // Assert
        assertThat(result).isNotNull();
        
        // 验证处理耗时计时器记录了数据
        assertThat(registry.find("order.process.duration").timer()).isNotNull();
        assertThat(registry.find("order.process.duration").timer().count()).isGreaterThan(0);
    }
    
    /**
     * Property 20.5: 多次操作指标累积
     * 
     * 验证多次操作后指标能够正确累积
     */
    @Property(tries = 50)
    void shouldAccumulateMetricsOverMultipleOperations(
        @ForAll @IntRange(min = 2, max = 10) int operationCount
    ) {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderMetrics orderMetrics = new OrderMetrics(registry, rabbitTemplate);
        
        OrderService orderService = mock(OrderService.class);
        OrderMessageProducer messageProducer = mock(OrderMessageProducer.class);
        
        OrderController controller = new OrderController();
        setField(controller, "orderService", orderService);
        setField(controller, "messageProducer", messageProducer);
        setField(controller, "orderMetrics", orderMetrics);
        
        when(messageProducer.sendOrderCreateMessage(any(Order.class))).thenReturn("msg-" + System.currentTimeMillis());
        
        double initialCount = orderMetrics.getOrderCreateTotal();
        
        // Act - 执行多次订单创建
        for (int i = 0; i < operationCount; i++) {
            Order order = new Order();
            order.setUserId(1000L + i);
            order.setTableId(10L + i);
            order.setTotalAmount(BigDecimal.valueOf(100));
            order.setActualAmount(BigDecimal.valueOf(100));
            
            Order createdOrder = new Order();
            createdOrder.setId(System.currentTimeMillis() + i);
            createdOrder.setUserId(order.getUserId());
            createdOrder.setTableId(order.getTableId());
            createdOrder.setTotalAmount(order.getTotalAmount());
            createdOrder.setActualAmount(order.getActualAmount());
            createdOrder.setStatus(0);
            createdOrder.setCreateTime(LocalDateTime.now());
            
            when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);
            
            controller.createOrder(order);
        }
        
        // Assert
        double finalCount = orderMetrics.getOrderCreateTotal();
        assertThat(finalCount).isEqualTo(initialCount + operationCount);
        
        // 验证计时器也记录了多次操作
        assertThat(registry.find("order.process.duration").timer().count()).isGreaterThanOrEqualTo(operationCount);
    }
    
    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
