package com.qiyun.dish.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 * 
 * 配置说明：
 * - maximumSize: 缓存最大条目数为10000
 * - expireAfterWrite: 写入后5分钟过期
 * - recordStats: 启用统计信息收集
 * 
 * Requirements: 7.1
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * 配置Caffeine缓存管理器
     * 
     * @return CacheManager实例
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)                          // 最大缓存条目数
            .expireAfterWrite(5, TimeUnit.MINUTES)       // 写入后5分钟过期
            .recordStats());                              // 启用统计信息
        return cacheManager;
    }
}
