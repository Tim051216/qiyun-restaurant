# RabbitMQ 服务部署指南

## 概述

本目录包含RabbitMQ消息队列服务的Docker部署配置。RabbitMQ用于实现订单服务的异步消息处理。

## 配置说明

### 服务信息
- **镜像**: rabbitmq:3.12-management-alpine
- **AMQP端口**: 5672
- **管理界面端口**: 15672
- **虚拟主机**: restaurant
- **默认用户**: admin / admin123

### 数据持久化
- 消息数据: `rabbitmq-data` 卷
- 日志文件: `rabbitmq-logs` 卷

## 启动步骤

### 1. 确保Docker网络存在
```bash
docker network create restaurant-network
```

### 2. 启动RabbitMQ服务
```bash
# 进入RabbitMQ配置目录
cd restaurant-server/docker/rabbitmq

# 启动服务
docker-compose -f docker-compose-rabbitmq.yml up -d

# 查看启动状态
docker-compose -f docker-compose-rabbitmq.yml ps

# 查看日志
docker-compose -f docker-compose-rabbitmq.yml logs -f
```

### 3. 验证服务
```bash
# 检查健康状态
docker exec restaurant-rabbitmq rabbitmq-diagnostics ping

# 查看集群状态
docker exec restaurant-rabbitmq rabbitmqctl cluster_status

# 查看虚拟主机
docker exec restaurant-rabbitmq rabbitmqctl list_vhosts
```

## 访问管理界面

打开浏览器访问: http://localhost:15672

- 用户名: `admin`
- 密码: `admin123`

## 交换机和队列配置

应用启动时会自动创建以下交换机和队列：

### 订单交换机
- **名称**: restaurant.order.exchange
- **类型**: Direct
- **持久化**: 是

### 订单队列
1. **订单创建队列**
   - 名称: restaurant.order.create.queue
   - 路由键: order.create
   - TTL: 30秒
   - 死信交换机: restaurant.dlx.exchange

2. **订单支付队列**
   - 名称: restaurant.order.paid.queue
   - 路由键: order.paid
   - TTL: 30秒
   - 死信交换机: restaurant.dlx.exchange

### 死信交换机和队列
- **交换机**: restaurant.dlx.exchange
- **队列**: restaurant.dlx.queue
- **路由键**: dlx

## 停止服务

```bash
# 停止服务
docker-compose -f docker-compose-rabbitmq.yml stop

# 停止并删除容器
docker-compose -f docker-compose-rabbitmq.yml down

# 停止并删除容器和数据卷（谨慎使用）
docker-compose -f docker-compose-rabbitmq.yml down -v
```

## 故障排查

### 服务无法启动
1. 检查端口是否被占用
   ```bash
   netstat -ano | findstr "5672"
   netstat -ano | findstr "15672"
   ```

2. 查看容器日志
   ```bash
   docker logs restaurant-rabbitmq
   ```

### 无法访问管理界面
1. 确认容器正在运行
   ```bash
   docker ps | findstr rabbitmq
   ```

2. 检查防火墙设置

### 消息堆积
1. 查看队列状态
   ```bash
   docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages consumers
   ```

2. 清空队列（开发环境）
   ```bash
   docker exec restaurant-rabbitmq rabbitmqctl purge_queue restaurant.order.create.queue
   ```

## 性能优化

### 连接池配置
应用中已配置连接池参数：
- 发布确认: correlated
- 发布返回: true
- 手动ACK: manual
- 预取数量: 10

### 监控指标
通过管理界面可以查看：
- 消息发送速率
- 消息消费速率
- 队列长度
- 连接数和通道数

## 生产环境建议

1. **集群部署**: 使用RabbitMQ集群提高可用性
2. **镜像队列**: 配置队列镜像实现数据冗余
3. **资源限制**: 设置内存和磁盘告警阈值
4. **监控告警**: 集成Prometheus监控
5. **备份策略**: 定期备份配置和数据

## 相关文档

- [RabbitMQ官方文档](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP文档](https://docs.spring.io/spring-amqp/reference/)
- [消息队列最佳实践](https://www.rabbitmq.com/best-practices.html)
