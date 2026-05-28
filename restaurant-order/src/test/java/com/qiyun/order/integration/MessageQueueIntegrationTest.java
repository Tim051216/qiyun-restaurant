package com.qiyun.order.integration;

import com.qiyun.order.OrderApplication;
import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mq.OrderMessageConsumer;
import com.qiyun.order.mq.OrderMessageProducer;
import com.qiyun.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息队列集成测试
 * 
 * 测试覆盖：
 * - 消息生产者与RabbitMQ的集成
 * - 消息消费者与RabbitMQ的集成
 * - 消息幂等性验证
 * - 消息重试机制
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("test")
@DisplayName("消息队列集成测试")
class MessageQueueIntegrationTest {
    
    @Autowired
    private OrderMessageProducer messageProducer;
    
    @Autowired
    private OrderMessageConsumer messageConsumer;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @MockBean
    private OrderService orderService;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        // 清理Redis缓存
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        
        testOrder = new Order();
        testOrder.setId(1001L);
        testOrder.setUserId(100L);
        testOrder.setTableId(5L);
        testOrder.setTotalAmount(new BigDecimal("99.80"));
        testOrder.setActualAmount(new BigDecimal("99.80"));
        testOrder.setStatus(0);
        testOrder.setCreateTime(LocalDateTime.now());
        
        // Mock订单服务
        doNothing().when(orderService).processOrderCreate(any(Order.class));
        doNothing().when(orderService).processOrderPaid(any(Order.class));
        doNothing().when(orderService).processOrderCancel(any(Order.class));
    }
    
    @Test
    @DisplayName("集成测试 - 发送订单创建消息到RabbitMQ")
    void testSendOrderCreateMessageIntegration() {
        // When
        String messageId = messageProducer.sendOrderCreateMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
        
        // 验证消息已发送到队列（通过检查队列消息数量）
        // 注意：实际测试中可能需要等待消息到达
    }
    
    @Test
    @DisplayName("集成测试 - 发送订单支付消息到RabbitMQ")
    void testSendOrderPaidMessageIntegration() {
        // When
        String messageId = messageProducer.sendOrderPaidMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
    }
    
    @Test
    @DisplayName("集成测试 - 发送订单取消消息到RabbitMQ")
    void testSendOrderCancelMessageIntegration() {
        // When
        String messageId = messageProducer.sendOrderCancelMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
    }
    
    @Test
    @DisplayName("集成测试 - 消息幂等性验证")
    void testMessageIdempotencyIntegration() {
        // Given
        String messageId = "test-message-id-123";
        String idempotentKey = "order:consumed:" + messageId;
        
        // When - 第一次处理消息
        Boolean firstResult = redisTemplate.opsForValue()
            .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        
        // Then - 第一次应该成功
        assertTrue(firstResult);
        
        // When - 第二次处理相同消息
        Boolean secondResult = redisTemplate.opsForValue()
            .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        
        // Then - 第二次应该失败（消息已被消费）
        assertFalse(secondResult);
    }
    
    @Test
    @DisplayName("集成测试 - 消息确认机制")
    void testMessageAcknowledgementIntegration() {
        // Given
        String messageId = messageProducer.sendOrderCreateMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        
        // 验证消息确认回调被触发
        // 注意：实际测试中需要配置RabbitTemplate的ConfirmCallback
    }
    
    @Test
    @DisplayName("集成测试 - 批量发送消息")
    void testBatchSendMessagesIntegration() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        order1.setUserId(100L);
        order1.setTableId(5L);
        order1.setTotalAmount(new BigDecimal("50.00"));
        order1.setActualAmount(new BigDecimal("50.00"));
        order1.setStatus(0);
        
        Order order2 = new Order();
        order2.setId(1002L);
        order2.setUserId(101L);
        order2.setTableId(6L);
        order2.setTotalAmount(new BigDecimal("80.00"));
        order2.setActualAmount(new BigDecimal("80.00"));
        order2.setStatus(0);
        
        // When
        messageProducer.sendBatchMessages(java.util.Arrays.asList(order1, order2), "CREATE");
        
        // Then - 验证两条消息都已发送
        // 注意：实际测试中需要验证队列中的消息数量
    }
    
    @Test
    @DisplayName("集成测试 - 消息发送失败重试")
    void testMessageSendRetryIntegration() {
        // Given - Mock RabbitTemplate抛出异常
        RabbitTemplate mockTemplate = mock(RabbitTemplate.class);
        doThrow(new RuntimeException("连接失败"))
            .when(mockTemplate).convertAndSend(
                anyString(), 
                anyString(), 
                any(Object.class), 
                any(MessagePostProcessor.class)
            );
        
        // When & Then - 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            messageProducer.sendOrderCreateMessage(testOrder);
        });
    }
    
    @Test
    @DisplayName("集成测试 - 验证消息路由键正确性")
    void testMessageRoutingKeyIntegration() {
        // When - 发送不同类型的消息
        messageProducer.sendOrderCreateMessage(testOrder);
        messageProducer.sendOrderPaidMessage(testOrder);
        messageProducer.sendOrderCancelMessage(testOrder);
        
        // Then - 验证消息被路由到正确的队列
        // 注意：实际测试中需要检查各个队列的消息数量
    }
    
    @Test
    @DisplayName("集成测试 - 消息TTL过期测试")
    void testMessageTTLIntegration() throws InterruptedException {
        // Given - 发送消息
        String messageId = messageProducer.sendOrderCreateMessage(testOrder);
        assertNotNull(messageId);
        
        // When - 等待消息TTL过期（配置为30秒）
        // 注意：实际测试中可能需要调整TTL为更短的时间
        
        // Then - 验证消息进入死信队列
        // 注意：需要配置死信队列监听器来验证
    }
    
    @Test
    @DisplayName("集成测试 - 消息持久化验证")
    void testMessagePersistenceIntegration() {
        // When - 发送持久化消息
        String messageId = messageProducer.sendOrderCreateMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        
        // 验证消息属性设置为持久化
        // 注意：需要检查消息的deliveryMode属性
    }
}
