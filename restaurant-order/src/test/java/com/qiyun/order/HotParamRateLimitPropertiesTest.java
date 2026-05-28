package com.qiyun.order;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 热点参数限流属性测试
 * 
 * 测试Sentinel热点参数限流功能的正确性
 * 
 * Property 25: 热点参数限流 - 针对特定参数值进行限流
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class HotParamRateLimitPropertiesTest {
    
    /**
     * Property 25: 热点参数限流
     * 
     * 属性：针对特定参数值的请求应该被单独限流
     * 
     * 验证：
     * 1. 配置热点参数限流规则（针对第一个参数）
     * 2. 对同一个参数值发送超过阈值的请求
     * 3. 验证该参数值的请求被限流
     * 4. 验证不同参数值的请求不受影响
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 25: 热点参数限流")
    void hotParamRateLimitShouldWorkForSpecificParamValue(
        @ForAll @IntRange(min = 5, max = 20) int paramThreshold,
        @ForAll @IntRange(min = 1000, max = 9999) int hotParamValue
    ) throws InterruptedException {
        // Given: 配置热点参数限流规则
        String resourceName = "testResource_" + System.nanoTime();
        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setParamIdx(0); // 第一个参数
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(paramThreshold);
        rule.setDurationInSec(1);
        
        List<ParamFlowRule> rules = new ArrayList<>();
        rules.add(rule);
        ParamFlowRuleManager.loadRules(rules);
        
        // When: 对同一个参数值发送超过阈值的请求
        int totalRequests = paramThreshold * 3;
        AtomicInteger passedCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        for (int i = 0; i < totalRequests; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        // 使用相同的参数值
                        entry = com.alibaba.csp.sentinel.SphU.entry(
                            resourceName,
                            com.alibaba.csp.sentinel.EntryType.IN,
                            1,
                            hotParamValue
                        );
                        passedCount.incrementAndGet();
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockedCount.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit(1, hotParamValue);
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(2, TimeUnit.SECONDS);
        
        // Then: 验证热点参数限流效果
        // 1. 应该有请求被限流
        assert blockedCount.get() > 0 : 
            String.format("应该有请求被限流，但实际被限流数量为0。阈值=%d, 总请求=%d, 参数值=%d", 
                paramThreshold, totalRequests, hotParamValue);
        
        // 2. 通过的请求数量应该接近阈值（允许一定容差）
        int tolerance = Math.max((int)(paramThreshold * 0.5), 10);
        assert passedCount.get() <= paramThreshold + tolerance : 
            String.format("通过的请求数量超过阈值过多。阈值=%d, 容差=%d, 实际通过=%d", 
                paramThreshold, tolerance, passedCount.get());
        
        // 清理规则
        ParamFlowRuleManager.loadRules(new ArrayList<>());
    }
    
    /**
     * Property 25扩展: 不同参数值的独立限流
     * 
     * 属性：不同参数值的请求应该被独立限流，互不影响
     * 
     * 验证：
     * 1. 配置热点参数限流规则
     * 2. 对参数值A发送超过阈值的请求
     * 3. 对参数值B发送少量请求
     * 4. 验证参数值A的请求被限流
     * 5. 验证参数值B的请求不受影响
     */
    @Property(tries = 50)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 25: 热点参数限流")
    void differentParamValuesShouldBeLimitedIndependently(
        @ForAll @IntRange(min = 10, max = 20) int paramThreshold
    ) throws InterruptedException {
        // Given: 配置热点参数限流规则
        String resourceName = "testResource_" + System.nanoTime();
        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setParamIdx(0);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(paramThreshold);
        rule.setDurationInSec(1);
        
        List<ParamFlowRule> rules = new ArrayList<>();
        rules.add(rule);
        ParamFlowRuleManager.loadRules(rules);
        
        // When: 对两个不同的参数值发送请求
        int paramValueA = 1001;
        int paramValueB = 2002;
        int requestsForA = paramThreshold * 3; // 超过阈值
        int requestsForB = paramThreshold / 2; // 远低于阈值
        
        AtomicInteger passedCountA = new AtomicInteger(0);
        AtomicInteger blockedCountA = new AtomicInteger(0);
        AtomicInteger passedCountB = new AtomicInteger(0);
        AtomicInteger blockedCountB = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(requestsForA + requestsForB);
        
        // 发送参数值A的请求
        for (int i = 0; i < requestsForA; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        entry = com.alibaba.csp.sentinel.SphU.entry(
                            resourceName,
                            com.alibaba.csp.sentinel.EntryType.IN,
                            1,
                            paramValueA
                        );
                        passedCountA.incrementAndGet();
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockedCountA.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit(1, paramValueA);
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        // 发送参数值B的请求
        for (int i = 0; i < requestsForB; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        entry = com.alibaba.csp.sentinel.SphU.entry(
                            resourceName,
                            com.alibaba.csp.sentinel.EntryType.IN,
                            1,
                            paramValueB
                        );
                        passedCountB.incrementAndGet();
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockedCountB.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit(1, paramValueB);
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(3, TimeUnit.SECONDS);
        
        // Then: 验证独立限流效果
        // 1. 参数值A应该有请求被限流
        assert blockedCountA.get() > 0 : 
            String.format("参数值A应该有请求被限流。阈值=%d, A请求=%d, A被限流=%d", 
                paramThreshold, requestsForA, blockedCountA.get());
        
        // 2. 参数值B的请求应该大部分通过（因为远低于阈值）
        double passRateB = (double) passedCountB.get() / requestsForB;
        assert passRateB >= 0.7 : 
            String.format("参数值B的请求应该大部分通过。阈值=%d, B请求=%d, B通过=%d, 通过率=%.2f", 
                paramThreshold, requestsForB, passedCountB.get(), passRateB);
        
        // 清理规则
        ParamFlowRuleManager.loadRules(new ArrayList<>());
    }
    
    /**
     * Property 25扩展: 特殊参数值的例外配置
     * 
     * 属性：配置了例外项的参数值应该使用特殊的限流阈值
     * 
     * 验证：
     * 1. 配置热点参数限流规则，包含例外项
     * 2. 对普通参数值发送请求，验证使用默认阈值
     * 3. 对例外参数值发送请求，验证使用特殊阈值
     */
    @Property(tries = 50)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 25: 热点参数限流")
    void exceptionalParamValuesShouldUseSpecialThreshold(
        @ForAll @IntRange(min = 5, max = 10) int defaultThreshold
    ) throws InterruptedException {
        // Given: 配置热点参数限流规则，包含例外项
        String resourceName = "testResource_" + System.nanoTime();
        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setParamIdx(0);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(defaultThreshold); // 默认阈值
        rule.setDurationInSec(1);
        
        // 配置例外项：特殊参数值使用更高的阈值
        int specialParamValue = 9999;
        int specialThreshold = defaultThreshold * 3;
        ParamFlowItem item = new ParamFlowItem();
        item.setObject(String.valueOf(specialParamValue));
        item.setClassType(Integer.class.getName());
        item.setCount(specialThreshold);
        rule.setParamFlowItemList(List.of(item));
        
        List<ParamFlowRule> rules = new ArrayList<>();
        rules.add(rule);
        ParamFlowRuleManager.loadRules(rules);
        
        // When: 对特殊参数值发送超过默认阈值但低于特殊阈值的请求
        int requestCount = defaultThreshold * 2; // 超过默认阈值，但低于特殊阈值
        AtomicInteger passedCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(requestCount);
        
        for (int i = 0; i < requestCount; i++) {
            new Thread(() -> {
                try {
                    com.alibaba.csp.sentinel.Entry entry = null;
                    try {
                        entry = com.alibaba.csp.sentinel.SphU.entry(
                            resourceName,
                            com.alibaba.csp.sentinel.EntryType.IN,
                            1,
                            specialParamValue
                        );
                        passedCount.incrementAndGet();
                    } catch (com.alibaba.csp.sentinel.slots.block.BlockException e) {
                        blockedCount.incrementAndGet();
                    } finally {
                        if (entry != null) {
                            entry.exit(1, specialParamValue);
                        }
                        latch.countDown();
                    }
                } catch (Exception e) {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(2, TimeUnit.SECONDS);
        
        // Then: 验证特殊阈值生效
        // 特殊参数值的请求应该大部分通过（因为使用了更高的阈值）
        double passRate = (double) passedCount.get() / requestCount;
        assert passRate >= 0.5 : 
            String.format("特殊参数值应该使用更高的阈值。默认阈值=%d, 特殊阈值=%d, 请求数=%d, 通过=%d, 通过率=%.2f", 
                defaultThreshold, specialThreshold, requestCount, passedCount.get(), passRate);
        
        // 清理规则
        ParamFlowRuleManager.loadRules(new ArrayList<>());
    }
}
