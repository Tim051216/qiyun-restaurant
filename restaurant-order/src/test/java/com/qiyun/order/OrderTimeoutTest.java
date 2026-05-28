package com.qiyun.order;

import com.qiyun.order.entity.Order;
import com.qiyun.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单超时关闭功能测试
 * 测试订单超时自动关闭和乐观锁并发控制
 * 
 * @author qiyun
 * @since 2026-02-10
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("订单超时关闭功能测试")
class OrderTimeoutTest {
    
    @Autowired
    private OrderService orderService;
    
    @Test
    @DisplayName("测试查询超时未支付订单")
    void testGetTimeoutUnpaidOrders() {
        // 创建一个超时的未支付订单
        Order order = new Order();
        order.setUserId(1001L);
        order.setTableId(1L);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setActualAmount(new BigDecimal("100.00"));
        order.setStatus(0); // 未支付
        order.setCreateTime(LocalDateTime.now().minusMinutes(35)); // 35分钟前创建
        
        orderService.save(order);
        
        // 查询超时订单（超时时间30分钟）
        List<Order> timeoutOrders = orderService.getTimeoutUnpaidOrders(30);
        
        // 验证
        assertNotNull(timeoutOrders);
        assertTrue(timeoutOrders.size() > 0);
        assertTrue(timeoutOrders.stream().anyMatch(o -> o.getId().equals(order.getId())));
        
        // 清理测试数据
        orderService.removeById(order.getId());
    }
    
    @Test
    @DisplayName("测试关闭超时订单")
    void testCloseTimeoutOrder() {
        // 创建一个未支付订单
        Order order = new Order();
        order.setUserId(1002L);
        order.setTableId(2L);
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setActualAmount(new BigDecimal("200.00"));
        order.setStatus(0); // 未支付
        
        orderService.save(order);
        
        // 关闭订单
        boolean success = orderService.closeTimeoutOrder(order.getId());
        
        // 验证
        assertTrue(success);
        
        // 查询订单状态
        Order closedOrder = orderService.getById(order.getId());
        assertNotNull(closedOrder);
        assertEquals(5, closedOrder.getStatus()); // 5=已关闭
        
        // 清理测试数据
        orderService.removeById(order.getId());
    }
    
    @Test
    @DisplayName("测试不关闭已支付订单")
    void testNotClosePaidOrder() {
        // 创建一个已支付订单
        Order order = new Order();
        order.setUserId(1003L);
        order.setTableId(3L);
        order.setTotalAmount(new BigDecimal("300.00"));
        order.setActualAmount(new BigDecimal("300.00"));
        order.setStatus(1); // 已支付
        
        orderService.save(order);
        
        // 尝试关闭订单
        boolean success = orderService.closeTimeoutOrder(order.getId());
        
        // 验证：不应该关闭已支付订单
        assertFalse(success);
        
        // 查询订单状态
        Order paidOrder = orderService.getById(order.getId());
        assertNotNull(paidOrder);
        assertEquals(1, paidOrder.getStatus()); // 状态应该保持为已支付
        
        // 清理测试数据
        orderService.removeById(order.getId());
    }
    
    @Test
    @DisplayName("测试乐观锁并发控制")
    void testOptimisticLockConcurrency() throws InterruptedException {
        // 创建一个未支付订单
        Order order = new Order();
        order.setUserId(1004L);
        order.setTableId(4L);
        order.setTotalAmount(new BigDecimal("400.00"));
        order.setActualAmount(new BigDecimal("400.00"));
        order.setStatus(0); // 未支付
        
        orderService.save(order);
        Long orderId = order.getId();
        
        // 并发测试：10个线程同时尝试关闭订单
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    boolean success = orderService.closeTimeoutOrder(orderId);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executorService.shutdown();
        
        // 验证：由于乐观锁，只有一个线程能成功关闭订单
        assertEquals(1, successCount.get(), "只有一个线程应该成功关闭订单");
        assertEquals(threadCount - 1, failCount.get(), "其他线程应该失败");
        
        // 查询订单状态
        Order closedOrder = orderService.getById(orderId);
        assertNotNull(closedOrder);
        assertEquals(5, closedOrder.getStatus()); // 5=已关闭
        
        // 清理测试数据
        orderService.removeById(orderId);
    }
    
    @Test
    @DisplayName("测试支付和关单并发冲突")
    void testPaymentAndCloseConcurrency() throws InterruptedException {
        // 创建一个未支付订单
        Order order = new Order();
        order.setUserId(1005L);
        order.setTableId(5L);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setActualAmount(new BigDecimal("500.00"));
        order.setStatus(0); // 未支付
        
        orderService.save(order);
        Long orderId = order.getId();
        
        // 并发测试：一个线程支付，一个线程关闭
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger paymentSuccess = new AtomicInteger(0);
        AtomicInteger closeSuccess = new AtomicInteger(0);
        
        // 线程1：支付订单
        executorService.submit(() -> {
            try {
                Order payOrder = orderService.getById(orderId);
                if (payOrder != null && payOrder.getStatus() == 0) {
                    payOrder.setStatus(1); // 已支付
                    boolean success = orderService.updateById(payOrder);
                    if (success) {
                        paymentSuccess.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                // 忽略异常
            } finally {
                latch.countDown();
            }
        });
        
        // 线程2：关闭订单
        executorService.submit(() -> {
            try {
                boolean success = orderService.closeTimeoutOrder(orderId);
                if (success) {
                    closeSuccess.incrementAndGet();
                }
            } catch (Exception e) {
                // 忽略异常
            } finally {
                latch.countDown();
            }
        });
        
        latch.await();
        executorService.shutdown();
        
        // 验证：由于乐观锁，只有一个操作能成功
        assertEquals(1, paymentSuccess.get() + closeSuccess.get(), 
            "支付和关闭操作只有一个能成功");
        
        // 查询最终订单状态
        Order finalOrder = orderService.getById(orderId);
        assertNotNull(finalOrder);
        assertTrue(finalOrder.getStatus() == 1 || finalOrder.getStatus() == 5, 
            "订单状态应该是已支付或已关闭");
        
        // 清理测试数据
        orderService.removeById(orderId);
    }
}
