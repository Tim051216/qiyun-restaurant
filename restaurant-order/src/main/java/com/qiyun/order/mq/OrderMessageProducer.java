package com.qiyun.order.mq;

import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单消息生产者
 * 负责发送订单相关消息到RabbitMQ
 */
@Slf4j
@Component
public class OrderMessageProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 发送订单创建消息
     * 
     * @param order 订单对象
     * @return 消息ID
     */
    public String sendOrderCreateMessage(Order order) {
        String messageId = UUID.randomUUID().toString();
        
        try {
            log.info("准备发送订单创建消息: orderId={}, messageId={}", order.getId(), messageId);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
                    order,
                    message -> {
                        // 设置消息ID
                        message.getMessageProperties().setMessageId(messageId);
                        // 设置时间戳
                        message.getMessageProperties().setTimestamp(
                                java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        // 设置内容类型
                        message.getMessageProperties().setContentType("application/json");
                        // 设置重试次数
                        message.getMessageProperties().setHeader("x-retry-count", 0);
                        return message;
                    }
            );
            
            log.info("订单创建消息发送成功: orderId={}, messageId={}", order.getId(), messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("订单创建消息发送失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            // 记录失败日志到数据库，用于后续补偿
            recordFailedMessage(messageId, order, "ORDER_CREATE", e.getMessage());
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 发送订单支付消息
     * 
     * @param order 订单对象
     * @return 消息ID
     */
    public String sendOrderPaidMessage(Order order) {
        String messageId = UUID.randomUUID().toString();
        
        try {
            log.info("准备发送订单支付消息: orderId={}, messageId={}", order.getId(), messageId);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_PAID_ROUTING_KEY,
                    order,
                    message -> {
                        message.getMessageProperties().setMessageId(messageId);
                        message.getMessageProperties().setTimestamp(
                                java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        message.getMessageProperties().setContentType("application/json");
                        message.getMessageProperties().setHeader("x-retry-count", 0);
                        return message;
                    }
            );
            
            log.info("订单支付消息发送成功: orderId={}, messageId={}", order.getId(), messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("订单支付消息发送失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            recordFailedMessage(messageId, order, "ORDER_PAID", e.getMessage());
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 发送订单取消消息
     * 
     * @param order 订单对象
     * @return 消息ID
     */
    public String sendOrderCancelMessage(Order order) {
        String messageId = UUID.randomUUID().toString();
        
        try {
            log.info("准备发送订单取消消息: orderId={}, messageId={}", order.getId(), messageId);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY,
                    order,
                    message -> {
                        message.getMessageProperties().setMessageId(messageId);
                        message.getMessageProperties().setTimestamp(
                                java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        message.getMessageProperties().setContentType("application/json");
                        message.getMessageProperties().setHeader("x-retry-count", 0);
                        return message;
                    }
            );
            
            log.info("订单取消消息发送成功: orderId={}, messageId={}", order.getId(), messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("订单取消消息发送失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            recordFailedMessage(messageId, order, "ORDER_CANCEL", e.getMessage());
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 记录失败的消息
     * 用于后续补偿机制
     * 
     * @param messageId 消息ID
     * @param order 订单对象
     * @param messageType 消息类型
     * @param errorMessage 错误信息
     */
    private void recordFailedMessage(String messageId, Order order, 
                                     String messageType, String errorMessage) {
        // TODO: 将失败消息记录到数据库
        // 包含：messageId, orderId, messageType, payload, errorMessage, createTime
        // 可以通过定时任务扫描失败消息表，进行重试
        log.warn("记录失败消息: messageId={}, orderId={}, type={}, error={}", 
                messageId, order.getId(), messageType, errorMessage);
    }
    
    /**
     * 批量发送消息
     * 
     * @param orders 订单列表
     * @param messageType 消息类型
     */
    public void sendBatchMessages(java.util.List<Order> orders, String messageType) {
        log.info("批量发送消息: count={}, type={}", orders.size(), messageType);
        
        for (Order order : orders) {
            try {
                switch (messageType) {
                    case "CREATE" -> sendOrderCreateMessage(order);
                    case "PAID" -> sendOrderPaidMessage(order);
                    case "CANCEL" -> sendOrderCancelMessage(order);
                    default -> log.warn("未知的消息类型: {}", messageType);
                }
            } catch (Exception e) {
                log.error("批量发送消息失败: orderId={}", order.getId(), e);
                // 继续处理下一条消息
            }
        }
        
        log.info("批量发送消息完成: count={}, type={}", orders.size(), messageType);
    }
}
