# Docker容器化部署指南

## 概述

本文档描述如何使用Docker和Docker Compose一键部署七云菜馆微服务系统。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network                          │
│                   (restaurant-network)                       │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │ RabbitMQ │  │  Nacos   │   │
│  │  :3306   │  │  :6379   │  │  :5672   │  │  :8848   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Gateway  │  │  Order   │  │   Dish   │  │  Member  │   │
│  │  :8080   │  │  :8081   │  │  :8082   │  │  :8083   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│  │  Admin   │  │Prometheus│  │ Grafana  │                 │
│  │  :8084   │  │  :9090   │  │  :3000   │                 │
│  └──────────┘  └──────────┘  └──────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

## 前置要求

### 必需软件
- Docker Desktop 24.x 或更高版本
- Docker Compose 2.x 或更高版本
- 至少 8GB 可用内存
- 至少 20GB 可用磁盘空间

### 验证安装
```bash
docker --version
docker-compose --version
```

## 快速开始

### 1. 启动所有服务

```bash
cd restaurant-server
docker-compose -f docker-compose-full.yml up -d
```

### 2. 查看服务状态

```bash
docker-compose -f docker-compose-full.yml ps
```

### 3. 查看服务日志

```bash
# 查看所有服务日志
docker-compose -f docker-compose-full.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose-full.yml logs -f gateway
docker-compose -f docker-compose-full.yml logs -f order-service
```

### 4. 停止所有服务

```bash
docker-compose -f docker-compose-full.yml down
```

### 5. 停止并删除数据卷

```bash
docker-compose -f docker-compose-full.yml down -v
```

## 服务端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|---------|---------|------|
| Gateway | 8080 | 8080 | API网关 |
| Order Service | 8081 | 8081 | 订单服务 |
| Dish Service | 8082 | 8082 | 菜品服务 |
| Member Service | 8083 | 8083 | 会员服务 |
| Admin Service | 8084 | 8084 | 管理服务 |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| RabbitMQ | 5672 | 5672 | 消息队列 |
| RabbitMQ管理界面 | 15672 | 15672 | MQ管理控制台 |
| Nacos | 8848 | 8848 | 服务注册中心 |
| Prometheus | 9090 | 9090 | 监控指标 |
| Grafana | 3000 | 3000 | 监控仪表板 |

## 访问地址

### 应用服务
- **API网关**: http://localhost:8080
- **网关健康检查**: http://localhost:8080/actuator/health
- **订单服务**: http://localhost:8081
- **菜品服务**: http://localhost:8082
- **会员服务**: http://localhost:8083
- **管理服务**: http://localhost:8084

### 基础设施
- **Nacos控制台**: http://localhost:8848/nacos
  - 用户名: nacos
  - 密码: nacos

- **RabbitMQ管理界面**: http://localhost:15672
  - 用户名: admin
  - 密码: admin123

- **Prometheus**: http://localhost:9090

- **Grafana**: http://localhost:3000
  - 用户名: admin
  - 密码: admin123

## 环境变量配置

### MySQL配置
```yaml
MYSQL_ROOT_PASSWORD: root123
MYSQL_DATABASE: restaurant_db
MYSQL_USER: restaurant
MYSQL_PASSWORD: restaurant123
```

### Redis配置
```yaml
REDIS_PASSWORD: redis123
```

### RabbitMQ配置
```yaml
RABBITMQ_DEFAULT_USER: admin
RABBITMQ_DEFAULT_PASS: admin123
```

### Nacos配置
```yaml
NACOS_SERVER_ADDR: nacos:8848
NACOS_NAMESPACE: dev
```

## 健康检查

所有服务都配置了健康检查，确保服务正常启动后才接受流量。

### 检查服务健康状态
```bash
# 检查Gateway
curl http://localhost:8080/actuator/health

# 检查Order Service
curl http://localhost:8081/actuator/health

# 检查Dish Service
curl http://localhost:8082/actuator/health

# 检查Member Service
curl http://localhost:8083/actuator/health

# 检查Admin Service
curl http://localhost:8084/actuator/health
```

## 数据持久化

以下数据通过Docker卷持久化：

- `mysql-master-data`: MySQL数据
- `redis-data`: Redis数据
- `rabbitmq-data`: RabbitMQ数据
- `nacos-data`: Nacos配置数据
- `nacos-logs`: Nacos日志
- `prometheus-data`: Prometheus监控数据
- `grafana-data`: Grafana仪表板配置

