package com.qiyun.order.mq;

import com.qiyun.order.config.RabbitMQConfig;
import com.qiyun.order.entity.Order;
import com.qiyun.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 订单消息消费者
 * 负责消费RabbitMQ中的订单消息
 */
@Slf4j
@Component
public class OrderMessageConsumer {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 消费订单创建消息
     * 
     * @param order 订单对象
     * @param message 原始消息
     * @param channel 通道
     */
    @RabbitListener(
            queues = RabbitMQConfig.ORDER_CREATE_QUEUE,
            autoStartup = "${spring.rabbitmq.listener.simple.auto-startup:true}"
    )
    public void handleOrderCreate(Order order, Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        log.info("接收到订单创建消息: orderId={}, messageId={}", order.getId(), messageId);
        
        try {
            // 幂等性检查
            if (!checkIdempotent(messageId)) {
                log.warn("消息已被消费，跳过: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            
            // 处理业务逻辑
            orderService.processOrderCreate(order);
            
            // 手动ACK确认
            channel.basicAck(deliveryTag, false);
            log.info("订单创建消息处理成功: orderId={}, messageId={}", order.getId(), messageId);
            
        } catch (Exception e) {
            log.error("订单创建消息处理失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            
            // 处理失败，根据重试次数决定是否重新入队
            handleMessageFailure(message, channel, deliveryTag, e);
        }
    }
    
    /**
     * 消费订单支付消息
     * 
     * @param order 订单对象
     * @param message 原始消息
     * @param channel 通道
     */
    @RabbitListener(
            queues = RabbitMQConfig.ORDER_PAID_QUEUE,
            autoStartup = "${spring.rabbitmq.listener.simple.auto-startup:true}"
    )
    public void handleOrderPaid(Order order, Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        log.info("接收到订单支付消息: orderId={}, messageId={}", order.getId(), messageId);
        
        try {
            // 幂等性检查
            if (!checkIdempotent(messageId)) {
                log.warn("消息已被消费，跳过: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            
            // 处理业务逻辑
            orderService.processOrderPaid(order);
            
            // 手动ACK确认
            channel.basicAck(deliveryTag, false);
            log.info("订单支付消息处理成功: orderId={}, messageId={}", order.getId(), messageId);
            
        } catch (Exception e) {
            log.error("订单支付消息处理失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            
            handleMessageFailure(message, channel, deliveryTag, e);
        }
    }
    
    /**
     * 消费订单取消消息
     * 
     * @param order 订单对象
     * @param message 原始消息
     * @param channel 通道
     */
    @RabbitListener(
            queues = RabbitMQConfig.ORDER_CANCEL_QUEUE,
            autoStartup = "${spring.rabbitmq.listener.simple.auto-startup:true}"
    )
    public void handleOrderCancel(Order order, Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        log.info("接收到订单取消消息: orderId={}, messageId={}", order.getId(), messageId);
        
        try {
            // 幂等性检查
            if (!checkIdempotent(messageId)) {
                log.warn("消息已被消费，跳过: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            
            // 处理业务逻辑
            orderService.processOrderCancel(order);
            
            // 手动ACK确认
            channel.basicAck(deliveryTag, false);
            log.info("订单取消消息处理成功: orderId={}, messageId={}", order.getId(), messageId);
            
        } catch (Exception e) {
            log.error("订单取消消息处理失败: orderId={}, messageId={}", 
                    order.getId(), messageId, e);
            
            handleMessageFailure(message, channel, deliveryTag, e);
        }
    }
    
    /**
     * 消费死信队列消息
     * 
     * @param message 原始消息
     */
    @RabbitListener(
            queues = RabbitMQConfig.DLX_QUEUE,
            autoStartup = "${spring.rabbitmq.listener.simple.auto-startup:true}"
    )
    public void handleDeadLetter(Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        String body = new String(message.getBody());
        
        log.error("接收到死信消息: messageId={}, body={}", messageId, body);
        
        // 记录死信消息到数据库或发送告警
        recordDeadLetterMessage(messageId, body, message);
        
        // TODO: 可以实现告警通知机制
        // 例如：发送邮件、短信、钉钉通知等
    }
    
    /**
     * 幂等性检查
     * 使用Redis的SETNX命令实现
     * 
     * @param messageId 消息ID
     * @return true=首次消费，false=重复消费
     */
    private boolean checkIdempotent(String messageId) {
        String idempotentKey = "order:consumed:" + messageId;
        
        // 使用SETNX命令，如果key不存在则设置成功返回true，存在则返回false
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 处理消息失败
     * 根据重试次数决定是否重新入队或进入死信队列
     * 
     * @param message 消息
     * @param channel 通道
     * @param deliveryTag 投递标签
     * @param exception 异常
     */
    private void handleMessageFailure(Message message, Channel channel, 
                                      long deliveryTag, Exception exception) throws IOException {
        // 获取重试次数
        Integer retryCount = (Integer) message.getMessageProperties()
                .getHeaders().getOrDefault("x-retry-count", 0);
        
        // 最大重试次数
        final int MAX_RETRY_COUNT = 3;
        
        if (retryCount < MAX_RETRY_COUNT) {
            // 重新入队，等待重试
            log.warn("消息处理失败，重新入队: messageId={}, retryCount={}", 
                    message.getMessageProperties().getMessageId(), retryCount);
            
            // 增加重试次数
            message.getMessageProperties().getHeaders().put("x-retry-count", retryCount + 1);
            
            // NACK并重新入队
            channel.basicNack(deliveryTag, false, true);
            
        } else {
            // 超过最大重试次数，进入死信队列
            log.error("消息处理失败，超过最大重试次数，进入死信队列: messageId={}, retryCount={}", 
                    message.getMessageProperties().getMessageId(), retryCount);
            
            // NACK但不重新入队，消息会进入死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }
    
    /**
     * 记录死信消息
     * 
     * @param messageId 消息ID
     * @param body 消息体
     * @param message 原始消息
     */
    private void recordDeadLetterMessage(String messageId, String body, Message message) {
        // TODO: 将死信消息记录到数据库
        // 包含：messageId, body, headers, timestamp, reason
        
        log.error("死信消息详情: messageId={}, body={}, headers={}", 
                messageId, body, message.getMessageProperties().getHeaders());
        
        // 可以存储到专门的死信消息表，用于后续人工处理
        // 例如：
        // DeadLetterMessage dlm = new DeadLetterMessage();
        // dlm.setMessageId(messageId);
        // dlm.setBody(body);
        // dlm.setHeaders(JSON.toJSONString(message.getMessageProperties().getHeaders()));
        // dlm.setCreateTime(LocalDateTime.now());
        // deadLetterMessageMapper.insert(dlm);
    }
}
