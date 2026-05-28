# Redis高并发解决方案

## 概述

本项目实现了面试中常问的Redis高并发问题解决方案，包括：

1. **超卖问题** - 使用Lua脚本保证原子性
2. **缓存穿透** - 布隆过滤器 + 空值缓存
3. **缓存击穿** - 分布式锁（互斥锁）
4. **缓存雪崩** - 过期时间随机化
5. **分布式锁** - Redisson实现
6. **限流** - 令牌桶算法 + 滑动窗口

---

## 1. 超卖问题解决方案

### 问题描述

在高并发场景下，多个用户同时购买商品，可能导致库存扣减出现问题，卖出的商品数量超过实际库存。

### 解决方案

#### 方案1：Redis原子操作 + Lua脚本（推荐）

**核心代码**：`SeckillServiceImpl.java`

```java
// Lua脚本保证原子性
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
```

**优点**：
- Lua脚本在Redis中原子执行，不会被打断
- 性能高，无需加锁
- 同时解决了超卖和重复购买问题

**面试要点**：
1. 为什么使用Lua脚本？
   - 保证多个Redis命令的原子性
   - 减少网络往返次数
   - 避免并发问题

2. 为什么不用数据库锁？
   - 数据库锁性能差
   - 分布式环境下数据库锁可能失效
   - Redis性能更高

#### 方案2：分布式锁（性能较差）

```java
public boolean seckillOrderWithLock(Long activityId, Long memberId) {
    String lockKey = LOCK_KEY_PREFIX + activityId + ":" + memberId;
    
    boolean acquired = lockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS);
    if (!acquired) {
        return false;
    }

    try {
        // 扣减库存
        Long stock = redisUtil.decrement(stockKey);
        if (stock < 0) {
            redisUtil.increment(stockKey);
            return false;
        }
        return true;
    } finally {
        lockUtil.unlock(lockKey);
    }
}
```

**缺点**：
- 性能较差，每次都要获取锁
- 锁的粒度较大

#### 方案3：数据库乐观锁

```sql
UPDATE product 
SET stock = stock - 1 
WHERE id = #{id} AND stock > 0
```

**缺点**：
- 数据库压力大
- 性能不如Redis

---

## 2. 缓存穿透解决方案

### 问题描述

大量请求查询不存在的数据，导致请求直接打到数据库，可能压垮数据库。

### 解决方案

**核心代码**：`CacheUtil.java`

```java
public <T> T getWithPassThrough(String key, Class<T> type, Supplier<T> dbFallback) {
    // 1. 布隆过滤器判断
    if (!mightContain(key)) {
        return null;
    }

    // 2. 查询Redis缓存
    Object cacheData = redisUtil.get(key);
    if (cacheData != null) {
        if ("".equals(cacheData)) {
            return null;  // 空值缓存
        }
        return type.cast(cacheData);
    }

    // 3. 查询数据库
    T data = dbFallback.get();

    // 4. 数据不存在，缓存空值
    if (data == null) {
        redisUtil.set(key, "", NULL_CACHE_EXPIRE, TimeUnit.SECONDS);
        return null;
    }

    // 5. 数据存在，写入缓存
    redisUtil.set(key, data, timeout, unit);
    return data;
}
```

**两种方案**：

#### 方案1：布隆过滤器

**优点**：
- 内存占用小
- 查询速度快

**缺点**：
- 存在误判（可能判断存在但实际不存在）
- 无法删除元素

**使用场景**：
- 数据量大
- 允许少量误判

#### 方案2：空值缓存

**优点**：
- 实现简单
- 无误判

**缺点**：
- 占用Redis内存
- 需要设置合理的过期时间

**使用场景**：
- 数据量不大
- 不允许误判

**面试要点**：
1. 布隆过滤器原理？
   - 使用多个hash函数
   - 将元素映射到位数组
   - 判断时检查所有位是否都为1

2. 为什么会误判？
   - 不同元素可能映射到相同的位
   - 只会误判存在，不会误判不存在

