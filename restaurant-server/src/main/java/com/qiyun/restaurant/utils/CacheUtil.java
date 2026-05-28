package com.qiyun.restaurant.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类
 * 
 * 面试要点：
 * 1. 缓存穿透：布隆过滤器 + 空值缓存
 * 2. 缓存击穿：互斥锁（分布式锁）
 * 3. 缓存雪崩：过期时间随机化
 */
@Component
@Slf4j
public class CacheUtil {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DistributedLockUtil lockUtil;

    // 布隆过滤器 - 解决缓存穿透
    private BloomFilter<String> bloomFilter;

    // 空值缓存时间（秒）
    private static final long NULL_CACHE_EXPIRE = 60;

    // 默认缓存时间（秒）
    private static final long DEFAULT_CACHE_EXPIRE = 3600;

    // 随机过期时间范围（秒）
    private static final long RANDOM_EXPIRE_RANGE = 300;

    @PostConstruct
    public void init() {
        // 初始化布隆过滤器，预计100万个元素，误判率0.01
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                1000000,
                0.01
        );
        log.info("布隆过滤器初始化完成");
    }

    /**
     * 添加到布隆过滤器
     */
    public void addToBloomFilter(String key) {
        bloomFilter.put(key);
    }

    /**
     * 判断key是否可能存在
     */
    public boolean mightContain(String key) {
        return bloomFilter.mightContain(key);
    }

    /**
     * 获取缓存（解决缓存穿透、击穿、雪崩）
     * 
     * @param key 缓存key
     * @param type 返回类型
     * @param dbFallback 数据库查询回调
     * @param <T> 泛型
     * @return 缓存数据
     */
    public <T> T getWithPassThrough(String key, Class<T> type, Supplier<T> dbFallback) {
        return getWithPassThrough(key, type, dbFallback, DEFAULT_CACHE_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存（解决缓存穿透、击穿、雪崩）
     * 
     * @param key 缓存key
     * @param type 返回类型
     * @param dbFallback 数据库查询回调
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param <T> 泛型
     * @return 缓存数据
     */
    public <T> T getWithPassThrough(String key, Class<T> type, Supplier<T> dbFallback, 
                                    long timeout, TimeUnit unit) {
        // 1. 布隆过滤器判断（防止缓存穿透）
        if (!mightContain(key)) {
            log.info("布隆过滤器判断key不存在: {}", key);
            return null;
        }

        // 2. 查询Redis缓存
        Object cacheData = redisUtil.get(key);
        
        // 3. 缓存命中
        if (cacheData != null) {
            // 判断是否是空值缓存
            if ("".equals(cacheData)) {
                return null;
            }
            return type.cast(cacheData);
        }

        // 4. 缓存未命中，使用分布式锁防止缓存击穿
        String lockKey = "lock:" + key;
        boolean acquired = lockUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
        
        if (!acquired) {
            // 获取锁失败，等待一段时间后重试
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getWithPassThrough(key, type, dbFallback, timeout, unit);
        }

        try {
            // 5. 双重检查，再次查询缓存
            cacheData = redisUtil.get(key);
            if (cacheData != null) {
                if ("".equals(cacheData)) {
                    return null;
                }
                return type.cast(cacheData);
            }

            // 6. 查询数据库
            T data = dbFallback.get();

            // 7. 数据不存在，缓存空值（防止缓存穿透）
            if (data == null) {
                redisUtil.set(key, "", NULL_CACHE_EXPIRE, TimeUnit.SECONDS);
                return null;
            }

            // 8. 数据存在，写入缓存（添加随机过期时间，防止缓存雪崩）
            long randomExpire = timeout + new Random().nextInt((int) RANDOM_EXPIRE_RANGE);
            redisUtil.set(key, data, randomExpire, unit);

            return data;
        } finally {
            // 9. 释放锁
            lockUtil.unlock(lockKey);
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisUtil.delete(key);
    }

    /**
     * 批量删除缓存（模糊匹配）
     */
    public void deletePattern(String pattern) {
        // 注意：生产环境慎用，可能影响性能
        log.warn("批量删除缓存: {}", pattern);
    }

    /**
     * 预热缓存
     */
    public <T> void warmUp(String key, T data, long timeout, TimeUnit unit) {
        // 添加到布隆过滤器
        addToBloomFilter(key);
        
        // 写入缓存
        long randomExpire = timeout + new Random().nextInt((int) RANDOM_EXPIRE_RANGE);
        redisUtil.set(key, data, randomExpire, unit);
        
        log.info("缓存预热完成: {}", key);
    }
}
