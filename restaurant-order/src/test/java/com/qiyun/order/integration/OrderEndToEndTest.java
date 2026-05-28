package com.qiyun.order.integration;

import com.qiyun.order.OrderApplication;
import com.qiyun.order.common.Result;
import com.qiyun.order.controller.OrderController;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.mq.OrderMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单端到端集成测试
 * 
 * 测试完整的订单业务流程：
 * 1. 用户创建订单
 * 2. 订单保存到数据库
 * 3. 发送消息到MQ
 * 4. 用户支付订单
 * 5. 更新订单状态
 * 6. 发送支付成功消息
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("订单端到端集成测试")
class OrderEndToEndTest {
    
    @Autowired
    private OrderController orderController;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @MockBean
    private OrderMessageProducer messageProducer;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderMapper.delete(null);
        
        testOrder = new Order();
        testOrder.setUserId(100L);
        testOrder.setTableId(5L);
        testOrder.setTotalAmount(new BigDecimal("99.80"));
        testOrder.setActualAmount(new BigDecimal("99.80"));
        testOrder.setStatus(0);
        testOrder.setCreateTime(LocalDateTime.now());
        
        // Mock消息发送
        when(messageProducer.sendOrderCreateMessage(any(Order.class)))
            .thenReturn("msg-create-123");
        when(messageProducer.sendOrderPaidMessage(any(Order.class)))
            .thenReturn("msg-paid-456");
        when(messageProducer.sendOrderCancelMessage(any(Order.class)))
            .thenReturn("msg-cancel-789");
    }
    
    @Test
    @DisplayName("端到端 - 完整的订单创建流程")
    void testCompleteOrderCreationFlow() {
        // Step 1: 用户创建订单
        Result<Order> createResult = orderController.createOrder(testOrder);
        
        // 验证创建成功
        assertNotNull(createResult);
        assertEquals(200, createResult.getCode());
        assertNotNull(createResult.getData());
        assertNotNull(createResult.getData().getId());
        
        Long orderId = createResult.getData().getId();
        
        // Step 2: 验证订单保存到数据库
        Order dbOrder = orderMapper.selectById(orderId);
        assertNotNull(dbOrder);
        assertEquals(testOrder.getUserId(), dbOrder.getUserId());
        assertEquals(testOrder.getTotalAmount(), dbOrder.getTotalAmount());
        assertEquals(0, dbOrder.getStatus()); // 待支付
        
        // Step 3: 验证消息已发送
        verify(messageProducer, times(1)).sendOrderCreateMessage(any(Order.class));
    }
    
    @Test
    @DisplayName("端到端 - 完整的订单支付流程")
    void testCompleteOrderPaymentFlow() {
        // Step 1: 创建订单
        Result<Order> createResult = orderController.createOrder(testOrder);
        Long orderId = createResult.getData().getId();
        
        // Step 2: 用户支付订单
        Result<Order> payResult = orderController.payOrder(orderId);
        
        // 验证支付成功
        assertNotNull(payResult);
        assertEquals(200, payResult.getCode());
        assertEquals(1, payResult.getData().getStatus()); // 已支付
        
        // Step 3: 验证数据库状态已更新
        Order dbOrder = orderMapper.selectById(orderId);
        assertEquals(1, dbOrder.getStatus());
        
        // Step 4: 验证支付消息已发送
        verify(messageProducer, times(1)).sendOrderPaidMessage(any(Order.class));
    }
    
    @Test
    @DisplayName("端到端 - 完整的订单取消流程")
    void testCompleteOrderCancellationFlow() {
        // Step 1: 创建订单
        Result<Order> createResult = orderController.createOrder(testOrder);
        Long orderId = createResult.getData().getId();
        
        // Step 2: 用户取消订单
        Result<Order> cancelResult = orderController.cancelOrder(orderId);
        
        // 验证取消成功
        assertNotNull(cancelResult);
        assertEquals(200, cancelResult.getCode());
        assertEquals(4, cancelResult.getData().getStatus()); // 已取消
        
        // Step 3: 验证数据库状态已更新
        Order dbOrder = orderMapper.selectById(orderId);
        assertEquals(4, dbOrder.getStatus());
        
        // Step 4: 验证取消消息已发送
        verify(messageProducer, times(1)).sendOrderCancelMessage(any(Order.class));
    }
    
    @Test
    @DisplayName("端到端 - 订单完整生命周期")
    void testCompleteOrderLifecycle() {
        // Step 1: 创建订单
        Result<Order> createResult = orderController.createOrder(testOrder);
        Long orderId = createResult.getData().getId();
        assertEquals(0, createResult.getData().getStatus()); // 待支付
        
        // Step 2: 支付订单
        Result<Order> payResult = orderController.payOrder(orderId);
        assertEquals(1, payResult.getData().getStatus()); // 已支付
        
        // Step 3: 查询订单
        Result<Order> getResult = orderController.getOrder(orderId);
        assertNotNull(getResult);
        assertEquals(200, getResult.getCode());
        assertEquals(orderId, getResult.getData().getId());
        assertEquals(1, getResult.getData().getStatus());
        
        // Step 4: 验证所有消息都已发送
        verify(messageProducer, times(1)).sendOrderCreateMessage(any(Order.class));
        verify(messageProducer, times(1)).sendOrderPaidMessage(any(Order.class));
    }
    
    @Test
    @DisplayName("端到端 - 订单查询不存在的订单")
    void testQueryNonExistentOrder() {
        // When
        Result<Order> result = orderController.getOrder(9999L);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }
    
    @Test
    @DisplayName("端到端 - 支付不存在的订单")
    void testPayNonExistentOrder() {
        // When
        Result<Order> result = orderController.payOrder(9999L);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }
    
    @Test
    @DisplayName("端到端 - 取消不存在的订单")
    void testCancelNonExistentOrder() {
        // When
        Result<Order> result = orderController.cancelOrder(9999L);
        
        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }
    
    @Test
    @DisplayName("端到端 - 消息发送失败不影响订单创建")
    void testOrderCreationWithMessageFailure() {
        // Given - Mock消息发送失败
        when(messageProducer.sendOrderCreateMessage(any(Order.class)))
            .thenThrow(new RuntimeException("MQ连接失败"));
        
        // When
        Result<Order> result = orderController.createOrder(testOrder);
        
        // Then - 订单仍然创建成功
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getId());
        
        // 验证订单已保存到数据库
        Order dbOrder = orderMapper.selectById(result.getData().getId());
        assertNotNull(dbOrder);
    }
    
    @Test
    @DisplayName("端到端 - 并发创建订单")
    void testConcurrentOrderCreation() throws InterruptedException {
        // Given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        Result<Order>[] results = new Result[threadCount];
        
        // When - 并发创建订单
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Order order = new Order();
                order.setUserId(100L + index);
                order.setTableId((long) (5 + index));
                order.setTotalAmount(new BigDecimal("50.00"));
                order.setActualAmount(new BigDecimal("50.00"));
                order.setStatus(0);
                results[index] = orderController.createOrder(order);
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - 验证所有订单都创建成功
        for (Result<Order> result : results) {
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData().getId());
        }
        
        // 验证数据库中有所有订单
        int count = orderMapper.selectList(null).size();
        assertTrue(count >= threadCount);
    }
}
