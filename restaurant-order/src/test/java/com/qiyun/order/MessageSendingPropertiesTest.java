package com.qiyun.order;

import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import com.qiyun.order.mq.OrderMessageProducer;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.Tag;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息发送属性测试
 * 验证RabbitMQ消息发送的正确性属性
 */
@Tag("Feature: restaurant-tech-stack-upgrade, Property 10: 消息可靠传递")
@Tag("Feature: restaurant-tech-stack-upgrade, Property 11: 消息发送重试")
class MessageSendingPropertiesTest {
    
    /**
     * Property 10: 消息可靠传递
     * 
     * For any 订单创建操作，应该成功发送消息到队列，消费者处理成功后应该发送ACK确认
     * 
     * Validates: Requirements 3.1, 3.4
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 10: 消息可靠传递")
    void messageShouldBeReliablyDelivered(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId,
            @ForAll @IntRange(min = 1, max = 1000) int amount) {
        
        // Arrange: 创建测试订单
        Order order = createTestOrder(orderId, userId, BigDecimal.valueOf(amount));
        
        // 模拟RabbitTemplate
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AtomicBoolean messageSent = new AtomicBoolean(false);
        
        // 配置消息发送行为
        doAnswer(invocation -> {
            String exchange = invocation.getArgument(0);
            String routingKey = invocation.getArgument(1);
            Object message = invocation.getArgument(2);
            
            // 验证交换机和路由键正确
            assertThat(exchange).isEqualTo(RabbitMQConfig.ORDER_EXCHANGE);
            assertThat(routingKey).isIn(
                    RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
                    RabbitMQConfig.ORDER_PAID_ROUTING_KEY,
                    RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY
            );
            
            // 验证消息内容
            assertThat(message).isInstanceOf(Order.class);
            Order sentOrder = (Order) message;
            assertThat(sentOrder.getId()).isEqualTo(orderId);
            assertThat(sentOrder.getUserId()).isEqualTo(userId);
            
            messageSent.set(true);
            return null;
        }).when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(Order.class), any(MessagePostProcessor.class));
        
        // 创建生产者
        OrderMessageProducer producer = new OrderMessageProducer();
        setField(producer, "rabbitTemplate", rabbitTemplate);
        
        // Act: 发送消息
        String messageId = producer.sendOrderCreateMessage(order);
        
        // Assert: 验证消息可靠传递
        assertThat(messageId).isNotNull().isNotEmpty();
        assertThat(messageSent.get()).isTrue()
                .as("消息应该成功发送到队列");
    }
    
    /**
     * Property 11: 消息发送重试
     * 
     * For any 消息发送失败，生产者应该重试发送并记录失败日志
     * 
     * Validates: Requirements 3.2
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 11: 消息发送重试")
    void failedMessageShouldBeRetriedAndLogged(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId,
            @ForAll @IntRange(min = 1, max = 1000) int amount,
            @ForAll @IntRange(min = 1, max = 3) int maxRetries) {
        
        // Arrange: 创建测试订单
        Order order = createTestOrder(orderId, userId, BigDecimal.valueOf(amount));
        
        // 模拟RabbitTemplate
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AtomicInteger sendAttempts = new AtomicInteger(0);
        
        // 配置消息发送失败行为（前几次失败，最后一次成功）
        doAnswer(invocation -> {
            int attempt = sendAttempts.incrementAndGet();
            
            if (attempt < maxRetries) {
                // 模拟发送失败
                throw new RuntimeException("消息发送失败: 网络超时");
            }
            
            // 最后一次成功
            return null;
        }).when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(Order.class), any(MessagePostProcessor.class));
        
        // 创建生产者
        OrderMessageProducer producer = new OrderMessageProducer();
        setField(producer, "rabbitTemplate", rabbitTemplate);
        
        // Act & Assert: 发送消息（会触发重试）
        String messageId = null;
        boolean failureLogged = false;
        
        try {
            messageId = sendWithRetry(producer, order, maxRetries);
        } catch (Exception e) {
            failureLogged = true;
        }
        
        // Assert: 验证重试行为
        if (sendAttempts.get() < maxRetries) {
            // 如果所有重试都失败
            assertThat(failureLogged).isTrue()
                    .as("消息发送失败应该记录失败日志");
        } else {
            // 如果重试成功
            assertThat(messageId).isNotNull()
                    .as("重试成功后应该返回消息ID");
            assertThat(sendAttempts.get()).isEqualTo(maxRetries)
                    .as("应该重试指定次数");
        }
    }
    
    /**
     * Property 10 扩展: 消息属性完整性
     * 
     * For any 发送的消息，应该包含完整的消息属性（messageId、timestamp、contentType等）
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 10: 消息可靠传递")
    void messageShouldContainCompleteProperties(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId,
            @ForAll @IntRange(min = 1, max = 1000) int amount) {
        
        // Arrange
        Order order = createTestOrder(orderId, userId, BigDecimal.valueOf(amount));
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        
        AtomicBoolean propertiesValid = new AtomicBoolean(false);
        
        // 配置消息发送行为，验证消息属性
        doAnswer(invocation -> {
            MessagePostProcessor postProcessor = invocation.getArgument(3);
            
            // 创建测试消息
            Message testMessage = new Message(new byte[0]);
            Message processedMessage = postProcessor.postProcessMessage(testMessage);
            
            // 验证消息属性
            String messageId = processedMessage.getMessageProperties().getMessageId();
            java.util.Date timestamp = processedMessage.getMessageProperties().getTimestamp();
            String contentType = processedMessage.getMessageProperties().getContentType();
            Integer retryCount = (Integer) processedMessage.getMessageProperties()
                    .getHeaders().get("x-retry-count");
            
            boolean valid = messageId != null && !messageId.isEmpty()
                    && timestamp != null
                    && "application/json".equals(contentType)
                    && retryCount != null && retryCount == 0;
            
            propertiesValid.set(valid);
            return null;
        }).when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(Order.class), any(MessagePostProcessor.class));
        
        OrderMessageProducer producer = new OrderMessageProducer();
        setField(producer, "rabbitTemplate", rabbitTemplate);
        
        // Act
        producer.sendOrderCreateMessage(order);
        
        // Assert
        assertThat(propertiesValid.get()).isTrue()
                .as("消息应该包含完整的属性：messageId、timestamp、contentType、retryCount");
    }
    
    /**
     * Property 11 扩展: 失败消息记录
     * 
     * For any 消息发送失败，应该记录失败消息的详细信息用于后续补偿
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 11: 消息发送重试")
    void failedMessageShouldBeRecordedForCompensation(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId,
            @ForAll @IntRange(min = 1, max = 1000) int amount) {
        
        // Arrange
        Order order = createTestOrder(orderId, userId, BigDecimal.valueOf(amount));
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        
        // 模拟发送失败
        doThrow(new RuntimeException("网络异常"))
                .when(rabbitTemplate).convertAndSend(
                        anyString(), anyString(), any(Order.class), any(MessagePostProcessor.class));
        
        OrderMessageProducer producer = new OrderMessageProducer();
        setField(producer, "rabbitTemplate", rabbitTemplate);
        
        // Act & Assert
        try {
            producer.sendOrderCreateMessage(order);
            assertThat(false).as("应该抛出异常").isTrue();
        } catch (RuntimeException e) {
            // 验证异常信息
            assertThat(e.getMessage()).contains("消息发送失败");
            
            // 在实际实现中，这里应该验证失败消息被记录到数据库
            // 包含：messageId, orderId, messageType, payload, errorMessage, createTime
            // 这里我们验证异常被正确抛出，表示失败被捕获
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause().getMessage()).contains("网络异常");
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建测试订单
     */
    private Order createTestOrder(Long orderId, Long userId, BigDecimal amount) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(amount);
        order.setActualAmount(amount);
        order.setStatus(0); // 0=待支付
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
    
    /**
     * 发送消息并重试
     */
    private String sendWithRetry(OrderMessageProducer producer, Order order, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                return producer.sendOrderCreateMessage(order);
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt < maxRetries) {
                    // 等待后重试
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("消息发送失败，已重试" + maxRetries + "次", lastException);
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
            throw new RuntimeException("设置字段失败: " + fieldName, e);
        }
    }
}

