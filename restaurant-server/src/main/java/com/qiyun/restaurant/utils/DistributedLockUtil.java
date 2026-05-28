package com.qiyun.restaurant.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 
 * 面试要点：
 * 1. 使用Redisson实现分布式锁
 * 2. 支持自动续期（看门狗机制）
 * 3. 支持可重入
 * 4. 支持公平锁和非公平锁
 */
@Component
@Slf4j
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取锁
     * 
     * @param lockKey 锁的key
     * @return 锁对象
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁（立即返回）
     * 
     * @param lockKey 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock();
        } catch (Exception e) {
            log.error("获取锁失败: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 尝试获取锁（等待指定时间）
     * 
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            log.error("获取锁被中断: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放锁
     * 
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 执行带锁的业务逻辑
     * 
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param unit 时间单位
     * @param task 业务逻辑
     * @return 是否执行成功
     */
    public boolean executeWithLock(String lockKey, long waitTime, long leaseTime, 
                                   TimeUnit unit, Runnable task) {
        RLock lock = getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                try {
                    task.run();
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            return false;
        } catch (InterruptedException e) {
            log.error("获取锁被中断: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
