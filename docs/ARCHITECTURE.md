# 七云菜馆技术栈升级 - 架构设计文档

## 文档版本
- **版本**: 2.0.0
- **日期**: 2026-02-09
- **作者**: 技术团队

## 目录
1. [系统概述](#系统概述)
2. [架构演进](#架构演进)
3. [技术栈](#技术栈)
4. [系统架构](#系统架构)
5. [服务设计](#服务设计)
6. [数据架构](#数据架构)
7. [安全架构](#安全架构)
8. [监控架构](#监控架构)
9. [部署架构](#部署架构)
10. [性能优化](#性能优化)

---

## 系统概述

### 项目背景
七云菜馆餐饮管理系统从Spring Boot 2.7.14 + Java 17单体应用升级到Spring Boot 3.2 + Java 21 + Spring Cloud微服务架构，以满足2026年现代化技术栈要求。

### 升级目标
- ✅ 核心框架升级到最新LTS版本
- ✅ 单体应用拆分为微服务架构
- ✅ 集成消息队列实现异步处理
- ✅ 容器化部署提升运维效率
- ✅ 完善监控体系保障系统稳定
- ✅ 实现限流降级保护系统
- ✅ 多级缓存优化性能
- ✅ 数据库优化支持海量数据

### 系统特点
- **高可用**: 微服务架构，服务独立部署
- **高性能**: 多级缓存，读写分离，分库分表
- **高扩展**: 容器化部署，弹性伸缩
- **高可靠**: 消息队列，熔断降级，限流保护
- **可观测**: 完整的监控、链路追踪、日志系统

---

## 架构演进

### 1.0 单体架构（升级前）

```
┌─────────────────────────────────────┐
│     Spring Boot 2.7.14 单体应用      │
│                                     │
│  ┌──────────┐  ┌──────────┐       │
│  │ 订单模块  │  │ 菜品模块  │       │
│  └──────────┘  └──────────┘       │
│  ┌──────────┐  ┌──────────┐       │
│  │ 会员模块  │  │ 管理模块  │       │
│  └──────────┘  └──────────┘       │
│                                     │
└─────────────────────────────────────┘
           ↓
    ┌──────────┐
    │  MySQL   │
    └──────────┘
```

**问题**:
- 单点故障风险
- 扩展性差
- 部署困难
- 技术栈老旧

### 2.0 微服务架构（升级后）

```
                    ┌─────────────┐
                    │   客户端     │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  API Gateway │
                    │   (8080)     │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐      ┌─────▼─────┐     ┌─────▼─────┐
   │  Order  │      │   Dish    │     │  Member   │
   │ Service │      │  Service  │     │  Service  │
   │ (8081)  │      │  (8082)   │     │  (8083)   │
   └────┬────┘      └─────┬─────┘     └─────┬─────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         ┌────▼───┐   ┌───▼───┐   ┌───▼────┐
         │ MySQL  │   │ Redis │   │RabbitMQ│
         └────────┘   └───────┘   └────────┘
```

**优势**:
- 服务独立部署
- 弹性伸缩
- 技术栈灵活
- 故障隔离

---

## 技术栈

### 核心框架
| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.2 | 核心框架 |
| Java | 21 LTS | 运行环境，支持虚拟线程 |
| Spring Cloud | 2023.0.x | 微服务框架 |

### 微服务组件
| 组件 | 版本 | 说明 |
|------|------|------|
| Nacos | 2.3.0 | 服务注册与配置中心 |
| Spring Cloud Gateway | 4.1.x | API网关 |
| OpenFeign | 4.1.x | 服务调用 |
| Spring Cloud LoadBalancer | 4.1.x | 负载均衡 |
| Sentinel | 1.8.6 | 限流降级 |

### 中间件
| 组件 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.2 | 缓存 |
| RabbitMQ | 3.12 | 消息队列 |
| Caffeine | 3.1.8 | 本地缓存 |

### 数据访问
| 组件 | 版本 | 说明 |
|------|------|------|
| MyBatis Plus | 3.5.5 | ORM框架 |
| ShardingSphere | 5.4.1 | 分库分表 |
| Redisson | 3.27.0 | 分布式锁 |

### 监控运维
| 组件 | 版本 | 说明 |
|------|------|------|
| Prometheus | 2.48 | 指标采集 |
| Grafana | 10.2 | 可视化 |
| SkyWalking | 9.6.0 | 链路追踪 |
| Docker | 24.x | 容器化 |
| Docker Compose | 2.x | 容器编排 |

---

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         客户端层                              │
│  Web浏览器  │  移动App  │  小程序  │  第三方系统              │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      API网关层                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Gateway Service (8080)                             │   │
│  │  - 路由转发  - 认证鉴权  - 限流熔断  - 跨域处理      │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      服务层                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Order   │  │   Dish   │  │  Member  │  │  Admin   │   │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │   │
│  │  (8081)  │  │  (8082)  │  │  (8083)  │  │  (8084)  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    中间件层                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Nacos   │  │ RabbitMQ │  │  Redis   │  │  MySQL   │   │
│  │  注册配置 │  │  消息队列 │  │   缓存   │  │  数据库   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    监控层                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │Prometheus│  │ Grafana  │  │SkyWalking│                  │
│  │  指标采集 │  │  可视化   │  │ 链路追踪  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

### 服务交互流程

```
1. 用户请求 → Gateway
2. Gateway → 认证鉴权
3. Gateway → 路由到目标服务
4. 服务 → Nacos服务发现
5. 服务 → 负载均衡选择实例
6. 服务 → Feign调用其他服务
7. 服务 → 查询缓存（Caffeine → Redis）
8. 服务 → 查询数据库（读写分离）
9. 服务 → 发送消息（RabbitMQ）
10. 服务 → 返回响应
11. Gateway → 返回客户端
```

---

## 服务设计

### 1. Gateway Service (API网关)

**职责**:
- 统一入口，路由转发
- 认证鉴权
- 限流熔断
- 跨域处理
- 日志记录

**端口**: 8080

**关键配置**:
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
            - name: RequestRateLimiter
```

**技术特点**:
- 响应式编程（WebFlux）
- Redis限流
- JWT认证
- 全局过滤器

### 2. Order Service (订单服务)

**职责**:
- 订单创建、查询、更新
- 订单状态流转
- 订单支付处理
- 发送订单消息

**端口**: 8081

**依赖服务**:
- Dish Service (查询菜品信息)
- Member Service (查询会员信息)

**技术特点**:
- Feign服务调用
- RabbitMQ消息发送
- 分库分表（ShardingSphere）
- 读写分离

### 3. Dish Service (菜品服务)

**职责**:
- 菜品管理（CRUD）
- 菜品分类管理
- 菜品库存管理
- 菜品缓存策略

**端口**: 8082

**技术特点**:
- 多级缓存（Caffeine + Redis）
- 布隆过滤器（防缓存穿透）
- 分布式锁（防缓存击穿）
- 缓存预热

### 4. Member Service (会员服务)

**职责**:
- 用户注册登录
- 会员信息管理
- 积分管理
- 优惠券管理

**端口**: 8083

**技术特点**:
- Redis缓存
- Sentinel限流
- 数据加密

### 5. Admin Service (管理服务)

**职责**:
- 员工管理
- 权限管理
- 系统配置
- 数据统计

**端口**: 8084

**技术特点**:
- RBAC权限控制
- 数据统计分析

---

## 数据架构

### 数据库设计

#### 主要表结构

**订单表 (t_order)**
```sql
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
);
```

**菜品表 (t_dish)**
```sql
CREATE TABLE t_dish (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    status TINYINT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
);
```

### 读写分离

```
写操作 → Master Database
读操作 → Slave Database (轮询)

配置:
- 1个主库（写）
- 2个从库（读）
- 负载均衡策略: ROUND_ROBIN
```

### 分库分表

**订单表分片策略**:
```
数据库分片: 按user_id哈希 → 16个库
表分片: 按create_time时间 → 按月分表

示例:
user_id=12345 → ds_9.t_order_2026_02
user_id=67890 → ds_10.t_order_2026_02
```

### 缓存架构

**三级缓存**:
```
L1: Caffeine (本地缓存)
    - 容量: 10000条
    - 过期: 5分钟
    - 命中率: 95%
    - 响应: <1ms

L2: Redis (分布式缓存)
    - 过期: 5-10分钟（随机）
    - 命中率: 90%
    - 响应: <10ms

L3: MySQL (数据库)
    - 响应: <100ms
```

**缓存保护**:
- 缓存穿透: 布隆过滤器
- 缓存击穿: 分布式锁
- 缓存雪崩: 过期时间随机化

---

## 安全架构

### 认证授权

**JWT认证流程**:
```
1. 用户登录 → 验证用户名密码
2. 生成JWT Token
3. 返回Token给客户端
4. 客户端请求携带Token
5. Gateway验证Token
6. 解析用户信息
7. 转发到后端服务
```

### 数据安全

- 敏感数据加密存储
- HTTPS传输
- SQL注入防护
- XSS防护
- CSRF防护

### 限流保护

**Sentinel限流规则**:
```
QPS限流: 100 QPS/秒
热点参数限流: 10 QPS/秒/商品
系统自适应保护: CPU > 80%触发
```

---

## 监控架构

### 指标监控 (Prometheus + Grafana)

**采集指标**:
- JVM指标: 堆内存、GC、线程
- 系统指标: CPU、内存、磁盘
- 业务指标: QPS、响应时间、错误率
- 中间件指标: Redis、MySQL、RabbitMQ

**告警规则**:
- CPU使用率 > 80%
- 内存使用率 > 85%
- 错误率 > 5%
- 响应时间 > 1s

### 链路追踪 (SkyWalking)

**追踪内容**:
- 服务间调用链路
- 数据库查询
- 缓存访问
- 消息队列

**性能分析**:
- 慢查询分析
- 热点接口分析
- 异常分析

### 日志系统

**日志格式**: JSON
**日志级别**: INFO, WARN, ERROR
**日志内容**:
- 请求日志
- 业务日志
- 错误日志
- 审计日志

---

## 部署架构

### 容器化部署

**Docker镜像**:
```
restaurant-gateway:latest
restaurant-order:latest
restaurant-dish:latest
restaurant-member:latest
restaurant-admin:latest
```

**Docker Compose编排**:
```yaml
services:
  - mysql-master
  - mysql-slave
  - redis
  - rabbitmq
  - nacos
  - gateway
  - order-service
  - dish-service
  - member-service
  - admin-service
  - prometheus
  - grafana
```

### 环境配置

**开发环境**:
- 单机部署
- 内存: 8GB
- CPU: 4核

**测试环境**:
- Docker Compose
- 内存: 16GB
- CPU: 8核

**生产环境**:
- Kubernetes集群
- 内存: 32GB+
- CPU: 16核+
- 高可用部署

---

## 性能优化

### 应用层优化

1. **虚拟线程**: Java 21虚拟线程，提升并发能力
2. **连接池**: HikariCP连接池优化
3. **异步处理**: CompletableFuture异步编程
4. **批量操作**: 批量查询和更新

### 缓存优化

1. **多级缓存**: Caffeine + Redis三级缓存
2. **缓存预热**: 启动时加载热点数据
3. **缓存更新**: 异步更新策略
4. **缓存保护**: 布隆过滤器、分布式锁

### 数据库优化

1. **读写分离**: 1主2从，读写分离
2. **分库分表**: 16个库，按月分表
3. **索引优化**: 合理创建索引
4. **SQL优化**: 避免慢查询

### 网络优化

1. **HTTP/2**: 支持HTTP/2协议
2. **GZIP压缩**: 响应数据压缩
3. **CDN加速**: 静态资源CDN
4. **连接复用**: Keep-Alive

---

## 附录

### 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API网关 |
| Order Service | 8081 | 订单服务 |
| Dish Service | 8082 | 菜品服务 |
| Member Service | 8083 | 会员服务 |
| Admin Service | 8084 | 管理服务 |
| Nacos | 8848 | 服务注册 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ管理 | 15672 | 管理界面 |
| Prometheus | 9090 | 指标采集 |
| Grafana | 3000 | 可视化 |

### 参考文档

- [Spring Boot 3.2文档](https://spring.io/projects/spring-boot)
- [Spring Cloud文档](https://spring.io/projects/spring-cloud)
- [Nacos文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Sentinel文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [ShardingSphere文档](https://shardingsphere.apache.org/document/current/cn/overview/)
