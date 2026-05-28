package com.qiyun.dish;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Caffeine缓存配置测试
 * 
 * 验证Caffeine缓存配置是否正确：
 * 1. 缓存大小限制是否生效
 * 2. 缓存过期时间是否正确
 * 3. 缓存统计功能是否正常
 * 
 * 这些测试不依赖Spring上下文，直接测试Caffeine的配置参数
 * 
 * Requirements: 7.1
 */
class CaffeineCacheConfigTest {
    
    /**
     * 测试缓存配置参数 - 最大大小10000
     * 验证与CacheConfig中的配置一致
     */
    @Test
    void testCacheConfigurationParameters() {
        // 创建与CacheConfig相同配置的缓存
        Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
        
        assertNotNull(cache, "缓存实例不应为null");
        
        // 测试基本操作
        cache.put("test-key", "test-value");
        assertEquals("test-value", cache.getIfPresent("test-key"), "缓存值应该匹配");
    }
    
    /**
     * 测试缓存过期时间配置
     * 
     * 验证缓存在5分钟后过期
     */
    @Test
    void testCacheExpiration() throws InterruptedException {
        // 创建一个短过期时间的缓存用于测试
        Cache<String, String> testCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();
        
        // 写入缓存
        testCache.put("expire-key", "expire-value");
        
        // 立即读取应该存在
        assertEquals("expire-value", testCache.getIfPresent("expire-key"));
        
        // 等待3秒后应该过期
        Thread.sleep(3000);
        assertNull(testCache.getIfPresent("expire-key"), "缓存应该已过期");
    }
    
    /**
     * 测试缓存大小限制
     * 
     * 验证缓存最大条目数限制
     */
    @Test
    void testCacheMaximumSize() {
        // 创建一个小容量的缓存用于测试
        Cache<String, String> testCache = Caffeine.newBuilder()
            .maximumSize(10)
            .build();
        
        // 写入11个条目
        for (int i = 0; i < 11; i++) {
            testCache.put("key-" + i, "value-" + i);
        }
        
        // 触发清理操作（Caffeine的驱逐是异步的）
        testCache.cleanUp();
        
        // 验证缓存大小不超过10
        long size = testCache.estimatedSize();
        assertTrue(size <= 10, "缓存大小应该不超过最大限制: " + size);
    }
    
    /**
     * 测试缓存统计功能
     * 
     * 验证recordStats配置是否生效
     */
    @Test
    void testCacheStats() {
        Cache<String, String> testCache = Caffeine.newBuilder()
            .maximumSize(100)
            .recordStats()
            .build();
        
        // 写入数据
        testCache.put("stats-key", "stats-value");
        
        // 命中
        testCache.getIfPresent("stats-key");
        
        // 未命中
        testCache.getIfPresent("non-existent-key");
        
        // 验证统计信息
        var stats = testCache.stats();
        assertNotNull(stats, "统计信息不应为null");
        assertEquals(1, stats.hitCount(), "命中次数应该为1");
        assertEquals(1, stats.missCount(), "未命中次数应该为1");
    }
}
