package com.qiyun.order.mq;

import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单消息生产者单元测试
 * 
 * 测试覆盖：
 * - 消息发送成功
 * - 消息发送失败
 * - 消息属性设置
 * - 批量发送
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单消息生产者单元测试")
class OrderMessageProducerTest {
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private OrderMessageProducer messageProducer;
    
    private Order testOrder;
    
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
    }
    
    @Test
    @DisplayName("发送订单创建消息 - 成功场景")
    void testSendOrderCreateMessage_Success() {
        // Given
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        String messageId = messageProducer.sendOrderCreateMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
        
        // 验证调用参数
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
            exchangeCaptor.capture(),
            routingKeyCaptor.capture(),
            orderCaptor.capture(),
            any(MessagePostProcessor.class)
        );
        
        assertEquals(RabbitMQConfig.ORDER_EXCHANGE, exchangeCaptor.getValue());
        assertEquals(RabbitMQConfig.ORDER_CREATE_ROUTING_KEY, routingKeyCaptor.getValue());
        assertEquals(testOrder, orderCaptor.getValue());
    }
    
    @Test
    @DisplayName("发送订单创建消息 - 发送失败")
    void testSendOrderCreateMessage_Failure() {
        // Given
        doThrow(new RuntimeException("RabbitMQ连接失败"))
            .when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageProducer.sendOrderCreateMessage(testOrder);
        });
        
        assertTrue(exception.getMessage().contains("消息发送失败"));
        verify(rabbitTemplate, times(1)).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
    }
    
    @Test
    @DisplayName("发送订单支付消息 - 成功场景")
    void testSendOrderPaidMessage_Success() {
        // Given
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        String messageId = messageProducer.sendOrderPaidMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
        
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.ORDER_EXCHANGE),
            routingKeyCaptor.capture(),
            eq(testOrder),
            any(MessagePostProcessor.class)
        );
        
        assertEquals(RabbitMQConfig.ORDER_PAID_ROUTING_KEY, routingKeyCaptor.getValue());
    }
    
    @Test
    @DisplayName("发送订单支付消息 - 发送失败")
    void testSendOrderPaidMessage_Failure() {
        // Given
        doThrow(new RuntimeException("网络超时"))
            .when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageProducer.sendOrderPaidMessage(testOrder);
        });
        
        assertTrue(exception.getMessage().contains("消息发送失败"));
    }
    
    @Test
    @DisplayName("发送订单取消消息 - 成功场景")
    void testSendOrderCancelMessage_Success() {
        // Given
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        String messageId = messageProducer.sendOrderCancelMessage(testOrder);
        
        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
        
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.ORDER_EXCHANGE),
            routingKeyCaptor.capture(),
            eq(testOrder),
            any(MessagePostProcessor.class)
        );
        
        assertEquals(RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY, routingKeyCaptor.getValue());
    }
    
    @Test
    @DisplayName("发送订单取消消息 - 发送失败")
    void testSendOrderCancelMessage_Failure() {
        // Given
        doThrow(new RuntimeException("队列已满"))
            .when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageProducer.sendOrderCancelMessage(testOrder);
        });
        
        assertTrue(exception.getMessage().contains("消息发送失败"));
    }
    
    @Test
    @DisplayName("批量发送消息 - CREATE类型")
    void testSendBatchMessages_CreateType() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        
        Order order2 = new Order();
        order2.setId(1002L);
        
        List<Order> orders = Arrays.asList(order1, order2);
        
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        messageProducer.sendBatchMessages(orders, "CREATE");
        
        // Then
        verify(rabbitTemplate, times(2)).convertAndSend(
            eq(RabbitMQConfig.ORDER_EXCHANGE),
            eq(RabbitMQConfig.ORDER_CREATE_ROUTING_KEY),
            any(Order.class),
            any(MessagePostProcessor.class)
        );
    }
    
    @Test
    @DisplayName("批量发送消息 - PAID类型")
    void testSendBatchMessages_PaidType() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        
        List<Order> orders = Arrays.asList(order1);
        
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        messageProducer.sendBatchMessages(orders, "PAID");
        
        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.ORDER_EXCHANGE),
            eq(RabbitMQConfig.ORDER_PAID_ROUTING_KEY),
            any(Order.class),
            any(MessagePostProcessor.class)
        );
    }
    
    @Test
    @DisplayName("批量发送消息 - CANCEL类型")
    void testSendBatchMessages_CancelType() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        
        List<Order> orders = Arrays.asList(order1);
        
        doNothing().when(rabbitTemplate).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        messageProducer.sendBatchMessages(orders, "CANCEL");
        
        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.ORDER_EXCHANGE),
            eq(RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY),
            any(Order.class),
            any(MessagePostProcessor.class)
        );
    }
    
    @Test
    @DisplayName("批量发送消息 - 未知类型")
    void testSendBatchMessages_UnknownType() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        
        List<Order> orders = Arrays.asList(order1);
        
        // When
        messageProducer.sendBatchMessages(orders, "UNKNOWN");
        
        // Then
        verify(rabbitTemplate, never()).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
    }
    
    @Test
    @DisplayName("批量发送消息 - 部分失败继续处理")
    void testSendBatchMessages_PartialFailure() {
        // Given
        Order order1 = new Order();
        order1.setId(1001L);
        
        Order order2 = new Order();
        order2.setId(1002L);
        
        Order order3 = new Order();
        order3.setId(1003L);
        
        List<Order> orders = Arrays.asList(order1, order2, order3);
        
        // 第二个订单发送失败
        doNothing()
            .doThrow(new RuntimeException("发送失败"))
            .doNothing()
            .when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(), any(MessagePostProcessor.class));
        
        // When
        messageProducer.sendBatchMessages(orders, "CREATE");
        
        // Then - 应该尝试发送所有3条消息
        verify(rabbitTemplate, times(3)).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
    }
    
    @Test
    @DisplayName("批量发送消息 - 空列表")
    void testSendBatchMessages_EmptyList() {
        // Given
        List<Order> orders = Arrays.asList();
        
        // When
        messageProducer.sendBatchMessages(orders, "CREATE");
        
        // Then
        verify(rabbitTemplate, never()).convertAndSend(
            anyString(), anyString(), any(), any(MessagePostProcessor.class));
    }
}