---

## 3. 缓存击穿解决方案

### 问题描述

热点key过期瞬间，大量请求同时查询数据库，导致数据库压力骤增。

### 解决方案

**核心代码**：`CacheUtil.java`

```java
// 使用分布式锁防止缓存击穿
String lockKey = "lock:" + key;
boolean acquired = lockUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);

if (!acquired) {
    // 获取锁失败，等待后重试
    Thread.sleep(50);
    return getWithPassThrough(key, type, dbFallback, timeout, unit);
}

try {
    // 双重检查
    cacheData = redisUtil.get(key);
    if (cacheData != null) {
        return type.cast(cacheData);
    }

    // 查询数据库
    T data = dbFallback.get();
    
    // 写入缓存
    redisUtil.set(key, data, timeout, unit);
    return data;
} finally {
    lockUtil.unlock(lockKey);
}
```

**面试要点**：
1. 为什么要双重检查？
   - 第一次检查：避免不必要的加锁
   - 第二次检查：防止重复查询数据库

2. 为什么不用本地锁？
   - 分布式环境下本地锁无效
   - 需要使用分布式锁

3. 其他方案？
   - 热点数据永不过期
   - 定时任务刷新缓存

---

## 4. 缓存雪崩解决方案

### 问题描述

大量缓存同时过期，导致请求全部打到数据库。

### 解决方案

**核心代码**：`CacheUtil.java`

```java
// 添加随机过期时间，防止缓存雪崩
long randomExpire = timeout + new Random().nextInt((int) RANDOM_EXPIRE_RANGE);
redisUtil.set(key, data, randomExpire, unit);
```

**多种方案**：

#### 方案1：过期时间随机化

```java
// 基础过期时间 + 随机时间（0-300秒）
long randomExpire = 3600 + new Random().nextInt(300);
```

#### 方案2：Redis集群

- 主从复制
- 哨兵模式
- Redis Cluster

#### 方案3：限流降级

- 使用Hystrix等熔断器
- 限制数据库访问频率

**面试要点**：
1. 缓存雪崩和缓存击穿的区别？
   - 雪崩：大量key同时过期
   - 击穿：单个热点key过期

2. 如何预防？
   - 过期时间随机化
   - 缓存预热
   - Redis高可用

---

## 5. 分布式锁实现

### 核心代码

**Redisson实现**：`DistributedLockUtil.java`

```java
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
        Thread.currentThread().interrupt();
        return false;
    }
}
```

**Redisson优势**：
1. 自动续期（看门狗机制）
2. 可重入
3. 支持公平锁和非公平锁
4. 支持读写锁

**面试要点**：
1. Redis分布式锁的实现原理？
   - SETNX + EXPIRE
   - Lua脚本保证原子性
   - 使用UUID防止误删

2. Redisson的看门狗机制？
   - 自动续期，防止业务未完成锁就过期
   - 默认30秒续期一次

3. 如何防止死锁？
   - 设置过期时间
   - 使用try-finally确保释放锁

---

## 6. 限流实现

### 核心代码

**令牌桶算法**：`RateLimiterUtil.java`

```java
// Lua脚本实现令牌桶
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
    "    return 1\n" +
    "else\n" +
    "    return 0\n" +
    "end";
```

**三种限流算法**：

#### 1. 固定窗口

```java
public boolean simpleRateLimit(String key, int maxCount, int window) {
    Long count = redisTemplate.opsForValue().increment(limitKey);
    if (count == 1) {
        redisTemplate.expire(limitKey, window, TimeUnit.SECONDS);
    }
    return count <= maxCount;
}
```

**优点**：实现简单  
**缺点**：临界问题（窗口边界流量突增）

#### 2. 滑动窗口

