package com.qiyun.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 * 
 * 测试覆盖：
 * - 订单创建
 * - 订单查询
 * - 订单状态更新
 * - 异常处理
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceImplTest {
    
    @Mock
    private OrderMapper orderMapper;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        // 设置baseMapper，这是MyBatis Plus ServiceImpl需要的
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        
        testOrder = new Order();
        testOrder.setId(1001L);
        testOrder.setUserId(100L);
        testOrder.setTableId(5L);
        testOrder.setTotalAmount(new BigDecimal("99.80"));
        testOrder.setActualAmount(new BigDecimal("99.80"));
        testOrder.setStatus(0); // 0=待支付
        testOrder.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("创建订单 - 成功场景")
    void testCreateOrder_Success() {
        // Given
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        
        // When
        Order result = orderService.createOrder(testOrder);
        
        // Then
        assertNotNull(result);
        assertEquals(testOrder.getUserId(), result.getUserId());
        assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
        verify(orderMapper, times(1)).insert(any(Order.class));
    }
    
    @Test
    @DisplayName("创建订单 - 空订单对象")
    void testCreateOrder_NullOrder() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            orderService.createOrder(null);
        });
        
        verify(orderMapper, never()).insert(any(Order.class));
    }
    
    @Test
    @DisplayName("创建订单 - 数据库异常")
    void testCreateOrder_DatabaseException() {
        // Given
        when(orderMapper.insert(any(Order.class)))
            .thenThrow(new RuntimeException("数据库连接失败"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testOrder);
        });
        
        assertTrue(exception.getMessage().contains("数据库连接失败"));
        verify(orderMapper, times(1)).insert(any(Order.class));
    }
    
    @Test
    @DisplayName("处理订单创建消息 - 成功场景")
    void testProcessOrderCreate_Success() {
        // When
        assertDoesNotThrow(() -> {
            orderService.processOrderCreate(testOrder);
        });
        
        // Then - 验证方法执行无异常
    }
    
    @Test
    @DisplayName("处理订单支付消息 - 成功场景")
    void testProcessOrderPaid_Success() {
        // Given
        when(orderMapper.selectById(testOrder.getId())).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        
        // When
        assertDoesNotThrow(() -> {
            orderService.processOrderPaid(testOrder);
        });
        
        // Then
        verify(orderMapper, times(1)).selectById(testOrder.getId());
        verify(orderMapper, times(1)).updateById(any(Order.class));
    }
    
    @Test
    @DisplayName("处理订单支付消息 - 订单不存在")
    void testProcessOrderPaid_OrderNotFound() {
        // Given
        when(orderMapper.selectById(testOrder.getId())).thenReturn(null);
        
        // When
        assertDoesNotThrow(() -> {
            orderService.processOrderPaid(testOrder);
        });
        
        // Then
        verify(orderMapper, times(1)).selectById(testOrder.getId());
        verify(orderMapper, never()).updateById(any(Order.class));
    }
    
    @Test
    @DisplayName("处理订单取消消息 - 成功场景")
    void testProcessOrderCancel_Success() {
        // Given
        when(orderMapper.selectById(testOrder.getId())).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        
        // When
        assertDoesNotThrow(() -> {
            orderService.processOrderCancel(testOrder);
        });
        
        // Then
        verify(orderMapper, times(1)).selectById(testOrder.getId());
        verify(orderMapper, times(1)).updateById(any(Order.class));
    }
    
    @Test
    @DisplayName("根据用户ID和订单ID查询 - 成功场景")
    void testGetOrderByUserIdAndOrderId_Success() {
        // Given
        when(orderMapper.selectOne(any(), anyBoolean())).thenReturn(testOrder);
        
        // When
        Order result = orderService.getOrderByUserIdAndOrderId(100L, 1001L);
        
        // Then
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getUserId(), result.getUserId());
        verify(orderMapper, times(1)).selectOne(any(), anyBoolean());
    }
    
    @Test
    @DisplayName("根据用户ID和订单ID查询 - 订单不存在")
    void testGetOrderByUserIdAndOrderId_NotFound() {
        // Given
        when(orderMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        
        // When
        Order result = orderService.getOrderByUserIdAndOrderId(100L, 9999L);
        
        // Then
        assertNull(result);
        verify(orderMapper, times(1)).selectOne(any(), anyBoolean());
    }
    
    @Test
    @DisplayName("根据时间范围查询 - 成功场景")
    void testGetOrdersByTimeRange_Success() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderMapper.selectList(any())).thenReturn(expectedOrders);
        
        // When
        List<Order> result = orderService.getOrdersByTimeRange(start, end);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderMapper, times(1)).selectList(any());
    }
    
    @Test
    @DisplayName("根据时间范围查询 - 空结果")
    void testGetOrdersByTimeRange_EmptyResult() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(orderMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        List<Order> result = orderService.getOrdersByTimeRange(start, end);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderMapper, times(1)).selectList(any());
    }
    
    @Test
    @DisplayName("根据用户ID查询订单列表 - 成功场景")
    void testGetOrdersByUserId_Success() {
        // Given
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderMapper.selectList(any())).thenReturn(expectedOrders);
        
        // When
        List<Order> result = orderService.getOrdersByUserId(100L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getUserId(), result.get(0).getUserId());
        verify(orderMapper, times(1)).selectList(any());
    }
    
    @Test
    @DisplayName("根据用户ID查询订单列表 - 空结果")
    void testGetOrdersByUserId_EmptyResult() {
        // Given
        when(orderMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        List<Order> result = orderService.getOrdersByUserId(999L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderMapper, times(1)).selectList(any());
    }
    
    @Test
    @DisplayName("订单金额计算 - 边界值测试")
    void testOrderAmount_BoundaryValues() {
        // Given - 最小金额
        Order minOrder = new Order();
        minOrder.setTotalAmount(new BigDecimal("0.01"));
        
        // When & Then
        assertEquals(new BigDecimal("0.01"), minOrder.getTotalAmount());
        
        // Given - 大额订单
        Order largeOrder = new Order();
        largeOrder.setTotalAmount(new BigDecimal("99999.99"));
        
        // When & Then
        assertEquals(new BigDecimal("99999.99"), largeOrder.getTotalAmount());
    }
    
    @Test
    @DisplayName("订单状态转换 - 正常流程")
    void testOrderStatusTransition_NormalFlow() {
        // Given
        Order order = new Order();
        
        // When & Then - 待支付 -> 已支付
        order.setStatus(0);
        assertEquals(0, order.getStatus());
        
        order.setStatus(1);
        assertEquals(1, order.getStatus());
        
        // When & Then - 已支付 -> 处理中
        order.setStatus(2);
        assertEquals(2, order.getStatus());
        
        // When & Then - 处理中 -> 已完成
        order.setStatus(3);
        assertEquals(3, order.getStatus());
    }
    
    @Test
    @DisplayName("订单状态转换 - 取消流程")
    void testOrderStatusTransition_CancelFlow() {
        // Given
        Order order = new Order();
        order.setStatus(0); // 待支付
        
        // When - 取消订单
        order.setStatus(4);
        
        // Then
        assertEquals(4, order.getStatus());
    }
}
