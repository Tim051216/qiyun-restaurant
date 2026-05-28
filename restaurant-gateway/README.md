# Restaurant Gateway Service

## 概述

API网关服务，作为所有微服务的统一入口，提供路由转发、认证鉴权、限流熔断等功能。

## 功能特性

### 1. 路由转发
- 订单服务：`/api/order/**` → `restaurant-order-service`
- 菜品服务：`/api/dish/**` → `restaurant-dish-service`
- 会员服务：`/api/member/**` → `restaurant-member-service`
- 管理服务：`/api/admin/**` → `restaurant-admin-service`

### 2. 认证鉴权
- JWT令牌验证
- 白名单路径配置
- 用户信息传递

### 3. 限流保护
- 基于IP的限流
- 基于用户的限流
- 基于API路径的限流
- Redis存储限流计数

### 4. 跨域支持
- 全局CORS配置
- 支持所有HTTP方法
- 自定义响应头

### 5. 监控指标
- Prometheus指标导出
- 请求响应时间统计
- 路由转发统计

## 快速启动

### 1. 本地开发

```bash
cd restaurant-gateway
mvn spring-boot:run
```

### 2. Docker部署

```bash
docker build -t restaurant-gateway:2.0.0 .
docker run -p 8080:8080 restaurant-gateway:2.0.0
```

### 3. 访问网关

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看路由信息
curl http://localhost:8080/actuator/gateway/routes
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| NACOS_SERVER_ADDR | Nacos服务地址 | localhost:8848 |
| NACOS_NAMESPACE | Nacos命名空间 | dev |
| REDIS_HOST | Redis主机地址 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| SENTINEL_DASHBOARD | Sentinel控制台地址 | localhost:8858 |

### 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://restaurant-order-service
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=1
```

### 限流配置

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 100  # 每秒补充令牌数
      redis-rate-limiter.burstCapacity: 200  # 令牌桶容量
      key-resolver: "#{@ipKeyResolver}"      # Key解析器
```

## API示例

### 1. 登录获取Token

```bash
curl -X POST http://localhost:8080/api/member/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"123456"}'
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 2. 使用Token访问受保护资源

```bash
curl -X GET http://localhost:8080/api/order/list \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. 查看网关路由

```bash
curl http://localhost:8080/actuator/gateway/routes
```

## 过滤器说明

### 全局过滤器

1. **AuthenticationFilter** (优先级: -100)
   - JWT令牌验证
   - 白名单路径放行
   - 用户信息传递

2. **LoggingFilter** (优先级: -50)
   - 请求日志记录
   - 响应时间统计
   - 状态码记录

### 路由过滤器

1. **StripPrefix**
   - 移除路径前缀
   - 例如：`/api/order/list` → `/order/list`

2. **RequestRateLimiter**
   - 请求限流
   - 基于Redis令牌桶算法

## 限流策略

### IP限流

```java
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest()
            .getRemoteAddress()
            .getAddress()
            .getHostAddress()
    );
}
```

### 用户限流

```java
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest()
            .getQueryParams()
            .getFirst("userId")
    );
}
```

## 监控和运维

### 健康检查

```bash
curl http://localhost:8080/actuator/health
```

### Prometheus指标

```bash
curl http://localhost:8080/actuator/prometheus
```

### 查看路由信息

```bash
# 所有路由
curl http://localhost:8080/actuator/gateway/routes

# 特定路由
curl http://localhost:8080/actuator/gateway/routes/order-service
```

### 刷新路由

```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

## 故障排查

### 1. 路由404错误

检查事项：
- 目标服务是否已注册到Nacos
- 路由配置是否正确
- 服务名称是否匹配

```bash
# 查看Nacos注册的服务
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10
```

### 2. 认证失败

检查事项：
- Token是否有效
- JWT密钥是否正确
- 路径是否在白名单中

### 3. 限流触发

检查事项：
- Redis连接是否正常
- 限流配置是否合理
- Key解析器是否正确

## 性能优化

### 1. 连接池配置

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
```

### 2. 超时配置

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 3000
        response-timeout: 5s
```

### 3. 虚拟线程

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## 安全建议

1. **使用HTTPS**
   - 生产环境必须使用HTTPS
   - 配置SSL证书

2. **JWT密钥管理**
   - 使用强密钥
   - 定期轮换密钥
   - 不要硬编码密钥

3. **限流配置**
   - 根据实际流量调整限流阈值
   - 区分不同API的限流策略

4. **日志脱敏**
   - 不记录敏感信息
   - 密码、Token等信息脱敏

## 参考资料

- [Spring Cloud Gateway官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Sentinel官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
