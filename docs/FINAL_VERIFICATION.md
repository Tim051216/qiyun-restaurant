# 最终验证和性能测试指南

## 文档说明

本文档描述七云菜馆技术栈升级项目的最终验证和性能测试流程。

**版本**: 2.0.0  
**更新日期**: 2026-02-09

---

## 目录

1. [验证准备](#验证准备)
2. [功能验证](#功能验证)
3. [性能测试](#性能测试)
4. [监控验证](#监控验证)
5. [容器化验证](#容器化验证)
6. [验证清单](#验证清单)
7. [问题排查](#问题排查)

---

## 验证准备

### 1. 环境要求

**硬件要求**:
- CPU: 8核+
- 内存: 16GB+
- 磁盘: 100GB+

**软件要求**:
- Docker 24.x
- Docker Compose 2.x
- Apache Bench (ab)
- curl
- jq

### 2. 启动系统

```bash
# 进入项目目录
cd restaurant-system

# 启动所有服务
docker-compose up -d

# 等待服务就绪（约2分钟）
sleep 120

# 查看服务状态
docker-compose ps
```

### 3. 验证工具安装

```bash
# 安装Apache Bench
sudo apt-get install apache2-utils

# 安装jq
sudo apt-get install jq

# 验证安装
ab -V
jq --version
```

---

## 功能验证

### 1. 服务健康检查

**验证所有服务健康状态**:

```bash
# Gateway
curl http://localhost:8080/actuator/health | jq

# Order Service
curl http://localhost:8081/actuator/health | jq

# Dish Service
curl http://localhost:8082/actuator/health | jq

# Member Service
curl http://localhost:8083/actuator/health | jq

# Admin Service
curl http://localhost:8084/actuator/health | jq
```

**预期结果**: 所有服务返回 `"status": "UP"`

### 2. 服务注册验证

**检查Nacos服务注册**:

```bash
# 查看所有注册服务
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-order-service" | jq

# 验证服务实例数量
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-order-service" | jq '.count'
```

**预期结果**: 每个服务至少有1个实例注册

### 3. 网关路由验证

**测试网关路由**:

```bash
# 测试订单服务路由
curl -X GET "http://localhost:8080/api/order/actuator/health"

# 测试菜品服务路由
curl -X GET "http://localhost:8080/api/dish/actuator/health"
```

**预期结果**: 路由正常，返回200状态码

### 4. 数据库连接验证

**测试数据库连接**:

```bash
# 测试MySQL连接
docker exec mysql-master mysql -uroot -proot123 -e "SELECT 1"

# 查看数据库
docker exec mysql-master mysql -uroot -proot123 -e "SHOW DATABASES"

# 查看表
docker exec mysql-master mysql -uroot -proot123 restaurant_db -e "SHOW TABLES"
```

**预期结果**: 连接成功，数据库和表存在

### 5. Redis连接验证

**测试Redis连接**:

```bash
# 测试连接
docker exec redis redis-cli -a redis123 ping

# 查看信息
docker exec redis redis-cli -a redis123 info server

# 测试读写
docker exec redis redis-cli -a redis123 set test_key "test_value"
docker exec redis redis-cli -a redis123 get test_key
```

**预期结果**: 连接正常，读写成功

### 6. RabbitMQ验证

**测试RabbitMQ**:

```bash
# 查看状态
docker exec rabbitmq rabbitmqctl status

# 查看队列
docker exec rabbitmq rabbitmqctl list_queues

# 查看交换机
docker exec rabbitmq rabbitmqctl list_exchanges

# 访问管理界面
open http://localhost:15672
# 账号: admin/admin123
```

**预期结果**: RabbitMQ运行正常，队列已创建

---

## 性能测试

### 1. 网关性能测试

**低并发测试**:
```bash
ab -n 1000 -c 10 http://localhost:8080/actuator/health
```

**预期结果**: 
- QPS > 100 req/s
- 平均响应时间 < 50ms
- 失败率 = 0%

**中并发测试**:
```bash
ab -n 5000 -c 50 http://localhost:8080/actuator/health
```

**预期结果**:
- QPS > 200 req/s
- 平均响应时间 < 100ms
- 失败率 < 1%

**高并发测试**:
```bash
ab -n 10000 -c 100 http://localhost:8080/actuator/health
```

**预期结果**:
- QPS > 150 req/s
- 平均响应时间 < 200ms
- 失败率 < 5%

### 2. 订单服务性能测试

```bash
# 健康检查性能
ab -n 5000 -c 100 http://localhost:8081/actuator/health
```

**预期结果**:
- QPS > 100 req/s
- 平均响应时间 < 100ms

### 3. 菜品服务性能测试

```bash
# 健康检查性能
ab -n 5000 -c 100 http://localhost:8082/actuator/health
```

**预期结果**:
- QPS > 100 req/s
- 平均响应时间 < 100ms

### 4. 缓存性能测试

**Redis性能测试**:
```bash
# 延迟测试
docker exec redis redis-cli -a redis123 --intrinsic-latency 5

# 基准测试
docker exec redis redis-cli -a redis123 --latency-history
```

**预期结果**:
- 平均延迟 < 1ms
- P99延迟 < 5ms

### 5. 数据库性能测试

**简单查询测试**:
```bash
# 执行100次查询
time for i in {1..100}; do
    docker exec mysql-master mysql -uroot -proot123 -e "SELECT 1" > /dev/null
done
```

**预期结果**:
- 100次查询总耗时 < 5秒
- 平均查询时间 < 50ms

---

## 监控验证

### 1. Prometheus验证

**访问Prometheus**:
```bash
open http://localhost:9090
```

**验证指标采集**:
```bash
# 查询服务状态
curl "http://localhost:9090/api/v1/query?query=up"

# 查询JVM内存
curl "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes"

# 查询HTTP请求
curl "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count"
```

**预期结果**: 所有服务指标正常采集

### 2. Grafana验证

**访问Grafana**:
```bash
open http://localhost:3000
# 账号: admin/admin123
```

**验证仪表板**:
1. 登录Grafana
2. 查看JVM监控仪表板
3. 查看Spring Boot监控仪表板
4. 查看业务指标仪表板

**预期结果**: 所有仪表板显示正常

### 3. 日志验证

**查看应用日志**:
```bash
# Gateway日志
docker logs -f gateway --tail 100

# Order Service日志
docker logs -f order-service --tail 100

# Dish Service日志
docker logs -f dish-service --tail 100
```

**预期结果**: 日志输出正常，无ERROR级别日志

---

## 容器化验证

### 1. 容器状态验证

```bash
# 查看所有容器
docker ps

# 查看容器资源使用
docker stats --no-stream
```

**预期结果**: 所有容器运行中，资源使用正常

### 2. 容器网络验证

```bash
# 查看网络
docker network ls

# 查看网络详情
docker network inspect restaurant-network

# 测试容器间连接
docker exec order-service ping -c 3 mysql-master
docker exec order-service ping -c 3 redis
docker exec order-service ping -c 3 rabbitmq
```

**预期结果**: 容器间网络连通

### 3. 容器健康检查

```bash
# 查看健康状态
docker ps --format "table {{.Names}}\t{{.Status}}"
```

**预期结果**: 所有容器健康状态为healthy

### 4. Docker Compose验证

```bash
# 验证配置
docker-compose config

# 重启服务测试
docker-compose restart order-service

# 查看日志
docker-compose logs order-service
```

**预期结果**: 配置正确，重启成功

---

## 验证清单

### 核心功能验证

- [ ] 所有服务启动成功
- [ ] 所有服务健康检查通过
- [ ] 服务注册到Nacos成功
- [ ] 网关路由正常工作
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] RabbitMQ运行正常

### 性能验证

- [ ] 网关QPS > 100 req/s
- [ ] 服务响应时间 < 100ms
- [ ] Redis延迟 < 1ms
- [ ] 数据库查询 < 50ms
- [ ] CPU使用率 < 80%
- [ ] 内存使用正常

### 监控验证

- [ ] Prometheus采集指标正常
- [ ] Grafana仪表板显示正常
- [ ] 日志输出正常
- [ ] 告警规则配置正确

### 容器化验证

- [ ] 所有容器运行正常
- [ ] 容器网络连通
- [ ] 健康检查通过
- [ ] Docker Compose配置正确

### 文档验证

- [ ] 架构设计文档完整
- [ ] API接口文档完整
- [ ] 部署运维文档完整
- [ ] 故障处理手册完整

---

## 问题排查

### 问题1: 服务启动失败

**排查步骤**:
1. 查看容器日志: `docker logs <container_name>`
2. 检查端口占用: `netstat -tlnp | grep <port>`
3. 验证配置文件: `docker exec <container> cat /app/application.yml`
4. 检查依赖服务: `docker-compose ps`

### 问题2: 性能不达标

**排查步骤**:
1. 查看系统资源: `docker stats`
2. 检查慢查询: `docker logs mysql-master | grep slow`
3. 查看缓存命中率: `docker exec redis redis-cli info stats`
4. 分析链路追踪: 访问SkyWalking

### 问题3: 监控数据缺失

**排查步骤**:
1. 检查Prometheus配置: `docker exec prometheus cat /etc/prometheus/prometheus.yml`
2. 验证actuator端点: `curl http://localhost:8081/actuator/prometheus`
3. 查看Prometheus日志: `docker logs prometheus`
4. 测试数据源连接: Grafana → Configuration → Data Sources

---

## 自动化验证脚本

### 运行完整验证

```bash
# 赋予执行权限
chmod +x scripts/final-verification.sh

# 执行验证
./scripts/final-verification.sh
```

### 运行性能测试

```bash
# 赋予执行权限
chmod +x scripts/performance-test.sh

# 执行性能测试
./scripts/performance-test.sh
```

---

## 验证报告

验证完成后，系统会自动生成以下报告：

1. **验证报告**: `verification-report-<timestamp>.txt`
   - 包含所有验证项的结果
   - 服务状态汇总
   - 问题和建议

2. **性能报告**: `performance-report-<timestamp>.txt`
   - 性能测试结果
   - 资源使用情况
   - 性能基准对比

---

## 验收标准

### 必须满足（P0）

1. ✅ 所有服务正常启动和运行
2. ✅ 服务注册和发现正常
3. ✅ 数据库、缓存、消息队列连接正常
4. ✅ 网关路由正常工作
5. ✅ 基本功能测试通过

### 应该满足（P1）

1. ✅ 性能达到基准要求
2. ✅ 监控系统正常运行
3. ✅ 容器化部署成功
4. ✅ 文档完整

### 可以满足（P2）

1. ✅ 高级功能测试通过
2. ✅ 压力测试通过
3. ✅ 安全测试通过

---

## 下一步

验证通过后，可以进行：

1. **生产部署准备**
   - 准备生产环境配置
   - 制定部署计划
   - 准备回滚方案

2. **团队培训**
   - 新架构培训
   - 运维培训
   - 故障处理培训

3. **持续优化**
   - 性能优化
   - 监控优化
   - 文档完善

---

## 联系支持

如有问题，请联系：

- **技术支持**: support@restaurant.example.com
- **紧急热线**: +86 138-0013-8000
- **文档中心**: https://docs.restaurant.example.com

