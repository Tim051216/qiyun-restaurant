package com.qiyun.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sentinel配置类
 * 
 * 配置限流和熔断降级规则
 * 
 * 规则持久化到Nacos：
 * - 流控规则：restaurant-order-service-flow-rules
 * - 降级规则：restaurant-order-service-degrade-rules
 * - 热点参数规则：restaurant-order-service-param-flow-rules
 * 
 * Sentinel Dashboard连接：
 * - Dashboard地址：localhost:8858
 * - 客户端端口：8719
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@Configuration
public class SentinelConfig {
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${spring.cloud.sentinel.transport.dashboard:localhost:8858}")
    private String sentinelDashboard;
    
    /**
     * 初始化Sentinel规则
     * 
     * 包括：
     * 1. QPS限流规则
     * 2. 熔断降级规则
     * 3. 热点参数限流规则
     * 
     * 注意：规则会自动从Nacos加载，这里的初始化规则仅作为默认配置
     * 如果Nacos中已有规则配置，会优先使用Nacos中的规则
     */
    @PostConstruct
    public void initRules() {
        log.info("开始初始化Sentinel规则...");
        log.info("应用名称: {}", applicationName);
        log.info("Sentinel Dashboard: {}", sentinelDashboard);
        
        initFlowRules();
        initDegradeRules();
        initParamFlowRules();
        
        log.info("Sentinel规则初始化完成");
        log.info("规则将持久化到Nacos配置中心");
        log.info("可通过Sentinel Dashboard ({}) 查看和管理规则", sentinelDashboard);
    }
    
    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 订单创建QPS限流：每秒100个请求
        FlowRule orderCreateRule = new FlowRule();
        orderCreateRule.setResource("orderCreate");
        orderCreateRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderCreateRule.setCount(100);
        rules.add(orderCreateRule);
        
        // 订单查询QPS限流：每秒200个请求
        FlowRule orderQueryRule = new FlowRule();
        orderQueryRule.setResource("orderQuery");
        orderQueryRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderQueryRule.setCount(200);
        rules.add(orderQueryRule);
        
        // 订单支付QPS限流：每秒50个请求
        FlowRule orderPayRule = new FlowRule();
        orderPayRule.setResource("orderPay");
        orderPayRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderPayRule.setCount(50);
        rules.add(orderPayRule);
        
        // 订单取消QPS限流：每秒30个请求
        FlowRule orderCancelRule = new FlowRule();
        orderCancelRule.setResource("orderCancel");
        orderCancelRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderCancelRule.setCount(30);
        rules.add(orderCancelRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("限流规则加载完成，共{}条规则", rules.size());
    }
    
    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        
        // 菜品服务调用熔断：错误率超过50%时熔断10秒
        DegradeRule dishServiceRule = new DegradeRule();
        dishServiceRule.setResource("DishServiceClient#getDishById(Long)");
        dishServiceRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        dishServiceRule.setCount(0.5); // 错误率50%
        dishServiceRule.setTimeWindow(10); // 熔断时长10秒
        dishServiceRule.setMinRequestAmount(5); // 最小请求数
        dishServiceRule.setStatIntervalMs(1000); // 统计时长1秒
        rules.add(dishServiceRule);
        
        // 慢调用熔断：响应时间超过1秒的比例超过50%时熔断
        DegradeRule slowCallRule = new DegradeRule();
        slowCallRule.setResource("orderCreate");
        slowCallRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        slowCallRule.setCount(1000); // 响应时间阈值1000ms
        slowCallRule.setTimeWindow(10); // 熔断时长10秒
        slowCallRule.setMinRequestAmount(5);
        slowCallRule.setStatIntervalMs(1000);
        rules.add(slowCallRule);
        
        DegradeRuleManager.loadRules(rules);
        log.info("熔断降级规则加载完成，共{}条规则", rules.size());
    }
    
    /**
     * 初始化热点参数限流规则
     */
    private void initParamFlowRules() {
        List<ParamFlowRule> rules = new ArrayList<>();
        
        // 菜品查询热点参数限流：针对dishId参数
        ParamFlowRule dishRule = new ParamFlowRule();
        dishRule.setResource("getDish");
        dishRule.setParamIdx(0); // 第一个参数（dishId）
        dishRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        dishRule.setCount(10); // 单个菜品每秒10个请求
        dishRule.setDurationInSec(1);
        
        // 配置特殊商品的限流阈值（热门商品可以有更高的阈值）
        ParamFlowItem item1 = new ParamFlowItem();
        item1.setObject("1001"); // 热门商品ID
        item1.setClassType(Long.class.getName());
        item1.setCount(50); // 热门商品每秒50个请求
        
        ParamFlowItem item2 = new ParamFlowItem();
        item2.setObject("1002"); // 热门商品ID
        item2.setClassType(Long.class.getName());
        item2.setCount(50);
        
        dishRule.setParamFlowItemList(List.of(item1, item2));
        rules.add(dishRule);
        
        ParamFlowRuleManager.loadRules(rules);
        log.info("热点参数限流规则加载完成，共{}条规则", rules.size());
    }
}
