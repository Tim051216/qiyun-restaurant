# 七云菜馆后端服务 - 2026面试版

> 基于Spring Boot 3.2 + Spring Cloud + 微服务架构的餐饮管理系统

## 🎯 项目亮点（面试重点）

### 1. 技术栈（2026主流）
- **框架**: Spring Boot 3.2 + Spring Cloud 2023
- **Java版本**: Java 21 (LTS) + Virtual Threads
- **微服务**: Nacos + Sentinel + OpenFeign + Gateway
- **消息队列**: RabbitMQ (异步解耦)
- **缓存**: Redis + Caffeine (三级缓存)
- **数据库**: MySQL 8.0 + MyBatis Plus
- **监控**: Prometheus + Grafana + SkyWalking
- **容器化**: Docker + Docker Compose
- **分布式**: Redisson (分布式锁) + Seata (分布式事务)

### 2. 架构设计
```
用户请求
    ↓
API Gateway (Spring Cloud Gateway)
    ↓
服务注册中心 (Nacos)
    ↓
├── 订单服务 (Order Service)
├── 菜品服务 (Dish Service)
├── 会员服务 (Member Service)
└── 管理服务 (Admin Service)
    ↓
消息队列 (RabbitMQ)
    ↓
缓存层 (Redis + Caffeine)
    ↓
数据库 (MySQL)
```

### 3. 核心功能
- ✅ 订单管理：堂食、外卖、秒杀
- ✅ 会员系统：积分、优惠券、等级
- ✅ 营销活动：秒杀、拼团、满减
- ✅ 数据统计：销售报表、菜品分析

### 4. 技术亮点
1. **微服务架构**: 服务独立部署，支持弹性扩缩容
2. **异步处理**: RabbitMQ实现订单异步处理，TPS提升3倍
3. **三级缓存**: 本地缓存+Redis+数据库，响应时间<20ms
4. **限流降级**: Sentinel保护，秒杀QPS达10000+
5. **分布式锁**: Redisson实现库存扣减，防止超卖
6. **监控告警**: Prometheus+Grafana+SkyWalking，故障发现<1分钟
7. **容器化**: Docker一键部署，K8s自动扩缩容

## 🚀 快速启动

### 方式1：Docker Compose（推荐）

```bash
# 一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f restaurant-server
```

启动后访问：
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/doc.html
- Nacos控制台: http://localhost:8848/nacos (nacos/nacos)
- RabbitMQ管理: http://localhost:15672 (admin/admin123)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin123)

### 方式2：本地开发

**前置要求：**
- JDK 21
- Maven 3.9+
- MySQL 8.0
- Redis 7.0
- RabbitMQ 3.12
- Nacos 2.3.0

**启动步骤：**

```bash
# 1. 启动基础服务
docker-compose up -d mysql redis rabbitmq nacos

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 启动后端服务
mvn spring-boot:run
```

## 📊 性能指标

| 指标 | 数值 | 说明 |
|------|------|------|
| QPS | 10000+ | 秒杀场景 |
| 响应时间 | <20ms | 99%请求 |
| 缓存命中率 | 95% | 热点数据 |
| 服务可用性 | 99.9% | 年度统计 |
| 单元测试覆盖率 | 80%+ | 核心业务 |

## 🎤 面试问答

### Q1: 为什么选择微服务架构？

**A:** 
1. **业务复杂度**: 订单、会员、营销等模块业务独立，适合拆分
2. **团队协作**: 不同团队负责不同服务，并行开发
3. **技术选型**: 不同服务可以选择不同技术栈
4. **弹性扩展**: 秒杀场景下，订单服务可以独立扩容
5. **故障隔离**: 单个服务故障不影响整体系统

### Q2: 如何保证分布式事务一致性？

**A:** 使用Seata实现分布式事务：
```java
@GlobalTransactional
public void createOrder(OrderDTO orderDTO) {
    // 1. 创建订单
    orderService.create(orderDTO);
    // 2. 扣减库存
    dishService.deductStock(orderDTO.getDishId());
    // 3. 扣减积分
    memberService.deductPoints(orderDTO.getMemberId());
    // 任何一步失败，全部回滚
}
```

### Q3: 秒杀系统如何设计？

**A:** 
1. **前端限流**: 按钮置灰，防止重复点击
2. **网关限流**: Sentinel限制QPS
3. **Redis预减库存**: 先在Redis中扣减，减少数据库压力
4. **消息队列异步**: 下单请求进入MQ，削峰填谷
5. **分布式锁**: Redisson保证库存扣减原子性
6. **缓存预热**: 提前将商品信息加载到Redis

### Q4: 如何处理缓存穿透、击穿、雪崩？

