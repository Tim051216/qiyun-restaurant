# Sentinel Dashboard 部署指南

## 概述

Sentinel Dashboard是Sentinel的控制台，提供流量控制、熔断降级等规则的可视化管理。

## 快速启动

### 1. 启动Sentinel Dashboard

```bash
# 在restaurant-server/docker/sentinel目录下执行
docker-compose -f docker-compose-sentinel.yml up -d
```

### 2. 访问Dashboard

- 地址：http://localhost:8858
- 用户名：sentinel
- 密码：sentinel123

### 3. 查看日志

```bash
docker logs -f restaurant-sentinel-dashboard
```

### 4. 停止服务

```bash
docker-compose -f docker-compose-sentinel.yml down
```

## 配置说明

### Dashboard配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 端口 | 8858 | Dashboard访问端口 |
| 用户名 | sentinel | 登录用户名 |
| 密码 | sentinel123 | 登录密码 |

### 客户端配置

微服务需要在application.yml中配置Sentinel Dashboard地址：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858  # Dashboard地址
        port: 8719                 # 客户端端口
        heartbeat-interval-ms: 10000  # 心跳间隔
      eager: true  # 启动时立即连接Dashboard
```

## 功能说明

### 1. 实时监控

- 查看实时QPS、响应时间、异常数等指标
- 查看资源调用链路
- 查看机器列表和健康状态

### 2. 流控规则

配置QPS限流规则：

- **资源名**：@SentinelResource注解的value值
- **阈值类型**：QPS或并发线程数
- **阈值**：限流阈值
- **流控模式**：直接、关联、链路
- **流控效果**：快速失败、Warm Up、排队等待

示例：
```
资源名：orderCreate
阈值类型：QPS
阈值：100
流控模式：直接
流控效果：快速失败
```

### 3. 降级规则

配置熔断降级规则：

- **慢调用比例**：响应时间超过阈值的比例
- **异常比例**：异常请求的比例
- **异常数**：异常请求的数量

示例：
```
资源名：DishServiceClient#getDishById(Long)
降级策略：异常比例
比例阈值：0.5 (50%)
时间窗口：10秒
最小请求数：5
```

### 4. 热点参数限流

针对特定参数值进行限流：

- **参数索引**：方法参数的位置（从0开始）
- **阈值类型**：QPS
- **阈值**：限流阈值
- **参数例外项**：特定参数值的特殊阈值

示例：
```
资源名：getDish
参数索引：0 (商品ID)
阈值类型：QPS
阈值：10
参数例外项：
  - 参数值：1001 (热门商品)
  - 阈值：50
