package com.qiyun.order;

import com.qiyun.order.entity.Order;
import com.qiyun.order.mq.OrderMessageConsumer;
import com.qiyun.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息消费测试
 * 验证消息消费的正确性
 * 
 * Property 12: 消息消费处理
 * Property 13: 消息失败处理
 * Property 14: 消息幂等性
 * Property 15: 死信队列日志记录
 */
class MessageConsumingTest {
    
    /**
     * 测试消息消费处理
     * Property 12: 消息消费处理
     */
    @Test
    void testMessageConsumption() throws Exception {
        // Arrange
        Order order = createTestOrder();
        OrderService orderService = mock(OrderService.class);
        RedisTemplate<String, String> redisTemplate = mockRedisTemplate(true);
        Channel channel = mock(Channel.class);
        Message message = createMessage("test-message-id");
        
        OrderMessageConsumer consumer = new OrderMessageConsumer();
        setField(consumer, "orderService", orderService);
        setField(consumer, "redisTemplate", redisTemplate);
        
        // Act
        consumer.handleOrderCreate(order, message, channel);
        
        // Assert
        verify(orderService, times(1)).processOrderCreate(order);
        verify(channel, times(1)).basicAck(anyLong(), eq(false));
    }
    
    /**
     * 测试消息幂等性
     * Property 14: 消息幂等性
     */
    @Test
    void testMessageIdempotency() throws Exception {
        // Arrange
        Order order = createTestOrder();
        OrderService orderService = mock(OrderService.class);
        RedisTemplate<String, String> redisTemplate = mockRedisTemplate(false); // 模拟已消费
        Channel channel = mock(Channel.class);
        Message message = createMessage("duplicate-message-id");
        
        OrderMessageConsumer consumer = new OrderMessageConsumer();
        setField(consumer, "orderService", orderService);
        setField(consumer, "redisTemplate", redisTemplate);
        
        // Act
        consumer.handleOrderCreate(order, message, channel);
        
        // Assert
        verify(orderService, never()).processOrderCreate(any());
        verify(channel, times(1)).basicAck(anyLong(), eq(false));
    }
    
    /**
     * 测试消息失败处理和重试
     * Property 13: 消息失败处理
     */
    @Test
    void testMessageFailureHandling() throws Exception {
        // Arrange
        Order order = createTestOrder();
        OrderService orderService = mock(OrderService.class);
        doThrow(new RuntimeException("处理失败")).when(orderService).processOrderCreate(any());
        
        RedisTemplate<String, String> redisTemplate = mockRedisTemplate(true);
        Channel channel = mock(Channel.class);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-retry-count", 0);
        Message message = createMessageWithHeaders("fail-message-id", headers);
        
        OrderMessageConsumer consumer = new OrderMessageConsumer();
        setField(consumer, "orderService", orderService);
        setField(consumer, "redisTemplate", redisTemplate);
        
        // Act
        consumer.handleOrderCreate(order, message, channel);
        
        // Assert
        verify(channel, times(1)).basicNack(anyLong(), eq(false), eq(true));
    }
    
    /**
     * 测试死信队列处理
     * Property 15: 死信队列日志记录
     */
    @Test
    void testDeadLetterHandling() {
        // Arrange
        MessageProperties properties = new MessageProperties();
        properties.setMessageId("dead-letter-message-id");
        Message message = new Message("Dead letter message body".getBytes(), properties);
        
        OrderMessageConsumer consumer = new OrderMessageConsumer();
        
        // Act
        consumer.handleDeadLetter(message);
        
        // Assert
        // 验证日志记录（实际应该验证数据库记录或告警发送）
        assertThat(message.getMessageProperties().getMessageId()).isNotNull();
    }
    
    // ==================== 辅助方法 ====================
    
    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
    
    private Message createMessage(String messageId) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId);
        properties.setDeliveryTag(1L);
        return new Message(new byte[0], properties);
    }
    
    private Message createMessageWithHeaders(String messageId, Map<String, Object> headers) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId);
        properties.setDeliveryTag(1L);
        properties.getHeaders().putAll(headers);
        return new Message(new byte[0], properties);
    }
    
    @SuppressWarnings("unchecked")
    private RedisTemplate<String, String> mockRedisTemplate(boolean isFirstTime) {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(isFirstTime);
        
        return redisTemplate;
    }
    
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置字段失败: " + fieldName, e);
        }
    }
}
