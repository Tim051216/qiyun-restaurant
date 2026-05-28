package com.qiyun.order;

import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mq.OrderMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 简单消息测试
 * 验证消息发送的基本功能
 */
class SimpleMessageTest {
    
    @Test
    void testMessageSending() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AtomicBoolean messageSent = new AtomicBoolean(false);
        
        doAnswer(invocation -> {
            messageSent.set(true);
            return null;
        }).when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(Order.class), any(MessagePostProcessor.class));
        
        OrderMessageProducer producer = new OrderMessageProducer();
        setField(producer, "rabbitTemplate", rabbitTemplate);
        
        // Act
        String messageId = producer.sendOrderCreateMessage(order);
        
        // Assert
        assertThat(messageId).isNotNull();
        assertThat(messageSent.get()).isTrue();
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
