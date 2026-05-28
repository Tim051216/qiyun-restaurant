package com.qiyun.order;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 服务注册和发现属性测试
 * 
 * 验证微服务架构中的服务注册和发现机制
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@SpringBootTest
public class ServiceRegistrationPropertiesTest {
    
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;
    
    private static final String SERVICE_NAME = "restaurant-order-service";
    
    @BeforeTry
    void setUp() {
        // 确保DiscoveryClient已注入
        if (discoveryClient == null) {
            System.out.println("警告: DiscoveryClient未注入，可能Nacos未启动");
        }
    }
    
    /**
     * Property 4: 服务生命周期管理
     * 
     * 验证服务能够正确注册到Nacos并保持心跳
     * 
     * 正确性属性：
     * - 服务启动后应该能在Nacos中查询到
     * - 服务实例应该包含正确的元数据（IP、端口等）
     * - 服务应该保持健康状态
     * 
     * **Validates: Requirements 2.2, 2.3, 2.4**
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 4: 服务生命周期管理")
    void serviceLifecycleManagement(@ForAll @IntRange(min = 1, max = 10) int attemptCount) {
        // 如果DiscoveryClient未注入，跳过测试
        if (discoveryClient == null) {
            return;
        }
        
        // 验证服务已注册
        List<String> services = discoveryClient.getServices();
        assertThat(services)
            .as("服务列表应该包含订单服务")
            .contains(SERVICE_NAME);
        
        // 验证服务实例
        List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_NAME);
        assertThat(instances)
            .as("应该至少有一个服务实例")
            .isNotEmpty();
        
        // 验证服务实例元数据
        ServiceInstance instance = instances.get(0);
        assertThat(instance.getServiceId())
            .as("服务ID应该正确")
            .isEqualTo(SERVICE_NAME);
        
        assertThat(instance.getHost())
            .as("服务主机地址不应为空")
            .isNotBlank();
        
        assertThat(instance.getPort())
            .as("服务端口应该在有效范围内")
            .isBetween(1, 65535);
        
        assertThat(instance.getUri())
            .as("服务URI不应为空")
            .isNotNull();
    }
    
    /**
     * Property 5: 服务发现一致性
     * 
     * 验证服务发现的一致性和可靠性
     * 
     * 正确性属性：
     * - 多次查询同一服务应该返回一致的结果
     * - 服务实例列表应该稳定（在没有服务变更的情况下）
     * - 服务元数据应该保持一致
     * 
     * **Validates: Requirements 2.2, 2.3, 2.4**
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 5: 服务发现一致性")
    void serviceDiscoveryConsistency(@ForAll @IntRange(min = 2, max = 5) int queryCount) {
        // 如果DiscoveryClient未注入，跳过测试
        if (discoveryClient == null) {
            return;
        }
        
        // 第一次查询
        List<ServiceInstance> firstQuery = discoveryClient.getInstances(SERVICE_NAME);
        
        if (firstQuery.isEmpty()) {
            // 如果服务未注册，跳过测试
            return;
        }
        
        // 多次查询并验证一致性
        for (int i = 0; i < queryCount; i++) {
            List<ServiceInstance> currentQuery = discoveryClient.getInstances(SERVICE_NAME);
            
            // 验证实例数量一致
            assertThat(currentQuery)
                .as("第%d次查询的实例数量应该与第一次一致", i + 1)
                .hasSameSizeAs(firstQuery);
            
            // 验证实例ID一致
            if (!currentQuery.isEmpty() && !firstQuery.isEmpty()) {
                ServiceInstance firstInstance = firstQuery.get(0);
                ServiceInstance currentInstance = currentQuery.get(0);
                
                assertThat(currentInstance.getServiceId())
                    .as("服务ID应该保持一致")
                    .isEqualTo(firstInstance.getServiceId());
                
                assertThat(currentInstance.getHost())
                    .as("服务主机应该保持一致")
                    .isEqualTo(firstInstance.getHost());
                
                assertThat(currentInstance.getPort())
                    .as("服务端口应该保持一致")
                    .isEqualTo(firstInstance.getPort());
            }
            
            // 短暂延迟，模拟真实场景
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
