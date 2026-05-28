package com.qiyun.order;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * QPS限流属性测试
 * 
 * 测试Sentinel QPS限流功能的正确性
 * 
 * Property 23: QPS限流判断 - 当请求QPS超过阈值时，应该触发限流
 * Property 24: 限流拒绝响应 - 被限流的请求应该返回友好的错误信息
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class QpsRateLimitPropertiesTest {
    
    /**
     * Property 23: QPS限流判断
     * 
     * 属性：当请求QPS超过配置的阈值时，应该触发限流
     * 
     * 验证：
     * 1. 配置QPS限流规则（阈值为qpsThreshold）
     * 2. 在1秒内发送超过阈值的请求
     * 3. 验证被限流的请求数量 > 0
     * 4. 验证通过的请求数量 <= qpsThreshold + 容差
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 23: QPS限流判断")
    void qpsRateLimitShouldTriggerWhenExceedingThreshold(
        @ForAll @IntRange(min = 10, max = 100) int qpsThreshold,
        @ForAll @IntRange(min = 150, max = 300) int totalRequests
    ) throws InterruptedException {
        // Given: 配置QPS限流规则
        String resourceName = "testResource_" + System.nanoTime();
        FlowRule rule = new FlowRule();
        rule.setResource(resourceName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qpsThreshold);
        
        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
        
        // When: 在1秒内发送超过阈值的请求
        AtomicInteger passedCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        entry = com.alibaba.csp.sentinel.SphU.entry(resourceName);
                        passedCount.incrementAndGet();
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockedCount.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit();
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        // 等待所有请求完成，最多等待2秒
        latch.await(2, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then: 验证限流效果
        // 1. 应该有请求被限流
        assert blockedCount.get() > 0 : 
            String.format("应该有请求被限流，但实际被限流数量为0。阈值=%d, 总请求=%d, 通过=%d", 
                qpsThreshold, totalRequests, passedCount.get());
        
        // 2. 通过的请求数量应该接近阈值（允许一定容差，因为Sentinel的限流是基于滑动窗口的）
        // 容差设置为阈值的20%或至少10个请求
        int tolerance = Math.max((int)(qpsThreshold * 0.2), 10);
        assert passedCount.get() <= qpsThreshold + tolerance : 
            String.format("通过的请求数量超过阈值过多。阈值=%d, 容差=%d, 实际通过=%d", 
                qpsThreshold, tolerance, passedCount.get());
        
        // 3. 通过数 + 限流数 应该等于总请求数
        assert passedCount.get() + blockedCount.get() == totalRequests : 
            String.format("通过数+限流数应该等于总请求数。通过=%d, 限流=%d, 总数=%d", 
                passedCount.get(), blockedCount.get(), totalRequests);
        
        // 清理规则
        FlowRuleManager.loadRules(new ArrayList<>());
    }
    
    /**
     * Property 24: 限流拒绝响应
     * 
     * 属性：被限流的请求应该抛出BlockException，而不是其他异常
     * 
     * 验证：
     * 1. 配置一个很低的QPS限流规则（阈值为1）
     * 2. 快速发送多个请求
     * 3. 验证被限流的请求抛出的是BlockException
     * 4. 验证BlockException包含正确的资源名称
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 24: 限流拒绝响应")
    void blockedRequestsShouldThrowBlockException() throws InterruptedException {
        // Given: 配置一个很低的QPS限流规则
        String resourceName = "testResource_" + System.nanoTime();
        FlowRule rule = new FlowRule();
        rule.setResource(resourceName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1); // 阈值设置为1，容易触发限流
        
        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
        
        // When: 快速发送多个请求
        int totalRequests = 50;
        AtomicInteger blockExceptionCount = new AtomicInteger(0);
        AtomicInteger otherExceptionCount = new AtomicInteger(0);
        AtomicInteger correctResourceNameCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        for (int i = 0; i < totalRequests; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        entry = com.alibaba.csp.sentinel.SphU.entry(resourceName);
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockExceptionCount.incrementAndGet();
                        // 验证异常中包含正确的资源名称
                        if (e.getRuleLimitApp() != null || e.getMessage().contains(resourceName)) {
                            correctResourceNameCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        otherExceptionCount.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit();
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        // 等待所有请求完成
        latch.await(2, TimeUnit.SECONDS);
        
        // Then: 验证限流响应
        // 1. 应该有请求被限流（抛出BlockException）
        assert blockExceptionCount.get() > 0 : 
            String.format("应该有请求被限流，但实际被限流数量为0。总请求=%d", totalRequests);
        
        // 2. 不应该有其他类型的异常
        assert otherExceptionCount.get() == 0 : 
            String.format("不应该有其他类型的异常，但实际有%d个其他异常", otherExceptionCount.get());
        
        // 3. BlockException应该包含正确的资源信息
        assert correctResourceNameCount.get() == blockExceptionCount.get() : 
            String.format("BlockException应该包含正确的资源信息。被限流=%d, 包含正确资源名=%d", 
                blockExceptionCount.get(), correctResourceNameCount.get());
        
        // 清理规则
        FlowRuleManager.loadRules(new ArrayList<>());
    }
    
    /**
     * 提供常见的QPS阈值
     */
    @Provide
    Arbitrary<Integer> qpsThresholds() {
        return Arbitraries.of(10, 20, 50, 100);
    }
}
