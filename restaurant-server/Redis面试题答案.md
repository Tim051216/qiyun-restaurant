# Redis面试题答案汇总

## 目录

1. [基础问题](#基础问题)
2. [高并发问题](#高并发问题)
3. [数据结构](#数据结构)
4. [持久化](#持久化)
5. [集群](#集群)
6. [实战场景](#实战场景)

---

## 基础问题

### 1. Redis为什么这么快？

**答案**：
1. **纯内存操作**：数据存储在内存中，读写速度快
2. **单线程模型**：避免了线程切换和锁竞争的开销
3. **IO多路复用**：使用epoll/select等机制，高效处理并发连接
4. **高效的数据结构**：针对不同场景优化的数据结构
5. **简单的协议**：RESP协议简单高效

**代码示例**：
```java
// 本项目中的Redis配置
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 配置序列化方式，提升性能
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // ... 序列化配置
        return template;
    }
}
```

### 2. Redis单线程为什么还能支持高并发？

**答案**：
1. **CPU不是瓶颈**：Redis的瓶颈是内存和网络带宽
2. **避免上下文切换**：单线程避免了线程切换的开销
3. **IO多路复用**：一个线程可以处理多个连接
4. **非阻塞IO**：不会因为某个操作阻塞整个线程

**注意**：Redis 6.0引入了多线程，但只用于网络IO，核心命令执行仍是单线程。

---

## 高并发问题

### 3. 如何解决缓存穿透？

**答案**：

#### 方案1：布隆过滤器

**原理**：
- 使用多个hash函数将元素映射到位数组
- 查询时检查所有位是否都为1
- 如果有任何一位为0，则元素一定不存在

**代码实现**：
```java
@Component
public class CacheUtil {
    private BloomFilter<String> bloomFilter;
    
    @PostConstruct
    public void init() {
        // 预计100万个元素，误判率0.01
        bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(StandardCharsets.UTF_8),
            1000000,
            0.01
        );
    }
    
    public boolean mightContain(String key) {
        return bloomFilter.mightContain(key);
    }
}
```

**优点**：
- 内存占用小
- 查询速度快O(k)，k为hash函数个数

**缺点**：
- 存在误判（可能判断存在但实际不存在）
- 无法删除元素

#### 方案2：空值缓存

**代码实现**：
```java
public <T> T getWithPassThrough(String key, Supplier<T> dbFallback) {
    // 查询数据库
    T data = dbFallback.get();
    
    // 数据不存在，缓存空值
    if (data == null) {
        redisUtil.set(key, "", 60, TimeUnit.SECONDS);
        return null;
    }
    
    return data;
}
```

**优点**：
- 实现简单
- 无误判

**缺点**：
- 占用Redis内存
- 可能被恶意攻击（大量不存在的key）

### 4. 如何解决缓存击穿？

**答案**：

#### 方案1：互斥锁（推荐）

**代码实现**：
```java
public <T> T getWithMutex(String key, Supplier<T> dbFallback) {
    // 1. 查询缓存
    Object data = redisUtil.get(key);
    if (data != null) {
        return (T) data;
    }
    
    // 2. 获取分布式锁
    String lockKey = "lock:" + key;
    boolean acquired = lockUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
    
    if (!acquired) {
        // 获取锁失败，等待后重试
        Thread.sleep(50);
        return getWithMutex(key, dbFallback);
    }
    
    try {
        // 3. 双重检查
        data = redisUtil.get(key);
        if (data != null) {
            return (T) data;
        }
        
        // 4. 查询数据库
        T result = dbFallback.get();
        
        // 5. 写入缓存
        redisUtil.set(key, result, 3600, TimeUnit.SECONDS);
        
        return result;
    } finally {
        lockUtil.unlock(lockKey);
    }
}
```

**优点**：
- 保证只有一个线程查询数据库
- 性能较好

**缺点**：
- 需要等待锁释放

#### 方案2：热点数据永不过期

**代码实现**：
```java
// 逻辑过期时间
public void setWithLogicalExpire(String key, Object value, long expire) {
    Map<String, Object> data = new HashMap<>();
    data.put("value", value);
    data.put("expireTime", System.currentTimeMillis() + expire * 1000);
    
    // 永不过期
    redisUtil.set(key, data);
}
```

**优点**：
- 不会出现缓存击穿
- 性能最好

**缺点**：
- 需要额外的线程更新缓存
- 可能返回过期数据

### 5. 如何解决缓存雪崩？

**答案**：

#### 方案1：过期时间随机化

**代码实现**：
```java
// 添加随机过期时间
long randomExpire = 3600 + new Random().nextInt(300);
redisUtil.set(key, data, randomExpire, TimeUnit.SECONDS);
```

#### 方案2：Redis集群

- 主从复制
- 哨兵模式
- Redis Cluster

#### 方案3：限流降级

```java
// 使用限流器
if (!rateLimiter.tryAcquire()) {
    return "系统繁忙，请稍后重试";
}
```

### 6. 如何解决超卖问题？

**答案**：

#### 方案1：Lua脚本（推荐）

**代码实现**：
```java
private static final String DEDUCT_STOCK_SCRIPT =
    "local stock = redis.call('get', KEYS[1])\n" +
    "if not stock or tonumber(stock) <= 0 then\n" +
    "    return -1\n" +
    "end\n" +
    "redis.call('decrby', KEYS[1], ARGV[1])\n" +
    "return 1";

public boolean deductStock(Long productId, Integer quantity) {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>(
        DEDUCT_STOCK_SCRIPT, 
        Long.class
    );
    
    Long result = redisTemplate.execute(
        script,
        Collections.singletonList("stock:" + productId),
        quantity.toString()
    );
    
    return result != null && result == 1;
}
```

**优点**：
- Lua脚本原子执行
- 性能高
- 不需要加锁

**为什么Lua脚本能保证原子性？**
- Redis单线程执行Lua脚本
- 脚本执行期间不会执行其他命令
- 避免了并发问题

#### 方案2：Redis DECR命令

```java
public boolean deductStock(Long productId) {
    Long stock = redisUtil.decrement("stock:" + productId);
    
    if (stock < 0) {
        // 库存不足，回滚
        redisUtil.increment("stock:" + productId);
        return false;
    }
    
    return true;
}
```

**优点**：
- 简单
- DECR是原子操作

**缺点**：
- 需要回滚操作
- 无法同时检查其他条件（如用户是否已购买）

#### 方案3：数据库乐观锁

```sql
UPDATE product 
SET stock = stock - 1 
WHERE id = #{id} AND stock > 0
```

**优点**：
- 实现简单
- 不需要Redis

**缺点**：
- 数据库压力大
- 性能不如Redis

---

## 数据结构

### 7. Redis有哪些数据结构？

**答案**：

#### 基础数据结构

1. **String（字符串）**
   - 最基本的类型
   - 可以存储字符串、整数、浮点数
   - 最大512MB

2. **Hash（哈希）**
   - 键值对集合
   - 适合存储对象

3. **List（列表）**
   - 双向链表
   - 适合消息队列、时间线

4. **Set（集合）**
   - 无序不重复集合
   - 适合标签、好友关系

5. **ZSet（有序集合）**
   - 有序不重复集合
   - 适合排行榜、延时队列

#### 高级数据结构

6. **Bitmap（位图）**
   - 用于统计
   - 节省内存

7. **HyperLogLog**
   - 基数统计
   - 误差0.81%

8. **Geo**
   - 地理位置
   - 附近的人

9. **Stream**
   - 消息队列
   - Redis 5.0新增

### 8. 什么时候用Hash，什么时候用String？

**答案**：

#### 使用String

```java
// 存储简单值
redisUtil.set("user:1:name", "张三");
redisUtil.set("user:1:age", "25");
```

**适用场景**：
- 简单的键值对
- 需要设置过期时间
- 需要原子操作（INCR/DECR）

#### 使用Hash

```java
// 存储对象
Map<String, Object> user = new HashMap<>();
user.put("name", "张三");
user.put("age", 25);
redisTemplate.opsForHash().putAll("user:1", user);
```

**适用场景**：
- 存储对象
- 需要修改对象的某个字段
- 字段较多时节省内存

**内存对比**：
- String：每个字段一个key，内存开销大
- Hash：一个key存储多个字段，内存开销小

---

## 持久化

### 9. Redis持久化方式有哪些？

**答案**：

#### RDB（Redis Database）

**原理**：
- 快照持久化
- 将某个时间点的数据保存到磁盘

**触发方式**：
1. 手动触发：SAVE、BGSAVE
2. 自动触发：配置save规则

**配置**：
```conf
# 900秒内至少1个key被修改
save 900 1
# 300秒内至少10个key被修改
save 300 10
# 60秒内至少10000个key被修改
save 60 10000
```

**优点**：
- 文件紧凑，适合备份
- 恢复速度快
- 对性能影响小

**缺点**：
- 可能丢失最后一次快照后的数据
- fork子进程时可能阻塞

#### AOF（Append Only File）

**原理**：
- 追加文件持久化
- 记录每个写操作

**同步策略**：
1. always：每个命令都同步
2. everysec：每秒同步一次（推荐）
3. no：由操作系统决定

**配置**：
```conf
appendonly yes
appendfsync everysec
```

**优点**：
- 数据更安全，最多丢失1秒数据
- 文件可读，可以手动修复

**缺点**：
- 文件体积大
- 恢复速度慢
- 对性能影响大

#### 混合持久化（推荐）

**原理**：
- RDB + AOF
- AOF重写时使用RDB格式

**配置**：
```conf
aof-use-rdb-preamble yes
```

**优点**：
- 结合了RDB和AOF的优点
- 恢复速度快
- 数据更安全

### 10. RDB和AOF如何选择？

**答案**：

| 场景 | 推荐方案 | 原因 |
|------|---------|------|
| 数据不能丢失 | AOF | 最多丢失1秒数据 |
| 可以接受分钟级数据丢失 | RDB | 性能更好 |
| 生产环境 | 混合持久化 | 兼顾性能和安全 |
| 缓存场景 | 不持久化 | 数据可以重建 |

---

## 集群

### 11. Redis集群方案有哪些？

**答案**：

#### 方案1：主从复制

**特点**：
- 一主多从
- 读写分离
- 主节点故障需要手动切换

**配置**：
```conf
# 从节点配置
replicaof 192.168.1.100 6379
```

#### 方案2：哨兵模式（Sentinel）

**特点**：
- 自动故障转移
- 监控主从节点
- 通知客户端主节点变化

**配置**：
```conf
sentinel monitor mymaster 192.168.1.100 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 60000
```

#### 方案3：Redis Cluster

**特点**：
- 分布式存储
- 数据分片（16384个槽）
- 自动故障转移

**本项目配置**：
```java
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }
}
```

### 12. Redis Cluster如何分片？

**答案**：

**哈希槽（Hash Slot）**：
- 总共16384个槽
- 每个key通过CRC16算法计算槽位
- 每个节点负责一部分槽

**计算公式**：
```
slot = CRC16(key) % 16384
```

**优点**：
- 数据分布均匀
- 扩容方便（迁移槽）

**缺点**：
- 不支持多key操作（除非在同一个槽）

---

## 实战场景

### 13. 如何实现分布式锁？

**答案**：

#### 方案1：SETNX + EXPIRE

```java
// 错误示例（不是原子操作）
redisUtil.setIfAbsent("lock:key", "value");
redisUtil.expire("lock:key", 30, TimeUnit.SECONDS);

// 正确示例（原子操作）
redisUtil.setIfAbsent("lock:key", "value", 30, TimeUnit.SECONDS);
```

#### 方案2：Lua脚本

```lua
-- 加锁
if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then
    redis.call('expire', KEYS[1], ARGV[2])
    return 1
else
    return 0
end

-- 解锁（防止误删）
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end
```

#### 方案3：Redisson（推荐）

**本项目实现**：
```java
@Component
public class DistributedLockUtil {
    @Autowired
    private RedissonClient redissonClient;
    
    public boolean executeWithLock(String lockKey, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);
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
}
```

**Redisson优势**：
1. **看门狗机制**：自动续期
2. **可重入**：同一线程可以多次获取锁
3. **公平锁**：支持公平和非公平锁
4. **读写锁**：支持读写分离

### 14. 如何实现限流？

**答案**：

#### 方案1：固定窗口

**本项目实现**：
```java
public boolean simpleRateLimit(String key, int maxCount, int window) {
    Long count = redisTemplate.opsForValue().increment(key);
    
    if (count == 1) {
        redisTemplate.expire(key, window, TimeUnit.SECONDS);
    }
    
    return count <= maxCount;
}
```

**缺点**：临界问题

#### 方案2：滑动窗口

**本项目实现**：
```java
public boolean slidingWindowRateLimit(String key, int maxCount, int window) {
    long now = System.currentTimeMillis();
    long windowStart = now - window * 1000L;
    
    // 移除窗口外的数据
    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
    
    // 获取当前窗口内的请求数
    Long count = redisTemplate.opsForZSet().zCard(key);
    
    if (count >= maxCount) {
        return false;
    }
    
    // 添加当前请求
    redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
    return true;
}
```

#### 方案3：令牌桶

**本项目实现**：
```java
// 使用Lua脚本实现令牌桶算法
private static final String LUA_SCRIPT = 
    "local tokens = tonumber(redis.call('get', KEYS[1]) or capacity)\n" +
    "local now = tonumber(ARGV[1])\n" +
    "local last_time = tonumber(redis.call('get', KEYS[2]) or now)\n" +
    "local delta = math.max(0, now - last_time)\n" +
    "local new_tokens = math.min(capacity, tokens + delta * rate)\n" +
    "if new_tokens >= requested then\n" +
    "    redis.call('set', KEYS[1], new_tokens - requested)\n" +
    "    redis.call('set', KEYS[2], now)\n" +
    "    return 1\n" +
    "else\n" +
    "    return 0\n" +
    "end";
```

### 15. 如何保证Redis和数据库的一致性？

**答案**：

#### 方案1：先更新数据库，再删除缓存（推荐）

```java
public void updateUser(User user) {
    // 1. 更新数据库
    userMapper.updateById(user);
    
    // 2. 删除缓存
    redisUtil.delete("user:" + user.getId());
}
```

**为什么不是更新缓存？**
- 更新缓存可能浪费（缓存可能不会被读取）
- 删除缓存更简单

#### 方案2：延迟双删

```java
public void updateUser(User user) {
    // 1. 删除缓存
    redisUtil.delete("user:" + user.getId());
    
    // 2. 更新数据库
    userMapper.updateById(user);
    
    // 3. 延迟删除缓存
    new Thread(() -> {
        try {
            Thread.sleep(500);
            redisUtil.delete("user:" + user.getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}
```

#### 方案3：Canal监听binlog

```java
// 监听MySQL binlog
@Component
public class CanalListener {
    @Autowired
    private RedisUtil redisUtil;
    
    public void onEvent(CanalEntry.Entry entry) {
        if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
            // 解析binlog
            // 删除对应的缓存
            redisUtil.delete("user:" + userId);
        }
    }
}
```

---

## 总结

本文档涵盖了Redis面试中最常问的问题，并提供了本项目中的实际代码实现。

**核心要点**：
1. ✅ 理解Redis的基本原理
2. ✅ 掌握高并发问题的解决方案
3. ✅ 熟悉各种数据结构的使用场景
4. ✅ 了解持久化和集群方案
5. ✅ 能够解决实际业务问题

**面试建议**：
1. 结合项目经验回答
2. 说出多种解决方案
3. 分析优缺点
4. 展示代码实现能力
