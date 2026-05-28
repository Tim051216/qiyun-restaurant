package com.qiyun.order.task;

import com.qiyun.order.entity.Order;
import com.qiyun.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单超时关闭定时任务
 * 使用 Spring Task 定时扫描未支付订单，超时后自动关闭并释放库存
 * 使用乐观锁解决订单支付和关单的并发冲突问题
 * 
 * @author qiyun
 * @since 2026-02-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {
    
    private final OrderService orderService;
    
    /**
     * 订单超时时间（分钟），默认30分钟
     */
    @Value("${order.timeout.minutes:30}")
    private int timeoutMinutes;
    
    /**
     * 定时扫描超时订单
     * 每5分钟执行一次
     * cron表达式: 0 星号/5 星号 星号 星号 问号 表示每5分钟的第0秒执行
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Trace
    public void closeTimeoutOrders() {
        String traceId = TraceContext.traceId();
        log.info("开始执行订单超时关闭任务, traceId={}", traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-timeout-task");
        ActiveSpan.tag("timeout.minutes", String.valueOf(timeoutMinutes));
        
        try {
            // 查询超时未支付订单
            List<Order> timeoutOrders = orderService.getTimeoutUnpaidOrders(timeoutMinutes);
            
            if (timeoutOrders.isEmpty()) {
                log.info("没有超时订单需要关闭, traceId={}", traceId);
                ActiveSpan.info("没有超时订单需要关闭");
                return;
            }
            
            log.info("发现{}个超时订单，开始关闭, traceId={}", timeoutOrders.size(), traceId);
            ActiveSpan.info("发现超时订单: count=" + timeoutOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 逐个关闭超时订单
            for (Order order : timeoutOrders) {
                try {
                    boolean success = orderService.closeTimeoutOrder(order.getId());
                    if (success) {
                        successCount++;
                        log.info("订单关闭成功: orderId={}, userId={}, traceId={}", 
                            order.getId(), order.getUserId(), traceId);
                    } else {
                        failCount++;
                        log.warn("订单关闭失败（可能已被支付或取消）: orderId={}, traceId={}", 
                            order.getId(), traceId);
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("订单关闭异常: orderId={}, traceId={}", order.getId(), traceId, e);
                }
            }
            
            log.info("订单超时关闭任务完成: 总数={}, 成功={}, 失败={}, traceId={}", 
                timeoutOrders.size(), successCount, failCount, traceId);
            
            ActiveSpan.info(String.format("任务完成: 总数=%d, 成功=%d, 失败=%d", 
                timeoutOrders.size(), successCount, failCount));
            
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单超时关闭任务执行异常, traceId={}", traceId, e);
        }
    }
}
