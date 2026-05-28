package com.qiyun.order;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 熔断降级属性测试
 * 
 * 验证Sentinel熔断降级机制的正确性
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@SpringBootTest
public class CircuitBreakerPropertiesTest {
    
    /**
     * Property 7: 熔断降级响应
     * 
     * 验证熔断降级机制能够正确响应异常情况
     * 
     * 正确性属性：
     * - 熔断规则应该被正确加载
     * - 熔断规则应该包含必要的配置参数
     * - 错误率阈值应该在合理范围内（0-1之间）
     * - 熔断时长应该大于0
     * - 最小请求数应该大于0
     * 
     * **Validates: Requirements 2.7**
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 7: 熔断降级响应")
    void circuitBreakerDegradeResponse(@ForAll @IntRange(min = 1, max = 10) int ruleCount) {
        // 获取已加载的熔断降级规则
        List<DegradeRule> rules = DegradeRuleManager.getRules();
        
        // 验证规则已加载
        assertThat(rules)
            .as("应该有熔断降级规则被加载")
            .isNotEmpty();
        
        // 验证每个规则的配置
        for (DegradeRule rule : rules) {
            // 验证资源名称不为空
            assertThat(rule.getResource())
                .as("熔断规则的资源名称不应为空")
                .isNotBlank();
            
            // 验证熔断策略类型有效
            assertThat(rule.getGrade())
                .as("熔断策略类型应该有效")
                .isIn(0, 1, 2); // 0=慢调用比例, 1=异常比例, 2=异常数
            
            // 验证阈值合理性
            if (rule.getGrade() == 1) { // 异常比例
                assertThat(rule.getCount())
                    .as("异常比例阈值应该在0-1之间")
                    .isBetween(0.0, 1.0);
            } else if (rule.getGrade() == 0) { // 慢调用比例
                assertThat(rule.getCount())
                    .as("慢调用响应时间阈值应该大于0")
                    .isGreaterThan(0);
            }
            
            // 验证熔断时长
            assertThat(rule.getTimeWindow())
                .as("熔断时长应该大于0秒")
                .isGreaterThan(0);
            
            // 验证最小请求数
            assertThat(rule.getMinRequestAmount())
                .as("最小请求数应该大于0")
                .isGreaterThan(0);
            
            // 验证统计时长
            assertThat(rule.getStatIntervalMs())
                .as("统计时长应该大于0毫秒")
                .isGreaterThan(0);
        }
        
        // 验证规则数量合理
        assertThat(rules.size())
            .as("熔断规则数量应该合理")
            .isGreaterThan(0)
            .isLessThanOrEqualTo(100);
    }
}
