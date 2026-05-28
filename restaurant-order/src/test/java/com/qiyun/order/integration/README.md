# 集成测试说明

## 概述

本目录包含订单服务的集成测试，用于验证服务与外部依赖（数据库、Redis、RabbitMQ等）的集成是否正常。

## 测试类型

### 1. OrderServiceIntegrationTest
**测试内容：**
- 订单服务与数据库的集成
- 订单服务与Feign客户端的集成
- 完整的订单业务流程

**测试场景：**
- 创建订单并保存到数据库
- 查询订单
- 更新订单状态
- 根据用户ID查询订单列表
- 根据时间范围查询订单
- 订单完整生命周期
- 订单取消流程
- 并发创建订单

### 2. OrderServiceTestcontainersTest
**测试内容：**
- 使用Testcontainers启动真实的MySQL和Redis容器
- 验证服务与真实数据库的交互
- 验证服务与真实Redis的交互

**测试场景：**
- MySQL容器运行验证
- Redis容器运行验证
- 创建订单到真实MySQL
- 查询订单从真实MySQL
- 更新订单到真实MySQL
- 删除订单从真实MySQL
- 事务回滚测试
- 数据库连接池测试

**依赖：**
- Docker环境（用于启动Testcontainers）
- 足够的系统资源（内存、CPU）

### 3. OrderEndToEndTest
**测试内容：**
- 端到端的完整业务流程测试
- 从Controller到Service到Mapper的完整链路
- 消息队列集成验证

**测试场景：**
- 完整的订单创建流程
- 完整的订单支付流程
- 完整的订单取消流程
- 订单完整生命周期
- 查询不存在的订单
- 支付不存在的订单
- 取消不存在的订单
- 消息发送失败不影响订单创建
- 并发创建订单

### 4. MessageQueueIntegrationTest
**测试内容：**
- 消息生产者与RabbitMQ的集成
- 消息消费者与RabbitMQ的集成
- 消息幂等性验证
- 消息重试机制

**测试场景：**
- 发送订单创建消息到RabbitMQ
- 发送订单支付消息到RabbitMQ
- 发送订单取消消息到RabbitMQ
- 消息幂等性验证
- 消息确认机制
- 批量发送消息
- 消息发送失败重试
- 验证消息路由键正确性
- 消息TTL过期测试
- 消息持久化验证

## 运行测试

### 前置条件

1. **安装Docker**（用于Testcontainers）
   ```bash
   docker --version
   ```

2. **启动本地依赖服务**（非Testcontainers测试需要）
   ```bash
   # 启动MySQL
   docker run -d --name mysql-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0
   
   # 启动Redis
   docker run -d --name redis-test -p 6379:6379 redis:7.2-alpine
   
   # 启动RabbitMQ
   docker run -d --name rabbitmq-test -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management
   ```

### 运行所有集成测试

```bash
# Maven
mvn test -Dtest=*IntegrationTest

# 或者运行特定测试类
mvn test -Dtest=OrderServiceIntegrationTest
mvn test -Dtest=OrderServiceTestcontainersTest
mvn test -Dtest=OrderEndToEndTest
mvn test -Dtest=MessageQueueIntegrationTest
```

### 运行Testcontainers测试

```bash
# 确保Docker正在运行
docker ps

# 运行Testcontainers测试
mvn test -Dtest=OrderServiceTestcontainersTest
```

## 测试配置

### application-test.yml
测试环境的配置文件，包含：
- 数据源配置
- Redis配置
- RabbitMQ配置
- MyBatis Plus配置
- 日志配置

### test-schema.sql
测试数据库的初始化脚本，包含：
- 表结构定义
- 测试数据插入

## 注意事项

1. **Testcontainers测试**
   - 需要Docker环境
   - 首次运行会下载Docker镜像，可能较慢
   - 占用较多系统资源
   - 测试结束后会自动清理容器

2. **数据清理**
   - 每个测试方法执行前会清理测试数据
   - 使用@Transactional注解确保测试数据不污染数据库
   - 测试结束后会自动回滚事务

3. **Mock vs 真实依赖**
   - OrderServiceIntegrationTest：使用真实数据库，Mock Feign客户端
   - OrderServiceTestcontainersTest：使用Testcontainers启动的真实容器
   - OrderEndToEndTest：使用真实数据库，Mock消息队列
   - MessageQueueIntegrationTest：使用真实RabbitMQ，Mock业务服务

4. **并发测试**
   - 并发测试可能受系统资源限制
   - 建议在性能测试环境中运行大规模并发测试

## 测试覆盖率

集成测试覆盖以下组件：
- ✅ OrderService（订单服务）
- ✅ OrderController（订单控制器）
- ✅ OrderMapper（订单数据访问）
- ✅ OrderMessageProducer（消息生产者）
- ✅ OrderMessageConsumer（消息消费者）
- ✅ MySQL数据库集成
- ✅ Redis缓存集成
- ✅ RabbitMQ消息队列集成

## 故障排查

### 1. Testcontainers启动失败
```
错误：Could not find a valid Docker environment
解决：确保Docker正在运行，并且当前用户有权限访问Docker
```

### 2. 数据库连接失败
```
错误：Communications link failure
解决：检查MySQL是否启动，端口是否正确，用户名密码是否正确
```

### 3. Redis连接失败
```
错误：Unable to connect to Redis
解决：检查Redis是否启动，端口是否正确
```

### 4. RabbitMQ连接失败
```
错误：Connection refused
解决：检查RabbitMQ是否启动，端口是否正确，用户名密码是否正确
```

## 持续集成

在CI/CD流程中运行集成测试：

```yaml
# GitHub Actions示例
- name: Run Integration Tests
  run: mvn test -Dtest=*IntegrationTest
  
- name: Run Testcontainers Tests
  run: mvn test -Dtest=*TestcontainersTest
```

## 参考资料

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers](https://www.testcontainers.org/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://site.mockito.org/)
