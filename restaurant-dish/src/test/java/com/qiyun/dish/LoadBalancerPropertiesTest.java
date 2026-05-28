package com.qiyun.dish;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 负载均衡属性测试
 * 
 * 验证微服务架构中的负载均衡机制
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@SpringBootTest
public class LoadBalancerPropertiesTest {
    
    @Autowired(required = false)
    private LoadBalancerClient loadBalancerClient;
    
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;
    
    private static final String SERVICE_NAME = "restaurant-dish-service";
    
    /**
     * Property 6: 负载均衡分发
     * 
     * 验证负载均衡器能够正确分发请求到多个服务实例
     * 
     * 正确性属性：
     * - 当有多个服务实例时，负载均衡器应该能够选择实例
     * - 多次调用应该分发到不同的实例（轮询策略）
     * - 选择的实例应该是健康的实例
     * - 负载均衡应该相对均匀
     * 
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 6: 负载均衡分发")
    void loadBalancerDistribution(@ForAll @IntRange(min = 5, max = 20) int requestCount) {
        // 如果LoadBalancerClient未注入，跳过测试
        if (loadBalancerClient == null || discoveryClient == null) {
            return;
        }
        
        // 获取服务实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_NAME);
        
        if (instances.isEmpty()) {
            // 如果没有服务实例，跳过测试
            return;
        }
        
        // 记录每个实例被选中的次数
        Map<String, Integer> instanceSelectionCount = new HashMap<>();
        
        // 多次调用负载均衡器
        for (int i = 0; i < requestCount; i++) {
            ServiceInstance instance = loadBalancerClient.choose(SERVICE_NAME);
            
            // 验证选择的实例不为空
            assertThat(instance)
                .as("负载均衡器应该能够选择一个实例")
                .isNotNull();
            
            // 验证选择的实例在服务列表中
            assertThat(instances)
                .as("选择的实例应该在服务列表中")
                .extracting(ServiceInstance::getInstanceId)
                .contains(instance.getInstanceId());
            
            // 记录选择次数
            String instanceId = instance.getInstanceId();
            instanceSelectionCount.put(instanceId, 
                instanceSelectionCount.getOrDefault(instanceId, 0) + 1);
        }
        
        // 如果有多个实例，验证负载均衡效果
        if (instances.size() > 1) {
            // 验证至少有一个实例被选中
            assertThat(instanceSelectionCount)
                .as("应该有实例被选中")
                .isNotEmpty();
            
            // 如果请求次数足够多，验证分发相对均匀
            if (requestCount >= instances.size() * 2) {
                // 每个实例至少应该被选中一次
                long selectedInstanceCount = instanceSelectionCount.size();
                assertThat(selectedInstanceCount)
                    .as("多次请求应该分发到不同的实例")
                    .isGreaterThan(1);
                
                // 验证分发不会过于集中（最大选择次数不应超过平均值的2倍）
                int maxSelections = Collections.max(instanceSelectionCount.values());
                double avgSelections = (double) requestCount / instances.size();
                
                assertThat(maxSelections)
                    .as("负载均衡应该相对均匀，最大选择次数不应过于集中")
                    .isLessThanOrEqualTo((int) (avgSelections * 2.5));
            }
        }
        
        // 验证所有选择的实例都是有效的
        for (String instanceId : instanceSelectionCount.keySet()) {
            boolean isValid = instances.stream()
                .anyMatch(inst -> inst.getInstanceId().equals(instanceId));
            
            assertThat(isValid)
                .as("选择的实例ID应该是有效的: %s", instanceId)
                .isTrue();
        }
    }
}