### 查看数据卷
```bash
docker volume ls | grep restaurant
```

### 备份数据卷
```bash
# 备份MySQL数据
docker run --rm -v restaurant-server_mysql-master-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# 备份Redis数据
docker run --rm -v restaurant-server_redis-data:/data -v $(pwd):/backup alpine tar czf /backup/redis-backup.tar.gz /data
```

## 网络配置

所有服务运行在同一个Docker网络 `restaurant-network` 中，使用桥接模式。

- 子网: 172.20.0.0/16
- 服务间通过服务名互相访问（如 `mysql-master`, `redis`, `nacos`）

## 故障排查

### 1. 服务启动失败

```bash
# 查看服务日志
docker-compose -f docker-compose-full.yml logs [service-name]

# 查看容器状态
docker-compose -f docker-compose-full.yml ps

# 重启特定服务
docker-compose -f docker-compose-full.yml restart [service-name]
```

### 2. 健康检查失败

```bash
# 进入容器检查
docker exec -it restaurant-gateway sh

# 检查网络连接
docker exec -it restaurant-gateway ping nacos
docker exec -it restaurant-gateway ping redis
```

### 3. 端口冲突

如果主机端口已被占用，修改 `docker-compose-full.yml` 中的端口映射：

```yaml
ports:
  - "18080:8080"  # 将主机端口改为18080
```

### 4. 内存不足

```bash
# 查看容器资源使用
docker stats

# 限制容器内存
docker-compose -f docker-compose-full.yml up -d --scale order-service=1
```

### 5. 数据库连接失败

```bash
# 检查MySQL是否就绪
docker exec -it restaurant-mysql-master mysqladmin ping -h localhost -uroot -proot123

# 查看MySQL日志
docker logs restaurant-mysql-master
```

## 性能优化

### 1. JVM参数调优

在 `docker-compose-full.yml` 中调整 `JAVA_OPTS`:

```yaml
environment:
  JAVA_OPTS: "-XX:+UseZGC -Xms1g -Xmx2g"
```

### 2. 资源限制

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 2G
    reservations:
      cpus: '1'
      memory: 1G
```

### 3. 并发连接数

调整数据库连接池大小：

```yaml
environment:
  SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: 20
```

## 生产环境建议

### 1. 使用外部数据库
生产环境建议使用云数据库服务（如RDS），而不是容器化的MySQL。

### 2. 配置日志收集
集成ELK或其他日志收集系统。

### 3. 配置告警
在Prometheus中配置告警规则，集成AlertManager。

### 4. 使用Kubernetes
对于大规模部署，建议使用Kubernetes进行容器编排。

### 5. 镜像优化
- 使用多阶段构建减小镜像大小
- 使用Alpine基础镜像
- 定期更新基础镜像修复安全漏洞

## 开发环境

### 仅启动基础设施服务

```bash
docker-compose -f docker-compose-full.yml up -d mysql-master redis rabbitmq nacos
```

然后在IDE中启动应用服务进行开发调试。

### 热重载

使用Spring Boot DevTools实现代码热重载：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

## 常用命令

```bash
# 构建所有镜像
docker-compose -f docker-compose-full.yml build

# 强制重新构建
docker-compose -f docker-compose-full.yml build --no-cache

# 启动并查看日志
docker-compose -f docker-compose-full.yml up

# 后台启动
docker-compose -f docker-compose-full.yml up -d

# 停止服务
docker-compose -f docker-compose-full.yml stop

# 删除容器
docker-compose -f docker-compose-full.yml rm -f

# 查看服务状态
docker-compose -f docker-compose-full.yml ps

# 扩容服务
docker-compose -f docker-compose-full.yml up -d --scale order-service=3

# 查看资源使用
docker stats
```

## 更新日志

### v1.0.0 (2026-02-08)
- 初始版本
- 支持5个微服务 + 4个基础设施服务 + 2个监控服务
- 配置健康检查和自动重启
- 配置数据持久化

## 技术支持

如有问题，请查看：
1. 服务日志: `docker-compose logs -f [service-name]`
2. 健康检查: `curl http://localhost:8080/actuator/health`
3. Nacos控制台: http://localhost:8848/nacos

---

**最后更新**: 2026-02-08
