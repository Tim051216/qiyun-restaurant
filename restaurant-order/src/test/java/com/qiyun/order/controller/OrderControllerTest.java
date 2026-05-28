package com.qiyun.order.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.qiyun.order.client.DishServiceClient;
import com.qiyun.order.common.Result;
import com.qiyun.order.entity.Order;
import com.qiyun.order.metrics.OrderMetrics;
import com.qiyun.order.mq.OrderMessageProducer;
import com.qiyun.order.service.OrderService;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单控制器单元测试
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单控制器单元测试")
class OrderControllerTest {
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private OrderMessageProducer messageProducer;
    
    @Mock
    private OrderMetrics orderMetrics;
    
    @Mock
    private DishServiceClient dishServiceClient;
    
    @InjectMocks
    private OrderController orderController;
    
    private Order testOrder;
    private Timer.Sample mockSample;
    
    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1001L);
        testOrder.setUserId(100L);
        testOrder.setTableId(5L);
        testOrder.setTotalAmount(new BigDecimal("99.80"));
        testOrder.setActualAmount(new BigDecimal("99.80"));
        testOrder.setStatus(0);
        testOrder.setCreateTime(LocalDateTime.now());
        
        mockSample = mock(Timer.Sample.class);
        // 使用lenient避免UnnecessaryStubbingException
        lenient().when(orderMetrics.startOrderProcessTimer()).thenReturn(mockSample);
    }
    
    @Test
    @DisplayName("创建订单 - 成功场景")
    void testCreateOrder_Success() {
        // Given
        when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);
        when(messageProducer.sendOrderCreateMessage(any(Order.class))).thenReturn("msg-123");
        
        // When
        Result<Order> result = orderController.createOrder(testOrder);
        
        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(testOrder.getId(), result.getData().getId());
        
        verify(orderService, times(1)).createOrder(any(Order.class));
        verify(orderMetrics, times(1)).recordOrderCreate();
        verify(messageProducer, times(1)).sendOrderCreateMessage(any(Order.class));
    }
    
    @Test
    @DisplayName("创建订单 - 消息发送失败不影响订单创建")
    void testCreateOrder_MessageSendFailed() {
        // Given
        when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);
        when(messageProducer.sendOrderCreateMessage(any(Order.class)))
            .thenThrow(new RuntimeException("MQ连接失败"));
        
        // When
        Result<Order> result = orderController.createOrder(testOrder);
        
        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(testOrder.getId(), result.getData().getId());
    }
    
    @Test
    @DisplayName("查询订单 - 成功场景")
    void testGetOrder_Success() {
        // Given
        when(orderService.getById(1001L)).thenReturn(testOrder);
        
        // When
        Result<Order> result = orderController.getOrder(1001L);
        
        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(testOrder.getId(), result.getData().getId());
    }
    
    @Test
    @DisplayName("支付订单 - 成功场景")
    void testPayOrder_Success() {
        // Given
        when(orderService.getById(1001L)).thenReturn(testOrder);
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        when(messageProducer.sendOrderPaidMessage(any(Order.class))).thenReturn("msg-456");
        
        // When
        Result<Order> result = orderController.payOrder(1001L);
        
        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getStatus());
    }
    
    @Test
    @DisplayName("支付订单 - 订单不存在")
    void testPayOrder_OrderNotFound() {
        // Given
        when(orderService.getById(9999L)).thenReturn(null);
        
        // When
        Result<Order> result = orderController.payOrder(9999L);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }
    
    @Test
    @DisplayName("取消订单 - 成功场景")
    void testCancelOrder_Success() {
        // Given
        when(orderService.getById(1001L)).thenReturn(testOrder);
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        when(messageProducer.sendOrderCancelMessage(any(Order.class))).thenReturn("msg-789");
        
        // When
        Result<Order> result = orderController.cancelOrder(1001L);
        
        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(4, result.getData().getStatus());
    }
    
    @Test
    @DisplayName("限流处理 - 创建订单")
    void testHandleCreateOrderBlock() {
        // Given
        BlockException blockException = new FlowException("限流");
        
        // When
        Result<Order> result = orderController.handleCreateOrderBlock(testOrder, blockException);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后再试", result.getMessage());
    }
    
    @Test
    @DisplayName("降级处理 - 创建订单")
    void testHandleCreateOrderFallback() {
        // Given
        Throwable throwable = new RuntimeException("服务异常");
        
        // When
        Result<Order> result = orderController.handleCreateOrderFallback(testOrder, throwable);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("服务暂时不可用，请稍后再试", result.getMessage());
    }
}
