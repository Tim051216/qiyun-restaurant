package com.qiyun.restaurant.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 限流工具类
 * 
 * 面试要点：
 * 1. 令牌桶算法
 * 2. 使用Lua脚本保证原子性
 * 3. 支持分布式限流
 */
@Component
@Slf4j
public class RateLimiterUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua脚本 - 令牌桶算法
     * 
     * KEYS[1]: 限流key
     * ARGV[1]: 令牌桶容量
     * ARGV[2]: 每秒生成令牌数
     * ARGV[3]: 当前时间戳（秒）
     * ARGV[4]: 需要的令牌数
     */
    private static final String LUA_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local capacity = tonumber(ARGV[1])\n" +
        "local rate = tonumber(ARGV[2])\n" +
        "local now = tonumber(ARGV[3])\n" +
        "local requested = tonumber(ARGV[4])\n" +
        "\n" +
        "local bucket = redis.call('hmget', key, 'tokens', 'last_time')\n" +
        "local tokens = tonumber(bucket[1])\n" +
        "local last_time = tonumber(bucket[2])\n" +
        "\n" +
        "if tokens == nil then\n" +
        "    tokens = capacity\n" +
        "    last_time = now\n" +
        "end\n" +
        "\n" +
        "local delta = math.max(0, now - last_time)\n" +
        "local new_tokens = math.min(capacity, tokens + delta * rate)\n" +
        "\n" +
        "if new_tokens >= requested then\n" +
        "    new_tokens = new_tokens - requested\n" +
        "    redis.call('hmset', key, 'tokens', new_tokens, 'last_time', now)\n" +
        "    redis.call('expire', key, 3600)\n" +
        "    return 1\n" +
        "else\n" +
        "    redis.call('hmset', key, 'tokens', new_tokens, 'last_time', now)\n" +
        "    redis.call('expire', key, 3600)\n" +
        "    return 0\n" +
        "end";

    /**
     * 尝试获取令牌
     * 
     * @param key 限流key
     * @param capacity 令牌桶容量
     * @param rate 每秒生成令牌数
     * @param requested 需要的令牌数
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int capacity, int rate, int requested) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        
        long now = System.currentTimeMillis() / 1000;
        
        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(key),
            capacity,
            rate,
            now,
            requested
        );
        
        return result != null && result == 1;
    }

    /**
     * 尝试获取1个令牌
     */
    public boolean tryAcquire(String key, int capacity, int rate) {
        return tryAcquire(key, capacity, rate, 1);
    }

    /**
     * 简单限流（固定窗口）
     * 
     * @param key 限流key
     * @param maxCount 最大请求数
     * @param window 时间窗口（秒）
     * @return 是否允许通过
     */
    public boolean simpleRateLimit(String key, int maxCount, int window) {
        String limitKey = "rate_limit:" + key;
        
        Long count = redisTemplate.opsForValue().increment(limitKey);
        
        if (count == null) {
            return false;
        }
        
        if (count == 1) {
            redisTemplate.expire(limitKey, window, TimeUnit.SECONDS);
        }
        
        return count <= maxCount;
    }

    /**
     * 滑动窗口限流
     * 
     * @param key 限流key
     * @param maxCount 最大请求数
     * @param window 时间窗口（秒）
     * @return 是否允许通过
     */
    public boolean slidingWindowRateLimit(String key, int maxCount, int window) {
        String limitKey = "sliding_window:" + key;
        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;
        
        // 移除窗口外的数据
        redisTemplate.opsForZSet().removeRangeByScore(limitKey, 0, windowStart);
        
        // 获取当前窗口内的请求数
        Long count = redisTemplate.opsForZSet().zCard(limitKey);
        
        if (count != null && count >= maxCount) {
            return false;
        }
        
        // 添加当前请求
        redisTemplate.opsForZSet().add(limitKey, String.valueOf(now), now);
        redisTemplate.expire(limitKey, window, TimeUnit.SECONDS);
        
        return true;
    }
}
