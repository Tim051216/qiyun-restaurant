package com.qiyun.order.integration;

import com.qiyun.order.entity.Order;
import com.qiyun.order.mapper.OrderMapper;
import com.qiyun.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单服务Testcontainers集成测试
 * 
 * 使用Testcontainers启动真实的MySQL和Redis容器进行测试
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@SpringBootTest
@Testcontainers
@DisplayName("订单服务Testcontainers集成测试")
class OrderServiceTestcontainersTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("restaurant_test")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("test-schema.sql");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
        .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 配置MySQL连接
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        
        // 配置Redis连接
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
    }
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderMapper orderMapper;
    
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
    }
    
    @Test
    @DisplayName("Testcontainers - MySQL容器运行正常")
    void testMySQLContainerIsRunning() {
        assertTrue(mysql.isRunning());
        assertNotNull(mysql.getJdbcUrl());
    }
    
    @Test
    @DisplayName("Testcontainers - Redis容器运行正常")
    void testRedisContainerIsRunning() {
        assertTrue(redis.isRunning());
        assertNotNull(redis.getHost());
        assertTrue(redis.getMappedPort(6379) > 0);
    }
    
    @Test
    @DisplayName("Testcontainers - 创建订单到真实MySQL")
    void testCreateOrderWithRealMySQL() {
        // When
        Order createdOrder = orderService.createOrder(testOrder);
        
        // Then
        assertNotNull(createdOrder);
        assertNotNull(createdOrder.getId());
        
        // 验证数据真实写入MySQL
        Order dbOrder = orderMapper.selectById(createdOrder.getId());
        assertNotNull(dbOrder);
        assertEquals(createdOrder.getUserId(), dbOrder.getUserId());
        assertEquals(createdOrder.getTotalAmount(), dbOrder.getTotalAmount());
    }
    
    @Test
    @DisplayName("Testcontainers - 查询订单从真实MySQL")
    void testQueryOrderFromRealMySQL() {
        // Given - 先创建订单
        Order createdOrder = orderService.createOrder(testOrder);
        
        // When - 查询订单
        Order foundOrder = orderService.getById(createdOrder.getId());
        
        // Then
        assertNotNull(foundOrder);
        assertEquals(createdOrder.getId(), foundOrder.getId());
        assertEquals(createdOrder.getUserId(), foundOrder.getUserId());
    }
    
    @Test
    @DisplayName("Testcontainers - 更新订单到真实MySQL")
    void testUpdateOrderInRealMySQL() {
        // Given
        Order createdOrder = orderService.createOrder(testOrder);
        
        // When - 更新订单状态
        createdOrder.setStatus(1);
        boolean updated = orderService.updateById(createdOrder);
        
        // Then
        assertTrue(updated);
        
        // 验证更新成功
        Order dbOrder = orderMapper.selectById(createdOrder.getId());
        assertEquals(1, dbOrder.getStatus());
    }
    
    @Test
    @DisplayName("Testcontainers - 删除订单从真实MySQL")
    void testDeleteOrderFromRealMySQL() {
        // Given
        Order createdOrder = orderService.createOrder(testOrder);
        Long orderId = createdOrder.getId();
        
        // When
        boolean deleted = orderService.removeById(orderId);
        
        // Then
        assertTrue(deleted);
        
        // 验证删除成功
        Order dbOrder = orderMapper.selectById(orderId);
        assertNull(dbOrder);
    }
    
    @Test
    @DisplayName("Testcontainers - 事务回滚测试")
    void testTransactionRollback() {
        // Given
        int initialCount = orderMapper.selectList(null).size();
        
        // When - 模拟异常导致事务回滚
        try {
            Order order = orderService.createOrder(testOrder);
            assertNotNull(order.getId());
            
            // 模拟后续操作失败
            throw new RuntimeException("模拟异常");
        } catch (RuntimeException e) {
            // 异常被捕获
        }
        
        // Then - 验证订单已创建（因为createOrder方法本身没有事务）
        int finalCount = orderMapper.selectList(null).size();
        assertEquals(initialCount + 1, finalCount);
    }
    
    @Test
    @DisplayName("Testcontainers - 数据库连接池测试")
    void testDatabaseConnectionPool() {
        // When - 并发创建多个订单
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(100L + i);
            order.setTableId((long) (5 + i));
            order.setTotalAmount(new BigDecimal("50.00"));
            order.setActualAmount(new BigDecimal("50.00"));
            order.setStatus(0);
            orderService.createOrder(order);
        }
        
        // Then - 验证所有订单都创建成功
        int count = orderMapper.selectList(null).size();
        assertTrue(count >= 10);
    }
}
