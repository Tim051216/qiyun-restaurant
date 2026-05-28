# 故障处理手册

## 文档说明

本文档提供七云菜馆技术栈升级项目的常见故障诊断和处理方案。

**版本**: 2.0.0  
**更新日期**: 2026-02-09

---

## 目录

1. [服务启动问题](#服务启动问题)
2. [服务注册问题](#服务注册问题)
3. [数据库问题](#数据库问题)
4. [缓存问题](#缓存问题)
5. [消息队列问题](#消息队列问题)
6. [网关问题](#网关问题)
7. [性能问题](#性能问题)
8. [监控告警问题](#监控告警问题)
9. [容器问题](#容器问题)
10. [网络问题](#网络问题)

---

## 服务启动问题

### 问题1: 服务启动失败，提示端口被占用

**症状**:
```
Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
Caused by: java.net.BindException: Address already in use
```

**原因**: 端口已被其他进程占用

**诊断步骤**:
```bash
# 查看端口占用情况
netstat -tlnp | grep 8081

# 或使用lsof
lsof -i :8081
```

**解决方案**:
```bash
# 方案1: 杀死占用端口的进程
kill -9 <PID>

# 方案2: 修改服务端口
# 编辑application.yml
server:
  port: 8091  # 使用新端口

# 方案3: 停止冲突的容器
docker stop <container_name>
```

**预防措施**:
- 使用docker-compose统一管理端口
- 在配置文件中明确指定端口
- 使用环境变量配置端口

---

### 问题2: 服务启动后立即退出

**症状**:
```bash
docker ps  # 看不到服务容器
docker ps -a  # 容器状态为Exited
```

**原因**: 
- 配置错误
- 依赖服务未就绪
- JVM参数不当
- 应用异常

**诊断步骤**:
```bash
# 查看容器日志
docker logs restaurant-order

# 查看最后100行日志
docker logs --tail 100 restaurant-order

# 实时查看日志
docker logs -f restaurant-order
```

**解决方案**:

1. **配置问题**:
```bash
# 检查配置文件
docker exec restaurant-order cat /app/application.yml

# 验证环境变量
docker exec restaurant-order env | grep SPRING
```

2. **依赖服务问题**:
```bash
# 检查依赖服务状态
docker-compose ps

# 等待依赖服务就绪
docker-compose up -d mysql redis nacos
sleep 30
docker-compose up -d order-service
```

3. **JVM参数问题**:
```bash
# 调整内存参数
docker run -e JAVA_OPTS="-Xms512m -Xmx1024m" restaurant-order
```

---

### 问题3: 服务启动慢，超时

**症状**: 服务启动时间超过2分钟

**原因**:
- 数据库连接池初始化慢
- 缓存预热耗时
- Nacos注册超时
- 资源不足

**诊断步骤**:
```bash
# 查看启动日志
docker logs restaurant-order | grep "Started"

# 检查资源使用
docker stats restaurant-order

# 查看系统资源
top
free -h
df -h
```

**解决方案**:

1. **优化数据库连接池**:
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
```

2. **异步缓存预热**:
```java
@PostConstruct
public void warmUp() {
    CompletableFuture.runAsync(() -> {
        // 异步预热缓存
    });
}
```

3. **增加健康检查时间**:
```yaml
# docker-compose.yml
healthcheck:
  start-period: 60s  # 增加启动等待时间
```

---

## 服务注册问题

### 问题4: 服务无法注册到Nacos

**症状**:
- Nacos控制台看不到服务
- 服务间调用失败
- 日志显示注册失败

**原因**:
- Nacos服务未启动
- 网络不通
- 配置错误
- 命名空间错误

**诊断步骤**:
```bash
# 检查Nacos状态
curl http://localhost:8848/nacos/v1/console/health/readiness

# 测试网络连接
docker exec restaurant-order ping nacos

# 查看服务列表
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-order-service"

# 检查配置
docker exec restaurant-order cat /app/application.yml | grep nacos
```

**解决方案**:

1. **启动Nacos**:
```bash
docker-compose up -d nacos
# 等待Nacos就绪
sleep 20
```

2. **修复网络连接**:
```bash
# 确保服务在同一网络
docker network ls
docker network inspect restaurant-network
```

3. **修正配置**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos:8848  # 使用容器名
        namespace: dev           # 确认命名空间
        group: RESTAURANT_GROUP
```

---

### 问题5: 服务注册成功但无法被发现

**症状**: 
- Nacos控制台能看到服务
- 但Feign调用失败
- 负载均衡不生效

**原因**:
- 服务实例不健康
- 负载均衡配置错误
- 网络隔离

**诊断步骤**:
```bash
# 查看服务实例详情
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-dish-service&healthyOnly=true"

# 测试服务间连接
docker exec restaurant-order curl http://restaurant-dish:8082/actuator/health

# 查看Feign日志
docker logs restaurant-order | grep Feign
```

**解决方案**:

1. **确保健康检查通过**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  health:
    defaults:
      enabled: true
```

2. **配置负载均衡**:
```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      cache:
        enabled: true
```

---

## 数据库问题

### 问题6: 数据库连接失败

**症状**:
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**原因**:
- MySQL服务未启动
- 连接参数错误
- 网络不通
- 防火墙阻止

**诊断步骤**:
```bash
# 检查MySQL状态
docker ps | grep mysql

# 测试连接
docker exec restaurant-order mysql -h mysql-master -urestaurant -prestaurant123 -e "SELECT 1"

# 查看MySQL日志
docker logs mysql-master

# 测试网络
docker exec restaurant-order ping mysql-master
```

**解决方案**:

1. **启动MySQL**:
```bash
docker-compose up -d mysql-master
```

2. **修正连接参数**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql-master:3306/restaurant_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: restaurant
    password: restaurant123
```

3. **检查用户权限**:
```sql
-- 进入MySQL
docker exec -it mysql-master mysql -uroot -proot123

-- 查看用户权限
SHOW GRANTS FOR 'restaurant'@'%';

-- 授予权限
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurant'@'%';
FLUSH PRIVILEGES;
```

---

### 问题7: 慢查询导致性能下降

**症状**:
- 接口响应慢
- 数据库CPU高
- 大量慢查询日志

**诊断步骤**:
```bash
# 查看慢查询日志
docker exec mysql-master tail -f /var/log/mysql/slow-query.log

# 查看当前执行的查询
docker exec -it mysql-master mysql -uroot -proot123 -e "SHOW PROCESSLIST"

# 分析查询
docker exec -it mysql-master mysql -uroot -proot123 -e "EXPLAIN SELECT * FROM t_order WHERE user_id = 1001"
```

**解决方案**:

1. **添加索引**:
```sql
-- 分析缺失的索引
ALTER TABLE t_order ADD INDEX idx_user_id (user_id);
ALTER TABLE t_order ADD INDEX idx_create_time (create_time);
```

2. **优化查询**:
```java
// 避免SELECT *
// 使用分页查询
// 避免N+1查询
```

3. **启用查询缓存**:
```ini
[mysqld]
query_cache_type = 1
query_cache_size = 256M
```

---

### 问题8: 主从同步延迟

**症状**:
- 读取到旧数据
- 主从数据不一致

**诊断步骤**:
```bash
# 检查主从状态
docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G"

# 查看延迟时间
docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G" | grep Seconds_Behind_Master
```

**解决方案**:

1. **强制读主库**:
```java
@Transactional(readOnly = false)  // 强制使用主库
public Order getOrderById(Long id) {
    return orderMapper.selectById(id);
}
```

2. **优化主从配置**:
```ini
# 主库配置
[mysqld]
sync_binlog = 1
innodb_flush_log_at_trx_commit = 1

# 从库配置
[mysqld]
slave_parallel_workers = 4
slave_parallel_type = LOGICAL_CLOCK
```

---

## 缓存问题

### 问题9: Redis连接失败

**症状**:
```
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

**诊断步骤**:
```bash
# 检查Redis状态
docker ps | grep redis

# 测试连接
docker exec restaurant-order redis-cli -h redis -a redis123 ping

# 查看Redis日志
docker logs redis
```

**解决方案**:

1. **启动Redis**:
```bash
docker-compose up -d redis
```

2. **修正配置**:
```yaml
spring:
  redis:
    host: redis
    port: 6379
    password: redis123
    timeout: 3000
```

---

### 问题10: 缓存穿透

**症状**:
- 大量请求直达数据库
- 数据库压力大
- 查询不存在的数据

**诊断步骤**:
```bash
# 查看Redis命中率
docker exec redis redis-cli -a redis123 info stats | grep keyspace

# 监控数据库查询
docker exec mysql-master mysqladmin -uroot -proot123 processlist
```

**解决方案**:

1. **使用布隆过滤器**:
```java
@Autowired
private BloomFilter<Long> bloomFilter;

public Dish getDishById(Long id) {
    if (!bloomFilter.mightContain(id)) {
        return null;  // 直接返回，不查数据库
    }
    // 继续查询缓存和数据库
}
```

2. **缓存空值**:
```java
if (dish == null) {
    // 缓存空值，设置较短过期时间
    redisTemplate.opsForValue().set(key, "NULL", 60, TimeUnit.SECONDS);
}
```

---

### 问题11: 缓存雪崩

**症状**:
- 大量缓存同时失效
- 数据库瞬间压力激增
- 系统响应变慢

**解决方案**:

1. **过期时间随机化**:
```java
long expire = 300 + ThreadLocalRandom.current().nextInt(60);
redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
```

2. **使用多级缓存**:
```java
// L1: Caffeine本地缓存
// L2: Redis分布式缓存
// L3: 数据库
```

3. **限流保护**:
```java
@SentinelResource(value = "getDish", blockHandler = "handleBlock")
public Dish getDishById(Long id) {
    // 业务逻辑
}
```

---

## 消息队列问题

### 问题12: 消息发送失败

**症状**:
- 消息未到达队列
- 发送超时
- 连接异常

**诊断步骤**:
```bash
# 检查RabbitMQ状态
docker exec rabbitmq rabbitmqctl status

# 查看连接
docker exec rabbitmq rabbitmqctl list_connections

# 查看队列
docker exec rabbitmq rabbitmqctl list_queues
```

**解决方案**:

1. **检查连接配置**:
```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: admin123
    connection-timeout: 15000
```

2. **启用发送确认**:
```java
rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
    if (!ack) {
        log.error("消息发送失败: {}", cause);
        // 重试或记录
    }
});
```

---

### 问题13: 消息堆积

**症状**:
- 队列消息数量持续增长
- 消费速度慢
- 内存占用高

**诊断步骤**:
```bash
# 查看队列详情
docker exec rabbitmq rabbitmqctl list_queues name messages consumers

# 访问管理界面
open http://localhost:15672
```

**解决方案**:

1. **增加消费者**:
```yaml
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 5
        max-concurrency: 10
```

2. **优化消费逻辑**:
```java
// 批量处理
// 异步处理
// 减少外部调用
```

3. **设置消息TTL**:
```java
args.put("x-message-ttl", 30000);  // 30秒过期
```

---

### 问题14: 消息重复消费

**症状**:
- 同一消息被处理多次
- 业务数据重复

**解决方案**:

1. **幂等性设计**:
```java
String idempotentKey = "order:consumed:" + messageId;
Boolean isConsumed = redisTemplate.opsForValue()
    .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);

if (Boolean.FALSE.equals(isConsumed)) {
    log.warn("消息已被消费，跳过");
    return;
}
```

2. **数据库唯一约束**:
```sql
ALTER TABLE t_order ADD UNIQUE KEY uk_order_no (order_no);
```

---

## 网关问题

### 问题15: 网关路由失败

**症状**:
- 404 Not Found
- 503 Service Unavailable
- 路由不生效

**诊断步骤**:
```bash
# 查看网关日志
docker logs gateway

# 测试路由
curl -v http://localhost:8080/api/order/list

# 检查服务注册
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-order-service"
```

**解决方案**:

1. **检查路由配置**:
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

2. **启用日志**:
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

---

### 问题16: 网关限流触发

**症状**: 返回429 Too Many Requests

**解决方案**:

1. **调整限流规则**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 200  # 增加限流阈值
                redis-rate-limiter.burstCapacity: 400
```

2. **使用令牌桶算法**:
```java
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
    );
}
```

---

## 性能问题

### 问题17: 接口响应慢

**诊断步骤**:

1. **查看链路追踪**:
```bash
# 访问SkyWalking
open http://localhost:8080
```

2. **分析慢查询**:
```bash
# 查看MySQL慢查询
docker exec mysql-master tail -f /var/log/mysql/slow-query.log
```

3. **检查缓存命中率**:
```bash
docker exec redis redis-cli -a redis123 info stats
```

**解决方案**:

1. **优化SQL**:
- 添加索引
- 避免全表扫描
- 使用分页查询

2. **增加缓存**:
- 使用多级缓存
- 缓存热点数据
- 设置合理过期时间

3. **异步处理**:
```java
@Async
public CompletableFuture<Result> processAsync() {
    // 异步处理
}
```

---

### 问题18: 内存溢出

**症状**:
```
java.lang.OutOfMemoryError: Java heap space
```

**诊断步骤**:
```bash
# 生成堆转储
docker exec restaurant-order jmap -dump:format=b,file=/tmp/heapdump.hprof <pid>

# 复制到主机
docker cp restaurant-order:/tmp/heapdump.hprof ./

# 使用MAT分析
```

**解决方案**:

1. **增加堆内存**:
```bash
JAVA_OPTS="-Xms2g -Xmx4g"
```

2. **修复内存泄漏**:
- 关闭资源
- 清理缓存
- 避免大对象

3. **优化GC**:
```bash
-XX:+UseZGC
-XX:MaxGCPauseMillis=200
```

---

## 监控告警问题

### 问题19: Prometheus无法采集指标

**诊断步骤**:
```bash
# 检查actuator端点
curl http://localhost:8081/actuator/prometheus

# 查看Prometheus配置
docker exec prometheus cat /etc/prometheus/prometheus.yml

# 查看Prometheus日志
docker logs prometheus
```

**解决方案**:

1. **暴露actuator端点**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

2. **修正Prometheus配置**:
```yaml
scrape_configs:
  - job_name: 'restaurant-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['order-service:8081']
```

---

### 问题20: Grafana无数据

**诊断步骤**:
```bash
# 检查数据源
# 登录Grafana → Configuration → Data Sources

# 测试Prometheus连接
curl http://prometheus:9090/api/v1/query?query=up
```

**解决方案**:

1. **配置数据源**:
- URL: http://prometheus:9090
- Access: Server

2. **导入仪表板**:
```bash
# 上传JSON文件
monitoring/grafana-dashboards/*.json
```

---

## 容器问题

### 问题21: 容器无法启动

**诊断步骤**:
```bash
# 查看容器状态
docker ps -a

# 查看容器日志
docker logs <container_name>

# 检查镜像
docker images

# 查看容器详情
docker inspect <container_name>
```

**解决方案**:

1. **重新构建镜像**:
```bash
docker-compose build --no-cache order-service
```

2. **清理资源**:
```bash
docker system prune -a
```

---

### 问题22: 容器间网络不通

**诊断步骤**:
```bash
# 查看网络
docker network ls
docker network inspect restaurant-network

# 测试连接
docker exec restaurant-order ping mysql-master
```

**解决方案**:

1. **确保在同一网络**:
```yaml
services:
  order-service:
    networks:
      - restaurant-network
```

2. **使用容器名访问**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql-master:3306/restaurant_db
```

---

## 网络问题

### 问题23: 服务间调用超时

**症状**:
```
feign.RetryableException: Read timed out
```

**解决方案**:

1. **增加超时时间**:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

2. **启用重试**:
```java
@Bean
public Retryer retryer() {
    return new Retryer.Default(100, 1000, 3);
}
```

---

## 应急处理流程

### 1. 服务宕机

```bash
# 1. 快速重启
docker-compose restart <service_name>

# 2. 查看日志
docker logs -f <service_name>

# 3. 如果无法恢复，回滚
docker-compose down <service_name>
docker pull <old_image>
docker-compose up -d <service_name>
```

### 2. 数据库故障

```bash
# 1. 切换到从库
# 修改配置，临时使用从库

# 2. 修复主库
# 重启MySQL
docker-compose restart mysql-master

# 3. 恢复主从同步
docker exec mysql-slave mysql -uroot -proot123 -e "START SLAVE"
```

### 3. 缓存故障

```bash
# 1. 重启Redis
docker-compose restart redis

# 2. 如果无法恢复，降级
# 临时关闭缓存，直接查数据库

# 3. 恢复后重新预热
curl http://localhost:8082/admin/cache/warmup
```

---

## 联系支持

如果以上方案无法解决问题，请联系技术支持：

- **技术支持邮箱**: support@restaurant.example.com
- **紧急热线**: +86 138-0013-8000
- **工单系统**: https://support.restaurant.example.com
- **文档中心**: https://docs.restaurant.example.com

---

## 附录

### 常用诊断命令

```bash
# 系统资源
top
htop
free -h
df -h
iostat

# 网络
netstat -tlnp
ss -tlnp
ping
telnet
curl

# Docker
docker ps
docker logs
docker stats
docker inspect
docker exec

# 数据库
mysql
mysqladmin
mysqldump

# Redis
redis-cli
redis-benchmark

# 日志
tail -f
grep
less
journalctl
```

### 日志位置

| 组件 | 日志位置 |
|------|----------|
| 应用服务 | /var/log/app.log |
| MySQL | /var/log/mysql/ |
| Redis | /var/log/redis/ |
| RabbitMQ | /var/log/rabbitmq/ |
| Nacos | /home/nacos/logs/ |
| Docker | /var/lib/docker/containers/ |

