package com.qiyun.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务自定义业务指标
 * 
 * 提供订单相关的Prometheus监控指标
 */
@Slf4j
@Component
public class OrderMetrics {
    
    private final Counter orderCreateCounter;
    private final Counter orderPaidCounter;
    private final Counter orderCancelCounter;
    private final Timer orderProcessTimer;
    private final Gauge orderQueueGauge;
    private final RabbitTemplate rabbitTemplate;
    
    // 用于队列长度监控的原子计数器
    private final AtomicLong queueSize = new AtomicLong(0);
    
    public OrderMetrics(MeterRegistry registry, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        
        // 订单创建计数器
        this.orderCreateCounter = Counter.builder("order.create.total")
            .description("订单创建总数")
            .tag("service", "order")
            .tag("type", "create")
            .register(registry);
        
        // 订单支付计数器
        this.orderPaidCounter = Counter.builder("order.paid.total")
            .description("订单支付总数")
            .tag("service", "order")
            .tag("type", "paid")
            .register(registry);
        
        // 订单取消计数器
        this.orderCancelCounter = Counter.builder("order.cancel.total")
            .description("订单取消总数")
            .tag("service", "order")
            .tag("type", "cancel")
            .register(registry);
        
        // 订单处理耗时计时器
        this.orderProcessTimer = Timer.builder("order.process.duration")
            .description("订单处理耗时（秒）")
            .tag("service", "order")
            .register(registry);
        
        // 订单队列长度监控
        this.orderQueueGauge = Gauge.builder("order.queue.size", this::getQueueSize)
            .description("订单队列长度")
            .tag("service", "order")
            .tag("queue", "order-create")
            .register(registry);
        
        log.info("订单业务指标初始化完成");
    }
    
    /**
     * 记录订单创建
     */
    public void recordOrderCreate() {
        orderCreateCounter.increment();
        log.debug("订单创建计数器+1，当前值: {}", orderCreateCounter.count());
    }
    
    /**
     * 记录订单支付
     */
    public void recordOrderPaid() {
        orderPaidCounter.increment();
        log.debug("订单支付计数器+1，当前值: {}", orderPaidCounter.count());
    }
    
    /**
     * 记录订单取消
     */
    public void recordOrderCancel() {
        orderCancelCounter.increment();
        log.debug("订单取消计数器+1，当前值: {}", orderCancelCounter.count());
    }
    
    /**
     * 记录订单处理耗时
     * 
     * @param task 要执行的任务
     */
    public void recordOrderProcess(Runnable task) {
        orderProcessTimer.record(task);
    }
    
    /**
     * 记录订单处理耗时（手动计时）
     * 
     * @return Timer.Sample 用于手动停止计时
     */
    public Timer.Sample startOrderProcessTimer() {
        return Timer.start();
    }
    
    /**
     * 停止订单处理计时
     * 
     * @param sample 开始计时时返回的Sample
     */
    public void stopOrderProcessTimer(Timer.Sample sample) {
        sample.stop(orderProcessTimer);
    }
    
    /**
     * 获取订单队列长度
     * 
     * @return 队列长度
     */
    private double getQueueSize() {
        try {
            // 从RabbitMQ获取队列长度
            String queueName = "restaurant.order.create.queue";
            org.springframework.amqp.core.QueueInformation queueInfo = 
                rabbitTemplate.execute(channel -> {
                    try {
                        com.rabbitmq.client.AMQP.Queue.DeclareOk declareOk = 
                            channel.queueDeclarePassive(queueName);
                        return new org.springframework.amqp.core.QueueInformation(
                            queueName, 
                            declareOk.getMessageCount(), 
                            declareOk.getConsumerCount()
                        );
                    } catch (Exception e) {
                        log.warn("获取队列信息失败: {}", e.getMessage());
                        return null;
                    }
                });
            
            if (queueInfo != null) {
                long size = queueInfo.getMessageCount();
                queueSize.set(size);
                return size;
            }
        } catch (Exception e) {
            log.warn("获取队列长度失败: {}", e.getMessage());
        }
        
        return queueSize.get();
    }
    
    /**
     * 获取订单创建总数
     */
    public double getOrderCreateTotal() {
        return orderCreateCounter.count();
    }
    
    /**
     * 获取订单支付总数
     */
    public double getOrderPaidTotal() {
        return orderPaidCounter.count();
    }
    
    /**
     * 获取订单取消总数
     */
    public double getOrderCancelTotal() {
        return orderCancelCounter.count();
    }
}
