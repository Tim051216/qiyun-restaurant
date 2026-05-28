package com.qiyun.order.integration;

import com.qiyun.order.OrderApplication;
import com.qiyun.order.client.DishServiceClient;
import com.qiyun.order.common.Result;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.service.OrderService;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 订单服务集成测试
 * 
 * 测试覆盖：
 * - 订单服务与数据库的集成
 * - 订单服务与Feign客户端的集成
 * - 完整的订单业务流程
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("订单服务集成测试")
class OrderServiceIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @MockBean
    private DishServiceClient dishServiceClient;
    
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
        
        // Mock菜品服务响应
        when(dishServiceClient.getDishByIds(anyList()))
            .thenReturn(Result.success(Arrays.asList()));
    }
    
    @Test
    @DisplayName("集成测试 - 创建订单并保存到数据库")
    void testCreateOrderIntegration() {
        // When
        Order createdOrder = orderService.createOrder(testOrder);
        
        // Then
        assertNotNull(createdOrder);
        assertNotNull(createdOrder.getId());
        assertEquals(testOrder.getUserId(), createdOrder.getUserId());
        assertEquals(testOrder.getTotalAmount(), createdOrder.getTotalAmount());
        
        // 验证数据库中存在该订单
        Order dbOrder = orderMapper.selectById(createdOrder.getId());
        assertNotNull(dbOrder);
        assertEquals(createdOrder.getId(), dbOrder.getId());
    }
    
    @Test
    @DisplayName("集成测试 - 查询订单")
    void testGetOrderIntegration() {
        // Given - 先创建订单
        Order createdOrder = orderService.createOrder(testOrder);
        
        // When
        Order foundOrder = orderService.getById(createdOrder.getId());
        
        // Then
        assertNotNull(foundOrder);
        assertEquals(createdOrder.getId(), foundOrder.getId());
        assertEquals(createdOrder.getUserId(), foundOrder.getUserId());
    }
    
    @Test
    @DisplayName("集成测试 - 更新订单状态")
    void testUpdateOrderStatusIntegration() {
        // Given - 先创建订单
        Order createdOrder = orderService.createOrder(testOrder);
        
        // When - 更新订单状态为已支付
        createdOrder.setStatus(1);
        boolean updated = orderService.updateById(createdOrder);
        
        // Then
        assertTrue(updated);
        
        // 验证数据库中的状态已更新
        Order dbOrder = orderMapper.selectById(createdOrder.getId());
        assertEquals(1, dbOrder.getStatus());
    }
    
    @Test
    @DisplayName("集成测试 - 根据用户ID查询订单列表")
    void testGetOrdersByUserIdIntegration() {
        // Given - 创建多个订单
        Order order1 = new Order();
        order1.setUserId(100L);
        order1.setTableId(5L);
        order1.setTotalAmount(new BigDecimal("50.00"));
        order1.setActualAmount(new BigDecimal("50.00"));
        order1.setStatus(0);
        orderService.createOrder(order1);
        
        Order order2 = new Order();
        order2.setUserId(100L);
        order2.setTableId(6L);
        order2.setTotalAmount(new BigDecimal("80.00"));
        order2.setActualAmount(new BigDecimal("80.00"));
        order2.setStatus(0);
        orderService.createOrder(order2);
        
        // When
        List<Order> orders = orderService.getOrdersByUserId(100L);
        
        // Then
        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getUserId().equals(100L)));
    }
    
    @Test
    @DisplayName("集成测试 - 根据时间范围查询订单")
    void testGetOrdersByTimeRangeIntegration() {
        // Given - 创建订单
        orderService.createOrder(testOrder);
        
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        // When
        List<Order> orders = orderService.getOrdersByTimeRange(start, end);
        
        // Then
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
        assertTrue(orders.stream()
            .allMatch(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end)));
    }
    
    @Test
    @DisplayName("集成测试 - 订单完整生命周期")
    void testOrderLifecycleIntegration() {
        // 1. 创建订单
        Order createdOrder = orderService.createOrder(testOrder);
        assertNotNull(createdOrder.getId());
        assertEquals(0, createdOrder.getStatus()); // 待支付
        
        // 2. 支付订单
        createdOrder.setStatus(1);
        orderService.updateById(createdOrder);
        Order paidOrder = orderService.getById(createdOrder.getId());
        assertEquals(1, paidOrder.getStatus()); // 已支付
        
        // 3. 处理订单
        paidOrder.setStatus(2);
        orderService.updateById(paidOrder);
        Order processingOrder = orderService.getById(createdOrder.getId());
        assertEquals(2, processingOrder.getStatus()); // 处理中
        
        // 4. 完成订单
        processingOrder.setStatus(3);
        orderService.updateById(processingOrder);
        Order completedOrder = orderService.getById(createdOrder.getId());
        assertEquals(3, completedOrder.getStatus()); // 已完成
    }
    
    @Test
    @DisplayName("集成测试 - 订单取消流程")
    void testOrderCancelIntegration() {
        // Given - 创建订单
        Order createdOrder = orderService.createOrder(testOrder);
        assertEquals(0, createdOrder.getStatus()); // 待支付
        
        // When - 取消订单
        createdOrder.setStatus(4);
        orderService.updateById(createdOrder);
        
        // Then
        Order cancelledOrder = orderService.getById(createdOrder.getId());
        assertEquals(4, cancelledOrder.getStatus()); // 已取消
    }
    
    @Test
    @DisplayName("集成测试 - 并发创建订单")
    void testConcurrentOrderCreationIntegration() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
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
                orderService.createOrder(order);
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - 验证所有订单都创建成功
        List<Order> allOrders = orderMapper.selectList(null);
        assertTrue(allOrders.size() >= threadCount);
    }
}
