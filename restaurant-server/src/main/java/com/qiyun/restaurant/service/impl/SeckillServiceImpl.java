package com.qiyun.restaurant.service.impl;

import com.qiyun.restaurant.service.SeckillService;
import com.qiyun.restaurant.utils.DistributedLockUtil;
import com.qiyun.restaurant.utils.RateLimiterUtil;
import com.qiyun.restaurant.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀Service实现
 * 
 * 面试要点：
 * 1. 超卖问题：使用Redis原子操作（decr）+ Lua脚本
 * 2. 分布式锁：防止重复下单
 * 3. 限流：防止恶意刷单
 * 4. 缓存预热：提前将库存加载到Redis
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DistributedLockUtil lockUtil;

    @Autowired
    private RateLimiterUtil rateLimiterUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 库存key前缀
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    
    // 用户购买记录key前缀
    private static final String USER_BUY_KEY_PREFIX = "seckill:user:";
    
    // 分布式锁key前缀
    private static final String LOCK_KEY_PREFIX = "seckill:lock:";

    /**
     * Lua脚本 - 扣减库存（原子操作）
     * 
     * KEYS[1]: 库存key
     * KEYS[2]: 用户购买记录key
     * ARGV[1]: 会员ID
     * ARGV[2]: 扣减数量
     * 
     * 返回值：
     * 1: 成功
     * -1: 库存不足
     * -2: 用户已购买
     */
    private static final String DEDUCT_STOCK_SCRIPT =
        "local stock = redis.call('get', KEYS[1])\n" +
        "if not stock or tonumber(stock) <= 0 then\n" +
        "    return -1\n" +
        "end\n" +
        "\n" +
        "local hasBought = redis.call('sismember', KEYS[2], ARGV[1])\n" +
        "if hasBought == 1 then\n" +
        "    return -2\n" +
        "end\n" +
        "\n" +
        "local quantity = tonumber(ARGV[2])\n" +
        "if tonumber(stock) < quantity then\n" +
        "    return -1\n" +
        "end\n" +
        "\n" +
        "redis.call('decrby', KEYS[1], quantity)\n" +
        "redis.call('sadd', KEYS[2], ARGV[1])\n" +
        "return 1";

    @Override
    public boolean seckillOrder(Long activityId, Long memberId) {
        // 1. 限流检查（每个用户每秒最多1次请求）
        String rateLimitKey = "seckill:rate:" + memberId;
        if (!rateLimiterUtil.simpleRateLimit(rateLimitKey, 1, 1)) {
            log.warn("用户{}请求过于频繁", memberId);
            return false;
        }

        // 2. 检查库存（快速失败）
        String stockKey = STOCK_KEY_PREFIX + activityId;
        Object stock = redisUtil.get(stockKey);
        if (stock == null || Integer.parseInt(stock.toString()) <= 0) {
            log.info("活动{}库存不足", activityId);
            return false;
        }

        // 3. 使用Lua脚本扣减库存（原子操作，解决超卖问题）
        String userBuyKey = USER_BUY_KEY_PREFIX + activityId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(DEDUCT_STOCK_SCRIPT, Long.class);
        
        Long result = redisTemplate.execute(
            script,
            java.util.Arrays.asList(stockKey, userBuyKey),
            memberId.toString(),
            "1"
        );

        if (result == null) {
            log.error("Lua脚本执行失败");
            return false;
        }

        if (result == -1) {
            log.info("活动{}库存不足", activityId);
            return false;
        }

        if (result == -2) {
            log.info("用户{}已购买活动{}", memberId, activityId);
            return false;
        }

        // 4. 秒杀成功，异步创建订单（这里简化处理）
        log.info("用户{}秒杀活动{}成功", memberId, activityId);
        
        // TODO: 发送MQ消息，异步创建订单
        // mqProducer.send(new SeckillOrderMessage(activityId, memberId));

        return true;
    }

    @Override
    public void initSeckillStock(Long activityId, Integer stock) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        redisUtil.set(stockKey, stock, 24, TimeUnit.HOURS);
        log.info("初始化秒杀库存: activityId={}, stock={}", activityId, stock);
    }

    @Override
    public Integer getSeckillStock(Long activityId) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        Object stock = redisUtil.get(stockKey);
        return stock == null ? 0 : Integer.parseInt(stock.toString());
    }

    /**
     * 方案2：使用分布式锁（性能较差，不推荐）
     */
    public boolean seckillOrderWithLock(Long activityId, Long memberId) {
        String lockKey = LOCK_KEY_PREFIX + activityId + ":" + memberId;
        
        // 尝试获取锁
        boolean acquired = lockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS);
        if (!acquired) {
            log.warn("用户{}获取锁失败", memberId);
            return false;
        }

        try {
            // 检查是否已购买
            String userBuyKey = USER_BUY_KEY_PREFIX + activityId;
            // 这里需要实现检查逻辑

            // 检查库存
            String stockKey = STOCK_KEY_PREFIX + activityId;
            Long stock = redisUtil.decrement(stockKey);
            
            if (stock == null || stock < 0) {
                // 库存不足，回滚
                redisUtil.increment(stockKey);
                return false;
            }

            // 记录用户购买
            // TODO: 保存到数据库

            return true;
        } finally {
            lockUtil.unlock(lockKey);
        }
    }

    /**
     * 方案3：使用Redis的WATCH命令（乐观锁）
     * 注意：需要使用RedisTemplate的execute方法
     */
    public boolean seckillOrderWithWatch(Long activityId, Long memberId) {
        // 这里省略实现，面试时可以口述原理
        // 1. WATCH stockKey
        // 2. GET stockKey
        // 3. 判断库存
        // 4. MULTI
        // 5. DECR stockKey
        // 6. EXEC
        return false;
    }
}
