package com.qiyun.dish.config;

// 暂时禁用Redisson配置，与Spring Boot 3.2存在兼容性问题
/*
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 
 * 配置Redisson客户端，用于实现分布式锁等功能。
 * 
 * 功能：
 * - 配置Redis连接信息
 * - 创建RedissonClient Bean
 * - 支持单机模式和集群模式
 * 
 * Requirements: 7.7
 * 
 * @author qiyun
 * @since 2026-02-08
 *//*
@Configuration
public class RedissonConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private Integer redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.data.redis.database:0}")
    private Integer redisDatabase;
    
    /**
     * 创建RedissonClient Bean
     * 
     * 配置说明：
     * - 使用单机模式（Single Server Mode）
     * - 连接池大小：64
     * - 最小空闲连接：10
     * - 连接超时：10秒
     * - 命令超时：3秒
     * - 重试次数：3次
     * 
     * @return RedissonClient实例
     *//*
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单机模式配置
        String address = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
            .setAddress(address)
            .setDatabase(redisDatabase)
            .setPassword(redisPassword.isEmpty() ? null : redisPassword)
            .setConnectionPoolSize(64)          // 连接池大小
            .setConnectionMinimumIdleSize(10)   // 最小空闲连接数
            .setConnectTimeout(10000)           // 连接超时10秒
            .setTimeout(3000)                   // 命令超时3秒
            .setRetryAttempts(3)                // 重试次数
            .setRetryInterval(1500);            // 重试间隔1.5秒
        
        return Redisson.create(config);
    }
}
*/