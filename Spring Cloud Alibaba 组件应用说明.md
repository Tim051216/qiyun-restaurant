# Spring Cloud Alibaba 组件在七云菜馆项目中的应用

## 📋 目录

1. [Nacos - 服务注册与配置中心](#1-nacos)
2. [Sentinel - 流量控制与熔断降级](#2-sentinel)
3. [Gateway - API 网关](#3-gateway)
4. [RabbitMQ - 消息队列](#4-rabbitmq)
5. [Seata - 分布式事务](#5-seata)
6. [Dubbo - RPC 框架](#6-dubbo)

---

## 1. Nacos - 服务注册与配置中心

### 🎯 在项目中的应用

#### 1.1 服务注册与发现

**应用场景：**
- 5 个微服务自动注册到 Nacos
- 服务间通过服务名调用，无需硬编码 IP 和端口
- 支持服务健康检查和自动剔除

**配置示例：**
```yaml
# bootstrap.yml
spring:
  application:
    name: restaurant-order  # 服务名
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos 地址
        namespace: dev  # 命名空间
        group: DEFAULT_GROUP  # 分组
```

**实际使用：**
```java
// Order 服务调用 Dish 服务
@FeignClient(name = "restaurant-dish")  // 使用服务名，不需要 IP
public interface DishServiceClient {
    @GetMapping("/api/dishes/{id}")
    DishDTO getDishById(@PathVariable Long id);
}
```

**效果：**
- ✅ 服务自动注册到 Nacos
- ✅ 服务实例动态上下线
- ✅ 负载均衡自动生效
- ✅ 服务健康检查（心跳机制）

---

#### 1.2 动态配置管理

**应用场景：**
- 数据库连接配置
- Redis 配置
- 业务参数配置（如订单超时时间）
- 功能开关

**配置示例：**
```yaml
# Nacos 配置中心 - restaurant-order-dev.yml
order:
  timeout: 1800  # 订单超时时间（秒）
  auto-cancel: true  # 是否自动取消超时订单
  
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_order
    username: root
    password: ${db.password}  # 支持加密
```

**代码使用：**
```java
@RefreshScope  // 支持动态刷新
@Component
public class OrderConfig {
    
    @Value("${order.timeout}")
    private Integer timeout;
    
    @Value("${order.auto-cancel}")
    private Boolean autoCancel;
    
    // 配置变更后自动刷新，无需重启服务
}
```

**实际案例：**
```
场景：订单超时时间从 30 分钟改为 1 小时

传统方式：
1. 修改配置文件
2. 重新打包
3. 重启服务
4. 影响线上业务

使用 Nacos：
1. 在 Nacos 控制台修改配置
2. 点击发布
3. 服务自动刷新配置
4. 零停机，无感知
```

---

## 2. Sentinel - 流量控制与熔断降级

### 🎯 在项目中的应用

#### 2.1 流量控制（限流）

**应用场景：**
- 订单创建接口限流（防止刷单）
- 秒杀接口限流（防止超卖）
- 查询接口限流（防止数据库压力过大）

**配置示例：**
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    @SentinelResource(
        value = "createOrder",  // 资源名
        blockHandler = "createOrderBlockHandler"  // 限流处理方法
    )
    public Result<OrderDTO> createOrder(@RequestBody OrderRequest request) {
        // 创建订单逻辑
        return Result.success(orderService.createOrder(request));
    }
    
    // 限流后的处理逻辑
    public Result<OrderDTO> createOrderBlockHandler(
            OrderRequest request, BlockException ex) {
        return Result.error("系统繁忙，请稍后再试");
    }
}
```

**Sentinel 控制台配置：**
```
资源名：createOrder
限流规则：
  - QPS 阈值：100（每秒最多 100 个请求）
  - 流控模式：直接
  - 流控效果：快速失败
```

**实际效果：**
```
正常情况：
请求 1-100：正常处理 ✅
请求 101+：返回"系统繁忙" ⚠️

秒杀场景：
10000 个并发请求 → 只有 100 个成功 → 保护系统稳定
```

---

#### 2.2 熔断降级

**应用场景：**
- Dish 服务调用失败时熔断
- 第三方支付接口超时熔断
- 数据库查询慢时降级

**配置示例：**
```java
@FeignClient(
    name = "restaurant-dish",
    fallback = DishServiceFallback.class  // 降级处理类
)
public interface DishServiceClient {
    @GetMapping("/api/dishes/{id}")
    DishDTO getDishById(@PathVariable Long id);
}

// 降级处理
@Component
public class DishServiceFallback implements DishServiceClient {
    
    @Override
    public DishDTO getDishById(Long id) {
        // 返回默认数据或缓存数据
        DishDTO dish = new DishDTO();
        dish.setId(id);
        dish.setName("菜品暂时无法获取");
        dish.setStatus("unavailable");
        return dish;
    }
}
```

**Sentinel 熔断规则：**
```
资源名：restaurant-dish
熔断策略：
  - 异常比例：50%（错误率超过 50% 触发熔断）
  - 时间窗口：10 秒
  - 最小请求数：5
  
熔断后：
  - 直接调用 fallback 方法
  - 10 秒后进入半开状态
  - 尝试恢复调用
```

**实际案例：**
```
场景：Dish 服务宕机

没有熔断：
Order 服务 → 调用 Dish 服务 → 超时等待 → 大量请求堆积 → Order 服务也崩溃 ❌

使用 Sentinel：
Order 服务 → 调用 Dish 服务 → 失败 5 次 → 触发熔断 → 
直接返回降级数据 → Order 服务正常运行 ✅
```

---

#### 2.3 系统保护

**应用场景：**
- CPU 使用率过高时限流
- 内存不足时拒绝请求
- 系统负载过高时保护

**配置示例：**
```yaml
# Sentinel 系统规则
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
      datasource:
        system:
          nacos:
            data-id: sentinel-system-rules
            rule-type: system
```

**系统规则：**
```json
[
  {
    "resource": "system",
    "highestSystemLoad": 10.0,  // 系统负载阈值
    "avgRt": 1000,  // 平均响应时间（ms）
    "maxThread": 100,  // 最大并发线程数
    "qps": 500  // 系统级 QPS 阈值
  }
]
```

---

## 3. Gateway - API 网关

### 🎯 在项目中的应用

#### 3.1 统一路由

**应用场景：**
- 所有外部请求统一入口
- 路由到不同的微服务
- 隐藏内部服务结构

**配置示例：**
```yaml
# Gateway 路由配置
spring:
  cloud:
    gateway:
      routes:
        # 订单服务路由
        - id: restaurant-order
          uri: lb://restaurant-order  # lb = LoadBalancer
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1  # 去掉 /api 前缀
            
        # 菜品服务路由
        - id: restaurant-dish
          uri: lb://restaurant-dish
          predicates:
            - Path=/api/dishes/**
          filters:
            - StripPrefix=1
            
        # 会员服务路由
        - id: restaurant-member
          uri: lb://restaurant-member
          predicates:
            - Path=/api/members/**
          filters:
            - StripPrefix=1
```

**实际效果：**
```
客户端请求：http://gateway:9080/api/orders/123

Gateway 处理：
1. 接收请求
2. 匹配路由规则（/api/orders/**）
3. 去掉前缀（/api）
4. 负载均衡选择实例
5. 转发到：http://order-service:8081/orders/123
```

---

#### 3.2 统一认证

**应用场景：**
- Token 验证
- 用户身份识别
- 权限检查

**代码示例：**
```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                             GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // 白名单：登录接口不需要认证
        if (path.contains("/login")) {
            return chain.filter(exchange);
        }
        
        // 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        
        if (StringUtils.isEmpty(token)) {
            // 没有 Token，返回 401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // 验证 Token
        if (!validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Token 有效，继续处理
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -100;  // 优先级最高
    }
}
```

---

#### 3.3 跨域配置

**配置示例：**
```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");  // 允许所有域名
        config.addAllowedMethod("*");  // 允许所有方法
        config.addAllowedHeader("*");  // 允许所有请求头
        config.setAllowCredentials(true);  // 允许携带 Cookie
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
```

---

#### 3.4 日志记录

**代码示例：**
```java
@Component
@Slf4j
public class LogFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                             GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录请求信息
        log.info("请求路径: {}", request.getPath());
        log.info("请求方法: {}", request.getMethod());
        log.info("请求参数: {}", request.getQueryParams());
        log.info("请求 IP: {}", request.getRemoteAddress());
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            log.info("响应状态: {}", 
                exchange.getResponse().getStatusCode());
            log.info("响应时间: {}ms", endTime - startTime);
        }));
    }
    
    @Override
    public int getOrder() {
        return -50;
    }
}
```

---

## 4. RabbitMQ - 消息队列

### 🎯 在项目中的应用

#### 4.1 异步订单处理

**应用场景：**
- 订单创建后异步通知
- 订单支付成功后更新库存
- 订单超时自动取消

**配置示例：**
```java
@Configuration
public class RabbitMQConfig {
    
    // 订单交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("order.exchange", true, false);
    }
    
    // 订单队列
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("order.queue")
            .ttl(1800000)  // 30 分钟 TTL
            .deadLetterExchange("order.dlx.exchange")  // 死信交换机
            .build();
    }
    
    // 绑定
    @Bean
    public Binding orderBinding() {
        return BindingBuilder
            .bind(orderQueue())
            .to(orderExchange())
            .with("order.create");
    }
}
```

**生产者：**
```java
@Service
public class OrderService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public OrderDTO createOrder(OrderRequest request) {
        // 1. 创建订单
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(request.getTotalAmount());
        orderMapper.insert(order);
        
        // 2. 发送消息到 MQ（异步处理）
        OrderMessage message = new OrderMessage();
        message.setOrderId(order.getId());
        message.setUserId(order.getUserId());
        message.setCreateTime(new Date());
        
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.create",
            message
        );
        
        // 3. 立即返回（不等待后续处理）
        return convertToDTO(order);
    }
}
```

**消费者：**
```java
@Component
@Slf4j
public class OrderMessageConsumer {
    
    @RabbitListener(queues = "order.queue")
    public void handleOrderCreate(OrderMessage message) {
        log.info("收到订单创建消息: {}", message.getOrderId());
        
        try {
            // 1. 扣减库存
            dishService.decreaseStock(message.getDishIds());
            
            // 2. 发送通知
            notificationService.sendOrderNotification(message);
            
            // 3. 更新订单状态
            orderService.updateStatus(message.getOrderId(), "PROCESSING");
            
            log.info("订单处理完成: {}", message.getOrderId());
            
        } catch (Exception e) {
            log.error("订单处理失败: {}", message.getOrderId(), e);
            // 消息会进入死信队列，等待人工处理
            throw e;
        }
    }
}
```

**实际效果：**
```
同步处理（传统方式）：
创建订单 → 扣减库存 → 发送通知 → 返回结果
总耗时：2000ms ❌

异步处理（使用 MQ）：
创建订单 → 发送消息 → 立即返回
总耗时：50ms ✅

后台异步：
接收消息 → 扣减库存 → 发送通知
用户无感知，体验更好
```

---

#### 4.2 订单超时自动取消

**配置示例：**
```java
@Configuration
public class DelayQueueConfig {
    
    // 延迟队列（30 分钟后过期）
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable("order.delay.queue")
            .ttl(1800000)  // 30 分钟
            .deadLetterExchange("order.cancel.exchange")
            .deadLetterRoutingKey("order.cancel")
            .build();
    }
}
```

**使用示例：**
```java
// 订单创建时发送延迟消息
public void createOrder(OrderRequest request) {
    Order order = saveOrder(request);
    
    // 发送延迟消息（30 分钟后处理）
    rabbitTemplate.convertAndSend(
        "order.delay.exchange",
        "order.delay",
        order.getId()
    );
}

// 30 分钟后自动触发
@RabbitListener(queues = "order.cancel.queue")
public void handleOrderTimeout(Long orderId) {
    Order order = orderMapper.selectById(orderId);
    
    // 如果订单还是未支付状态，自动取消
    if ("UNPAID".equals(order.getStatus())) {
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        
        // 恢复库存
        dishService.restoreStock(order.getDishIds());
    }
}
```

---

## 5. Seata - 分布式事务

### 🎯 在项目中的应用场景

**注意：** 当前项目使用 RabbitMQ 实现最终一致性，Seata 可作为强一致性场景的补充方案。

#### 5.1 订单-库存-积分 分布式事务

**场景：** 创建订单时需要：
1. Order 服务：创建订单
2. Dish 服务：扣减库存
3. Member 服务：扣减积分

**Seata AT 模式示例：**
```java
@Service
public class OrderService {
    
    @Autowired
    private DishServiceClient dishServiceClient;
    
    @Autowired
    private MemberServiceClient memberServiceClient;
    
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public OrderDTO createOrder(OrderRequest request) {
        // 1. 创建订单（本地事务）
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(request.getTotalAmount());
        orderMapper.insert(order);
        
        // 2. 扣减库存（远程调用 Dish 服务）
        dishServiceClient.decreaseStock(request.getDishIds());
        
        // 3. 扣减积分（远程调用 Member 服务）
        memberServiceClient.decreasePoints(
            request.getUserId(), 
            request.getPoints()
        );
        
        // 任何一步失败，全部回滚
        return convertToDTO(order);
    }
}
```

**效果：**
```
成功场景：
创建订单 ✅ → 扣减库存 ✅ → 扣减积分 ✅ → 全部提交 ✅

失败场景：
创建订单 ✅ → 扣减库存 ✅ → 扣减积分 ❌ → 全部回滚 ↩️
结果：订单未创建，库存未扣减，积分未扣减
```

---

## 6. Dubbo - RPC 框架

### 🎯 在项目中的应用场景

**注意：** 当前项目使用 OpenFeign 进行服务间调用，Dubbo 可作为高性能场景的替代方案。

#### 6.1 高性能服务调用

**Dubbo vs OpenFeign 对比：**
```
OpenFeign（当前使用）：
- 基于 HTTP/REST
- 性能：中等
- 易用性：高
- 适合：外部 API、跨语言调用

Dubbo（可选）：
- 基于 RPC/TCP
- 性能：高（比 HTTP 快 30-50%）
- 易用性：中等
- 适合：内部服务、高并发场景
```

**Dubbo 示例：**
```java
// 服务提供者（Dish 服务）
@DubboService(version = "1.0.0")
public class DishServiceImpl implements DishService {
    
    @Override
    public DishDTO getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        return convertToDTO(dish);
    }
}

// 服务消费者（Order 服务）
@Service
public class OrderService {
    
    @DubboReference(version = "1.0.0")
    private DishService dishService;
    
    public void createOrder(OrderRequest request) {
        // 直接调用，像本地方法一样
        DishDTO dish = dishService.getDishById(request.getDishId());
        // ...
    }
}
```

---

## 📊 组件使用总结

### 当前项目使用情况

| 组件 | 使用状态 | 应用场景 | 优先级 |
|------|---------|---------|--------|
| **Nacos** | ✅ 已使用 | 服务注册、配置管理 | 必须 |
| **Sentinel** | ✅ 已使用 | 限流、熔断、降级 | 必须 |
| **Gateway** | ✅ 已使用 | API 网关、统一认证 | 必须 |
| **RabbitMQ** | ✅ 已使用 | 异步处理、削峰填谷 | 必须 |
| **Seata** | ⏳ 可选 | 分布式事务（强一致性） | 可选 |
| **Dubbo** | ⏳ 可选 | 高性能 RPC 调用 | 可选 |

---

## 🎯 实际业务流程示例

### 完整的订单创建流程

```
1. 用户提交订单
   ↓
2. Gateway 接收请求
   - 验证 Token ✅
   - 记录日志 📝
   - 限流检查（Sentinel）⚡
   ↓
3. 路由到 Order 服务
   - Nacos 服务发现 🔍
   - 负载均衡选择实例 ⚖️
   ↓
4. Order 服务处理
   - 创建订单记录 💾
   - 发送消息到 MQ 📨
   - 立即返回结果 ⚡
   ↓
5. 异步处理（RabbitMQ）
   - 调用 Dish 服务扣减库存
     * Sentinel 熔断保护 🛡️
     * 失败自动降级 ↘️
   - 调用 Member 服务扣减积分
   - 发送订单通知
   ↓
6. 配置动态更新（Nacos）
   - 订单超时时间可动态调整 🔧
   - 无需重启服务 🚀
```

---

## 💡 面试要点

### 1. Nacos 核心功能
- 服务注册与发现（替代 Eureka）
- 动态配置管理（替代 Config Server）
- 支持 AP 和 CP 模式

### 2. Sentinel 核心功能
- 流量控制（QPS 限流）
- 熔断降级（保护系统）
- 系统保护（CPU、内存）
- 实时监控

### 3. Gateway 核心功能
- 统一路由
- 统一认证
- 限流熔断
- 日志记录

### 4. RabbitMQ 核心功能
- 异步解耦
- 削峰填谷
- 最终一致性
- 延迟队列

### 5. 为什么选择这些组件？
- **Nacos**：阿里开源，功能强大，社区活跃
- **Sentinel**：轻量级，性能好，易于集成
- **Gateway**：Spring 官方，与 Spring Cloud 无缝集成
- **RabbitMQ**：成熟稳定，功能丰富，易于使用

---

**文档版本：** v1.0.0  
**最后更新：** 2026年3月4日  
**适用项目：** 七云菜馆餐厅管理系统