```java
public boolean slidingWindowRateLimit(String key, int maxCount, int window) {
    long now = System.currentTimeMillis();
    long windowStart = now - window * 1000L;
    
    // 移除窗口外的数据
    redisTemplate.opsForZSet().removeRangeByScore(limitKey, 0, windowStart);
    
    // 获取当前窗口内的请求数
    Long count = redisTemplate.opsForZSet().zCard(limitKey);
    
    if (count >= maxCount) {
        return false;
    }
    
    // 添加当前请求
    redisTemplate.opsForZSet().add(limitKey, String.valueOf(now), now);
    return true;
}
```

**优点**：解决临界问题  
**缺点**：内存占用较大

#### 3. 令牌桶

**优点**：
- 支持突发流量
- 平滑限流

**缺点**：
- 实现复杂

**面试要点**：
1. 令牌桶和漏桶的区别？
   - 令牌桶：允许突发流量
   - 漏桶：严格限制流量

2. 如何选择限流算法？
   - 固定窗口：简单场景
   - 滑动窗口：精确限流
   - 令牌桶：需要支持突发流量

---

## 测试接口

### 1. 初始化秒杀库存

```bash
POST http://localhost:8080/seckill/init?activityId=1&stock=100
```

### 2. 秒杀下单

```bash
POST http://localhost:8080/seckill/order?activityId=1&memberId=1
```

### 3. 查询库存

```bash
GET http://localhost:8080/seckill/stock?activityId=1
```

### 4. 压力测试

使用JMeter或Apache Bench进行压力测试：

```bash
# 1000个并发请求
ab -n 1000 -c 100 http://localhost:8080/seckill/order?activityId=1&memberId=1
```

---

## 面试常见问题

### 1. Redis如何解决超卖问题？

**答案**：
1. 使用Lua脚本保证原子性
2. 使用Redis的DECR命令（原子操作）
3. 使用分布式锁（性能较差）

### 2. 缓存穿透、击穿、雪崩的区别？

**答案**：
- **穿透**：查询不存在的数据，请求打到数据库
- **击穿**：热点key过期，大量请求打到数据库
- **雪崩**：大量key同时过期，请求打到数据库

### 3. Redis分布式锁如何实现？

**答案**：
1. 使用SETNX + EXPIRE
2. 使用Lua脚本保证原子性
3. 使用UUID防止误删
4. 使用Redisson实现（推荐）

### 4. 如何保证Redis和数据库的一致性？

**答案**：
1. 先更新数据库，再删除缓存
2. 使用延迟双删
3. 使用Canal监听binlog
4. 使用分布式事务（Seata）

### 5. Redis持久化方式有哪些？

**答案**：
1. **RDB**：快照持久化
2. **AOF**：追加文件持久化
3. **混合持久化**：RDB + AOF

---

## 项目结构

```
restaurant-server/
├── config/
│   ├── RedisConfig.java          # Redis配置
│   └── RedissonConfig.java       # Redisson配置
├── utils/
│   ├── RedisUtil.java            # Redis工具类
│   ├── DistributedLockUtil.java  # 分布式锁工具
│   ├── CacheUtil.java            # 缓存工具（解决穿透/击穿/雪崩）
│   └── RateLimiterUtil.java      # 限流工具
├── service/
│   ├── SeckillService.java       # 秒杀Service接口
│   └── impl/
│       └── SeckillServiceImpl.java  # 秒杀Service实现（解决超卖）
└── controller/
    └── SeckillController.java    # 秒杀Controller
```

---

## 总结

本项目实现了面试中最常问的Redis高并发问题解决方案，代码可以直接运行和测试。

**核心要点**：
1. ✅ 超卖问题 - Lua脚本原子操作
2. ✅ 缓存穿透 - 布隆过滤器 + 空值缓存
3. ✅ 缓存击穿 - 分布式锁（互斥锁）
4. ✅ 缓存雪崩 - 过期时间随机化
5. ✅ 分布式锁 - Redisson实现
6. ✅ 限流 - 令牌桶 + 滑动窗口

**面试建议**：
1. 理解每个问题的本质
2. 掌握多种解决方案
3. 能够说出优缺点
4. 结合实际项目经验
