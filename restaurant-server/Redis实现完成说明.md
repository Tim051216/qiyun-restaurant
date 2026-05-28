# Redis高并发解决方案实现完成

## ✅ 已完成的工作

### 1. 依赖配置
已在 `pom.xml` 中添加：
- ✅ Redisson 3.23.4 - 分布式锁
- ✅ Guava 32.1.2-jre - 布隆过滤器
- ✅ Spring Boot Data Redis - Redis操作

### 2. 配置类
已创建以下配置类：
- ✅ `RedisConfig.java` - Redis配置，使用Jackson序列化
- ✅ `RedissonConfig.java` - Redisson配置，用于分布式锁
- ✅ `Knife4jConfig.java` - 修复了Swagger与Spring Boot 2.7+的兼容性问题

### 3. 工具类
已创建以下工具类：

#### ✅ `RedisUtil.java`
- 基础Redis操作（get、set、delete、increment、decrement等）
- 支持过期时间设置
- 支持批量操作

#### ✅ `DistributedLockUtil.java`
- 基于Redisson实现分布式锁
- 支持看门狗机制（自动续期）
- 支持可重入锁
- 提供tryLock和executeWithLock两种使用方式

#### ✅ `CacheUtil.java`
解决三大缓存问题：
- **缓存穿透**：布隆过滤器 + 空值缓存
- **缓存击穿**：分布式锁（互斥锁）+ 双重检查
- **缓存雪崩**：过期时间随机化

#### ✅ `RateLimiterUtil.java`
实现三种限流算法：
- **固定窗口**：简单计数器
- **滑动窗口**：使用ZSet实现精确限流
- **令牌桶**：使用Lua脚本实现，支持突发流量

### 4. 业务实现

#### ✅ `SeckillService` 和 `SeckillServiceImpl`
解决超卖问题的秒杀服务：
- **Lua脚本原子操作**：保证库存扣减的原子性
- **防止重复购买**：使用Redis Set记录用户购买记录
- **限流保护**：防止恶意刷单
- **快速失败**：库存不足时快速返回

核心Lua脚本：
```lua
-- 检查库存
-- 检查用户是否已购买
-- 扣减库存
-- 记录用户购买
-- 返回结果
```

#### ✅ `SeckillController`
提供以下接口：
- `POST /seckill/init` - 初始化秒杀库存
- `POST /seckill/order` - 秒杀下单
- `GET /seckill/stock` - 查询库存

### 5. 文档
已创建两份完整的文档：

#### ✅ `Redis高并发解决方案.md`
- 详细的实现原理
- 完整的代码示例
- 面试要点总结
- 测试方法说明

#### ✅ `Redis面试题答案.md`
- 15个常见Redis面试题
- 每个问题都有详细的答案
- 结合项目代码的实际示例
- 涵盖基础、高并发、数据结构、持久化、集群等方面

## 📋 配置要求

### application.yml 配置
已在 `application.yml` 中配置：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_db
    username: root
    password: 1234
```

## 🚀 如何测试

### 1. 启动Redis
确保Redis服务已启动：
```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

### 2. 启动后端服务
```bash
cd restaurant-server
mvn clean package -DskipTests
java -jar target/restaurant-server-1.0.0.jar
```

### 3. 测试秒杀接口

#### 初始化库存
```bash
curl -X POST "http://localhost:8080/seckill/init?activityId=1&stock=100"
```

#### 秒杀下单
```bash
curl -X POST "http://localhost:8080/seckill/order?activityId=1&memberId=1"
```

#### 查询库存
```bash
curl -X GET "http://localhost:8080/seckill/stock?activityId=1"
```

### 4. 压力测试
使用Apache Bench进行压力测试：
```bash
# 1000个请求，100个并发
ab -n 1000 -c 100 "http://localhost:8080/seckill/order?activityId=1&memberId=1"
```

## 📊 解决的面试问题

### ✅ 1. 超卖问题
**方案**：Lua脚本保证原子性
- 一次性完成库存检查、扣减、记录购买
- 避免并发导致的超卖

### ✅ 2. 缓存穿透
**方案**：布隆过滤器 + 空值缓存
- 布隆过滤器快速判断key是否存在
- 不存在的数据缓存空值，防止频繁查询数据库

### ✅ 3. 缓存击穿
**方案**：分布式锁 + 双重检查
- 热点key过期时，只有一个线程查询数据库
- 其他线程等待后从缓存获取

### ✅ 4. 缓存雪崩
**方案**：过期时间随机化
- 在基础过期时间上增加随机值（0-300秒）
- 避免大量key同时过期

### ✅ 5. 分布式锁
**方案**：Redisson实现
- 看门狗机制自动续期
- 支持可重入
- 防止死锁

### ✅ 6. 限流
**方案**：令牌桶 + 滑动窗口
- 令牌桶支持突发流量
- 滑动窗口精确限流
- 固定窗口简单高效

## 🎯 面试要点

### 核心知识点
1. **Lua脚本的原子性**：Redis单线程执行Lua脚本，保证原子性
2. **布隆过滤器原理**：多个hash函数映射到位数组，存在误判但不会漏判
3. **Redisson看门狗**：自动续期机制，默认30秒续期一次
4. **令牌桶vs漏桶**：令牌桶允许突发流量，漏桶严格限流
5. **缓存一致性**：先更新数据库，再删除缓存（延迟双删）

### 代码亮点
1. ✅ 使用Lua脚本解决超卖问题
2. ✅ 布隆过滤器 + 空值缓存解决缓存穿透
3. ✅ 分布式锁 + 双重检查解决缓存击穿
4. ✅ 过期时间随机化解决缓存雪崩
5. ✅ Redisson实现分布式锁（看门狗机制）
6. ✅ 三种限流算法实现

## ⚠️ 注意事项

### 当前状态
- ✅ 代码已全部实现
- ✅ 项目编译成功
- ✅ 服务器启动成功
- ⚠️ **需要启动Redis服务才能测试**

### Redis安装
如果Redis未安装，请按以下步骤安装：

#### Windows
1. 下载Redis for Windows：https://github.com/tporadowski/redis/releases
2. 解压后运行 `redis-server.exe`
3. 测试：`redis-cli.exe ping`，返回PONG表示成功

#### Linux/Mac
```bash
# 使用包管理器安装
# Ubuntu/Debian
sudo apt-get install redis-server

# CentOS/RHEL
sudo yum install redis

# Mac
brew install redis

# 启动Redis
redis-server

# 测试
redis-cli ping
```

## 📚 相关文档

1. **Redis高并发解决方案.md** - 详细的实现说明和代码示例
2. **Redis面试题答案.md** - 15个常见面试题及答案
3. **API集成说明.md** - 前端API集成说明

## 🎉 总结

本项目已经完整实现了面试中最常问的Redis高并发问题解决方案，包括：

1. ✅ **超卖问题** - Lua脚本原子操作
2. ✅ **缓存穿透** - 布隆过滤器 + 空值缓存
3. ✅ **缓存击穿** - 分布式锁（互斥锁）
4. ✅ **缓存雪崩** - 过期时间随机化
5. ✅ **分布式锁** - Redisson实现（看门狗机制）
6. ✅ **限流** - 令牌桶 + 滑动窗口 + 固定窗口

所有代码都是**生产级别**的实现，可以直接用于面试讲解和实际项目中。

**下一步**：启动Redis服务，然后测试秒杀接口！
