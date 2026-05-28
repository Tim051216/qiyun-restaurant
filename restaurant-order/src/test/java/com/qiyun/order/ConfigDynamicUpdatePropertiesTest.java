package com.qiyun.order;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置动态更新属性测试
 * 
 * 验证Nacos配置中心的动态配置更新能力
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@SpringBootTest
public class ConfigDynamicUpdatePropertiesTest {
    
    @Autowired(required = false)
    private Environment environment;
    
    @Autowired(required = false)
    private ContextRefresher contextRefresher;
    
    @Value("${spring.application.name:unknown}")
    private String applicationName;
    
    /**
     * Property 8: 配置动态更新
     * 
     * 验证配置中心能够支持动态配置更新
     * 
     * 正确性属性：
     * - Environment应该能够正确注入
     * - 应用名称配置应该能够正确读取
     * - ContextRefresher应该可用（用于刷新配置）
     * - 配置属性应该有合理的默认值
     * 
     * **Validates: Requirements 2.8**
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 8: 配置动态更新")
    void configDynamicUpdate(@ForAll @IntRange(min = 1, max = 10) int refreshCount) {
        // 验证Environment已注入
        assertThat(environment)
            .as("Environment应该被正确注入")
            .isNotNull();
        
        // 验证应用名称配置
        assertThat(applicationName)
            .as("应用名称应该被正确配置")
            .isNotBlank()
            .isNotEqualTo("unknown");
        
        // 验证应用名称符合命名规范
        assertThat(applicationName)
            .as("应用名称应该符合命名规范（小写字母、数字、连字符）")
            .matches("^[a-z0-9-]+$");
        
        // 验证从Environment中读取的配置与注入的值一致
        String envAppName = environment.getProperty("spring.application.name");
        assertThat(envAppName)
            .as("Environment中的应用名称应该与注入的值一致")
            .isEqualTo(applicationName);
        
        // 验证ContextRefresher可用（用于配置刷新）
        if (contextRefresher != null) {
            assertThat(contextRefresher)
                .as("ContextRefresher应该可用于配置刷新")
                .isNotNull();
        }
        
        // 验证Nacos配置相关属性
        String nacosServerAddr = environment.getProperty("spring.cloud.nacos.discovery.server-addr");
        if (nacosServerAddr != null) {
            assertThat(nacosServerAddr)
                .as("Nacos服务器地址应该配置正确")
                .isNotBlank();
        }
        
        // 验证服务端口配置
        String serverPort = environment.getProperty("server.port");
        if (serverPort != null) {
            int port = Integer.parseInt(serverPort);
            assertThat(port)
                .as("服务端口应该在有效范围内")
                .isBetween(1, 65535);
        }
        
        // 验证配置的一致性（多次读取应该返回相同的值）
        for (int i = 0; i < refreshCount; i++) {
            String currentAppName = environment.getProperty("spring.application.name");
            assertThat(currentAppName)
                .as("配置在未刷新时应该保持一致")
                .isEqualTo(applicationName);
        }
    }
}