```

### 5. 系统规则

系统级别的保护规则：

- **Load**：系统负载
- **RT**：平均响应时间
- **线程数**：并发线程数
- **入口QPS**：入口流量QPS
- **CPU使用率**：CPU使用率

### 6. 授权规则

黑白名单控制：

- **流控应用**：调用方应用名称
- **授权类型**：白名单或黑名单

## 规则持久化

### Nacos持久化配置

规则会自动持久化到Nacos配置中心：

| 规则类型 | Nacos DataId | Group |
|---------|--------------|-------|
| 流控规则 | restaurant-order-service-flow-rules | SENTINEL_GROUP |
| 降级规则 | restaurant-order-service-degrade-rules | SENTINEL_GROUP |
| 热点参数规则 | restaurant-order-service-param-flow-rules | SENTINEL_GROUP |
| 系统规则 | restaurant-order-service-system-rules | SENTINEL_GROUP |
| 授权规则 | restaurant-order-service-authority-rules | SENTINEL_GROUP |

### 查看Nacos配置

1. 访问Nacos控制台：http://localhost:8848/nacos
2. 登录（用户名/密码：nacos/nacos）
3. 进入"配置管理" -> "配置列表"
4. 选择命名空间：dev
5. 查看Group：SENTINEL_GROUP

### 规则格式示例

流控规则（JSON格式）：
```json
[
  {
    "resource": "orderCreate",
    "limitApp": "default",
    "grade": 1,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

降级规则（JSON格式）：
```json
[
  {
    "resource": "DishServiceClient#getDishById(Long)",
    "grade": 0,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000,
    "slowRatioThreshold": 0.5
  }
]
```

## 使用流程

### 1. 启动服务

```bash
# 1. 启动Nacos（如果还没启动）
cd restaurant-server/docker/nacos
docker-compose -f docker-compose-nacos.yml up -d

# 2. 启动Sentinel Dashboard
cd ../sentinel
docker-compose -f docker-compose-sentinel.yml up -d

# 3. 启动微服务
cd ../../restaurant-order
mvn spring-boot:run
```

### 2. 访问Dashboard

打开浏览器访问：http://localhost:8858

### 3. 查看服务

在左侧菜单中可以看到已连接的服务：
- restaurant-order-service

### 4. 配置规则

1. 点击服务名称
2. 选择"流控规则"或"降级规则"
3. 点击"新增流控规则"或"新增降级规则"
4. 填写规则配置
5. 点击"新增"保存

### 5. 测试规则

使用curl或Postman测试接口：

```bash
# 测试订单创建（QPS限流：100/秒）
for i in {1..150}; do
  curl -X POST http://localhost:8080/api/order \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"dishId":1,"quantity":1,"amount":50}' &
done

# 观察Dashboard中的实时监控
# 超过100 QPS的请求会被限流
```

### 6. 查看监控

在Dashboard中可以看到：
- 实时QPS曲线
- 通过QPS和拒绝QPS
- 响应时间
- 异常数

## 常见问题

### 1. Dashboard看不到服务

**原因**：
- 服务未启动
- 服务未配置Dashboard地址
- 服务未触发任何Sentinel资源

**解决**：
- 确保服务已启动
- 检查application.yml中的sentinel.transport.dashboard配置
- 访问一次被@SentinelResource注解的接口，触发Sentinel初始化

### 2. 规则不生效

**原因**：
- 规则配置错误
- 资源名不匹配
- Nacos连接失败

**解决**：
- 检查资源名是否与@SentinelResource的value一致
- 检查Nacos连接配置
- 查看服务日志中的Sentinel相关日志

### 3. 规则不持久化

**原因**：
- 未配置Nacos数据源
- Nacos连接失败

**解决**：
- 检查application.yml中的sentinel.datasource配置
- 确保Nacos服务正常运行
- 检查Nacos中是否有对应的配置

## 监控指标

### 关键指标

| 指标 | 说明 | 正常范围 |
|------|------|---------|
| 通过QPS | 成功通过的请求数 | 根据业务 |
| 拒绝QPS | 被限流的请求数 | 应该较低 |
| 异常QPS | 发生异常的请求数 | 应该接近0 |
| 平均RT | 平均响应时间 | <100ms |
| 并发线程数 | 当前并发线程数 | 根据配置 |

### 告警阈值建议

- 拒绝QPS > 10/秒：限流阈值可能设置过低
- 异常QPS > 5/秒：服务可能存在问题
- 平均RT > 1000ms：服务响应慢，可能需要优化
- 并发线程数 > 200：可能存在线程泄漏

## 最佳实践

### 1. 规则配置

- 根据压测结果设置合理的限流阈值
- 熔断阈值不要设置过低，避免误熔断
- 使用热点参数限流保护热点数据
- 配置系统规则作为最后一道防线

### 2. 监控告警

- 定期查看Dashboard监控数据
- 配置Prometheus采集Sentinel指标
- 设置告警规则，及时发现问题

### 3. 规则管理

- 使用Nacos持久化规则，避免重启丢失
- 在Dashboard中统一管理规则
- 定期review规则配置，根据业务调整

### 4. 测试验证

- 上线前进行压测验证
- 验证限流、熔断、降级是否生效
- 验证规则持久化是否正常

## 参考资料

- [Sentinel官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Sentinel Dashboard文档](https://sentinelguard.io/zh-cn/docs/dashboard.html)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
