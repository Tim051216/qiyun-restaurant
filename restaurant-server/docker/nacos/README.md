# Nacos 注册和配置中心

## 概述

Nacos是阿里巴巴开源的服务注册与配置中心，用于微服务架构中的服务发现、配置管理和服务管理。

## 快速启动

### 1. 启动Nacos（单机模式）

```bash
cd restaurant-server/docker/nacos
docker-compose -f docker-compose-nacos.yml up -d
```

### 2. 访问Nacos控制台

- URL: http://localhost:8848/nacos
- 默认用户名: nacos
- 默认密码: nacos

### 3. 验证服务状态

```bash
curl http://localhost:8848/nacos/v1/console/health/readiness
```

## 配置说明

### 环境变量

- `MODE`: 运行模式（standalone/cluster）
- `SPRING_DATASOURCE_PLATFORM`: 数据源平台（mysql）
- `MYSQL_SERVICE_HOST`: MySQL主机地址
- `MYSQL_SERVICE_DB_NAME`: Nacos配置数据库名

### 端口说明

- `8848`: HTTP API端口
- `9848`: gRPC端口（客户端请求）
- `9849`: gRPC端口（服务端请求）

## 服务注册配置

### Spring Boot应用配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: RESTAURANT_GROUP
      config:
        server-addr: localhost:8848
        file-extension: yaml
        namespace: dev
        group: RESTAURANT_GROUP
```

## 命名空间和分组

### 命名空间（Namespace）

- `dev`: 开发环境
- `test`: 测试环境
- `prod`: 生产环境

### 分组（Group）

- `RESTAURANT_GROUP`: 餐厅服务组
- `DEFAULT_GROUP`: 默认分组

## 配置管理

### 创建配置

1. 登录Nacos控制台
2. 进入"配置管理" -> "配置列表"
3. 点击"+"创建配置
4. 填写Data ID、Group、配置内容
5. 发布配置

### 配置示例

**Data ID**: restaurant-order-service.yaml
**Group**: RESTAURANT_GROUP

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://mysql-master:3306/restaurant_db
    username: root
    password: root123
```

## 服务发现

### 查看已注册服务

1. 登录Nacos控制台
2. 进入"服务管理" -> "服务列表"
3. 查看所有已注册的服务实例

### 服务健康检查

Nacos会定期检查服务实例的健康状态：
- 健康检查间隔: 5秒
- 不健康阈值: 3次失败
- 自动剔除不健康实例

## 数据持久化

### MySQL存储

Nacos使用MySQL存储配置数据：
- 数据库: `nacos_config`
- 主要表: `config_info`, `config_info_aggr`, `config_info_beta`

### 数据备份

```bash
# 备份Nacos配置
docker exec restaurant-nacos mysqldump -h mysql-master -u root -proot123 nacos_config > nacos_backup.sql

# 恢复配置
docker exec -i restaurant-nacos mysql -h mysql-master -u root -proot123 nacos_config < nacos_backup.sql
```

## 集群部署（生产环境）

### 集群配置

1. 修改`MODE=cluster`
2. 配置`cluster.conf`文件
3. 配置负载均衡器（Nginx/SLB）

### 集群节点配置

```
# cluster.conf
192.168.1.101:8848
192.168.1.102:8848
192.168.1.103:8848
```

## 监控和运维

### 健康检查

```bash
# 检查Nacos健康状态
curl http://localhost:8848/nacos/actuator/health

# 检查服务列表
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10
```

### 日志查看

```bash
# 查看Nacos日志
docker logs -f restaurant-nacos

# 查看持久化日志
tail -f ./logs/nacos.log
```

## 故障排查

### 常见问题

1. **服务注册失败**
   - 检查网络连接
   - 验证Nacos地址配置
   - 查看应用日志

2. **配置无法加载**
   - 确认Data ID和Group正确
   - 检查命名空间配置
   - 验证配置格式

3. **MySQL连接失败**
   - 检查MySQL服务状态
   - 验证数据库连接信息
   - 确认数据库已创建

## 安全配置

### 修改默认密码

1. 登录Nacos控制台
2. 进入"权限控制" -> "用户列表"
3. 修改nacos用户密码

### 开启鉴权

```yaml
nacos:
  core:
    auth:
      enabled: true
      server:
        identity:
          key: serverIdentity
          value: security
```

## 参考资料

- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Nacos GitHub](https://github.com/alibaba/nacos)
- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba)