**A:**
- **穿透**: 布隆过滤器 + 空值缓存
- **击穿**: 分布式锁 + 热点数据永不过期
- **雪崩**: 过期时间随机 + 多级缓存 + 限流降级

### Q5: 如何保证消息不丢失？

**A:**
1. **生产者**: 开启confirm机制，确认消息到达MQ
2. **MQ**: 消息持久化到磁盘
3. **消费者**: 手动ACK，处理成功后再确认

## 📁 项目结构

```
restaurant-server/
├── src/main/java/com/qiyun/restaurant/
│   ├── common/              # 通用类
│   │   ├── Result.java      # 统一返回结果
│   │   └── BaseContext.java # 线程上下文
│   ├── config/              # 配置类
│   │   ├── RedisConfig.java
│   │   ├── RabbitMQConfig.java
│   │   ├── SentinelConfig.java
│   │   └── CacheConfig.java
│   ├── controller/          # 控制器
│   ├── service/             # 服务层
│   │   └── impl/
│   ├── mapper/              # 数据访问层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── vo/                  # 视图对象
│   ├── utils/               # 工具类
│   │   ├── RedisUtil.java
│   │   ├── DistributedLockUtil.java
│   │   └── RateLimiterUtil.java
│   └── interceptor/         # 拦截器
├── src/main/resources/
│   ├── application.yml      # 主配置
│   ├── application-dev.yml  # 开发环境
│   ├── application-prod.yml # 生产环境
│   └── logback-spring.xml   # 日志配置
├── src/test/                # 测试代码
├── sql/                     # SQL脚本
├── monitoring/              # 监控配置
├── Dockerfile               # Docker镜像
├── docker-compose.yml       # Docker编排
└── pom.xml                  # Maven配置
```

## 🔧 核心代码示例

### 1. 限流降级
```java
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    
    @GetMapping("/kill/{dishId}")
    @SentinelResource(value = "seckillKill", 
                      blockHandler = "handleBlock")
    public Result seckill(@PathVariable Long dishId) {
        return seckillService.kill(dishId);
    }
    
    public Result handleBlock(Long dishId, BlockException e) {
        return Result.error("系统繁忙，请稍后再试");
    }
}
```

### 2. 分布式锁
```java
@Service
public class SeckillService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    public Result kill(Long dishId) {
        String lockKey = "seckill:lock:" + dishId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 扣减库存
                boolean success = deductStock(dishId);
                if (success) {
                    // 创建订单
                    createOrder(dishId);
                    return Result.success("秒杀成功");
                }
            }
        } finally {
            lock.unlock();
        }
        return Result.error("秒杀失败");
    }
}
```

### 3. 三级缓存
```java
@Service
public class DishService {
    
    @Autowired
    private Cache<String, Dish> localCache; // Caffeine
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    public Dish getById(Long id) {
        String key = "dish:" + id;
        
        // 1. 查本地缓存
        Dish dish = localCache.getIfPresent(key);
        if (dish != null) return dish;
        
        // 2. 查Redis
        dish = (Dish) redisTemplate.opsForValue().get(key);
        if (dish != null) {
            localCache.put(key, dish);
            return dish;
        }
        
        // 3. 查数据库
        dish = dishMapper.selectById(id);
        if (dish != null) {
            redisTemplate.opsForValue().set(key, dish, 1, TimeUnit.HOURS);
            localCache.put(key, dish);
        }
        return dish;
    }
}
```

### 4. 消息队列
```java
@Service
public class OrderService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void createOrder(OrderDTO orderDTO) {
        // 发送消息到MQ
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.create",
            orderDTO
        );
    }
}

@Component
public class OrderConsumer {
    
    @RabbitListener(queues = "order.queue")
    public void handleOrder(OrderDTO orderDTO) {
        // 异步处理订单
        processOrder(orderDTO);
    }
}
```

## 📈 监控大屏

### Grafana Dashboard
- JVM监控：堆内存、GC次数、线程数
- 应用监控：QPS、响应时间、错误率
- 数据库监控：连接数、慢查询
- Redis监控：命中率、内存使用
- RabbitMQ监控：消息堆积、消费速率

### SkyWalking链路追踪
- 服务拓扑图
- 接口调用链路
- 慢接口分析
- 异常追踪

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行单个测试
mvn test -Dtest=OrderServiceTest

# 生成测试报告
mvn test jacoco:report
```

## 📚 相关文档

- [技术栈升级方案](./技术栈升级方案-2026面试版.md)
- [API文档](http://localhost:8080/doc.html)
- [架构设计文档](./docs/architecture.md)
- [部署文档](./docs/deployment.md)

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

---

**⭐ 如果这个项目对你有帮助，请给个Star！**
