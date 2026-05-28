# 部署运维文档

## 文档说明

本文档描述七云菜馆技术栈升级项目的部署和运维指南。

**版本**: 2.0.0  
**更新日期**: 2026-02-09

---

## 目录

1. [环境要求](#环境要求)
2. [快速开始](#快速开始)
3. [详细部署](#详细部署)
4. [配置说明](#配置说明)
5. [监控运维](#监控运维)
6. [故障处理](#故障处理)
7. [性能调优](#性能调优)
8. [备份恢复](#备份恢复)

---

## 环境要求

### 硬件要求

**开发环境**:
- CPU: 4核
- 内存: 8GB
- 磁盘: 50GB

**测试环境**:
- CPU: 8核
- 内存: 16GB
- 磁盘: 100GB

**生产环境**:
- CPU: 16核+
- 内存: 32GB+
- 磁盘: 500GB+
- 网络: 千兆网卡

### 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Java | 21 LTS | 运行环境 |
| Maven | 3.9+ | 构建工具 |
| Docker | 24.x | 容器运行时 |
| Docker Compose | 2.x | 容器编排 |
| Git | 2.x | 版本控制 |

### 操作系统

- Linux (推荐 Ubuntu 22.04 LTS 或 CentOS 8+)
- macOS 12+
- Windows 10/11 (需要WSL2)

---

## 快速开始

### 1. 克隆代码

```bash
git clone https://github.com/your-org/restaurant-system.git
cd restaurant-system
```

### 2. 一键启动（Docker Compose）

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 验证部署

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health

# 访问API文档
open http://localhost:8080/doc.html

# 访问Grafana监控
open http://localhost:3000
```

---

## 详细部署

### 方式一：Docker Compose部署（推荐）

#### 1. 准备配置文件

```bash
# 复制环境配置
cp .env.example .env

# 编辑配置
vim .env
```

**.env配置示例**:
```properties
# MySQL配置
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=restaurant_db
MYSQL_USER=restaurant
MYSQL_PASSWORD=restaurant123

# Redis配置
REDIS_PASSWORD=redis123

# RabbitMQ配置
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=admin123

# Nacos配置
NACOS_AUTH_ENABLE=true
NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789

# 应用配置
SPRING_PROFILES_ACTIVE=prod
```

#### 2. 启动基础设施

```bash
# 启动MySQL、Redis、RabbitMQ、Nacos
docker-compose up -d mysql redis rabbitmq nacos

# 等待服务就绪
sleep 30

# 验证服务
docker-compose ps
```

#### 3. 初始化数据库

```bash
# 执行SQL脚本
docker exec -i restaurant-mysql-master mysql -uroot -proot123 restaurant_db < sql/init.sql
```

#### 4. 启动应用服务

```bash
# 启动所有应用服务
docker-compose up -d gateway order-service dish-service member-service admin-service

# 查看启动日志
docker-compose logs -f order-service
```

#### 5. 启动监控服务

```bash
# 启动Prometheus和Grafana
docker-compose up -d prometheus grafana

# 访问Grafana
open http://localhost:3000
# 默认账号: admin/admin123
```

### 方式二：手动部署

#### 1. 安装Java 21

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk

# 验证安装
java -version
```

#### 2. 安装MySQL

```bash
# Ubuntu/Debian
sudo apt install mysql-server-8.0

# 启动MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 创建数据库和用户
mysql -uroot -p <<EOF
CREATE DATABASE restaurant_db;
CREATE USER 'restaurant'@'%' IDENTIFIED BY 'restaurant123';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurant'@'%';
FLUSH PRIVILEGES;
EOF

# 导入数据
mysql -urestaurant -prestaurant123 restaurant_db < sql/init.sql
```

#### 3. 安装Redis

```bash
# Ubuntu/Debian
sudo apt install redis-server

# 配置密码
sudo vim /etc/redis/redis.conf
# 添加: requirepass redis123

# 重启Redis
sudo systemctl restart redis
sudo systemctl enable redis
```

#### 4. 安装RabbitMQ

```bash
# Ubuntu/Debian
sudo apt install rabbitmq-server

# 启动RabbitMQ
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server

# 启用管理插件
sudo rabbitmq-plugins enable rabbitmq_management

# 创建用户
sudo rabbitmqctl add_user admin admin123
sudo rabbitmqctl set_user_tags admin administrator
sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

#### 5. 安装Nacos

```bash
# 下载Nacos
wget https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.tar.gz
tar -xzf nacos-server-2.3.0.tar.gz
cd nacos

# 配置MySQL存储
vim conf/application.properties
# 添加MySQL配置

# 启动Nacos（单机模式）
sh bin/startup.sh -m standalone

# 访问控制台
open http://localhost:8848/nacos
# 默认账号: nacos/nacos
```

#### 6. 构建应用

```bash
# 构建所有服务
mvn clean package -DskipTests

# 或单独构建
cd restaurant-order
mvn clean package -DskipTests
```

#### 7. 启动应用

```bash
# 启动Gateway
cd restaurant-gateway
nohup java -jar target/restaurant-gateway-2.0.0.jar > gateway.log 2>&1 &

# 启动Order Service
cd restaurant-order
nohup java -jar target/restaurant-order-2.0.0.jar > order.log 2>&1 &

# 启动Dish Service
cd restaurant-dish
nohup java -jar target/restaurant-dish-2.0.0.jar > dish.log 2>&1 &

# 启动Member Service
cd restaurant-member
nohup java -jar target/restaurant-member-2.0.0.jar > member.log 2>&1 &

# 启动Admin Service
cd restaurant-admin-service
nohup java -jar target/restaurant-admin-service-2.0.0.jar > admin.log 2>&1 &
```

---

## 配置说明

### 应用配置

**application.yml**:
```yaml
spring:
  application:
    name: restaurant-order-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        file-extension: yaml
  
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/${MYSQL_DATABASE:restaurant_db}
    username: ${MYSQL_USER:restaurant}
    password: ${MYSQL_PASSWORD:restaurant123}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:redis123}
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
```

### JVM参数

**生产环境推荐配置**:
```bash
java -jar \
  -Xms2g \
  -Xmx2g \
  -XX:+UseZGC \
  -XX:+UseStringDeduplication \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/heapdump.hprof \
  -Dspring.profiles.active=prod \
  app.jar
```

### Docker配置

**Dockerfile优化**:
```dockerfile
FROM eclipse-temurin:21-jre-alpine

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建应用目录
WORKDIR /app

# 复制jar包
COPY target/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+UseStringDeduplication", \
  "-Xms512m", \
  "-Xmx1024m", \
  "-jar", \
  "app.jar"]
```

---

## 监控运维

### 服务监控

#### 1. Prometheus监控

**访问地址**: http://localhost:9090

**常用查询**:
```promql
# CPU使用率
rate(process_cpu_usage[5m])

# 内存使用
jvm_memory_used_bytes

# QPS
rate(http_server_requests_seconds_count[1m])

# 响应时间
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# 错误率
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

#### 2. Grafana仪表板

**访问地址**: http://localhost:3000

**默认账号**: admin/admin123

**导入仪表板**:
1. 登录Grafana
2. 点击 "+" → "Import"
3. 上传 `monitoring/grafana-dashboards/*.json`

**仪表板列表**:
- JVM监控仪表板
- Spring Boot监控仪表板
- 业务指标仪表板

#### 3. SkyWalking链路追踪

**部署SkyWalking**:
```bash
# 启动SkyWalking
docker-compose -f monitoring/skywalking/docker-compose-skywalking.yml up -d

# 访问UI
open http://localhost:8080
```

**配置Agent**:
```bash
java -javaagent:/path/to/skywalking-agent.jar \
  -Dskywalking.agent.service_name=restaurant-order-service \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -jar app.jar
```

### 日志管理

#### 1. 日志配置

**logback-spring.xml**:
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

#### 2. 查看日志

```bash
# Docker Compose日志
docker-compose logs -f order-service

# 容器日志
docker logs -f restaurant-order

# 主机日志
tail -f /var/log/app.log
```

### 告警配置

**Prometheus告警规则**:
```yaml
groups:
  - name: restaurant-alerts
    rules:
      - alert: HighCPUUsage
        expr: rate(process_cpu_usage[5m]) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "{{ $labels.instance }} CPU使用率超过80%"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "{{ $labels.instance }} 内存使用率超过85%"
      
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "错误率过高"
          description: "{{ $labels.instance }} 错误率超过5%"
```

---

## 故障处理

### 常见问题

#### 1. 服务无法启动

**症状**: 服务启动失败或启动后立即退出

**排查步骤**:
```bash
# 查看日志
docker logs restaurant-order

# 检查配置
docker exec restaurant-order cat /app/application.yml

# 检查端口占用
netstat -tlnp | grep 8081

# 检查依赖服务
docker-compose ps
```

**解决方案**:
- 检查配置文件是否正确
- 确认依赖服务（MySQL、Redis、Nacos）已启动
- 检查端口是否被占用
- 查看详细错误日志

#### 2. 服务注册失败

**症状**: 服务无法注册到Nacos

**排查步骤**:
```bash
# 检查Nacos状态
curl http://localhost:8848/nacos/v1/console/health/readiness

# 查看服务列表
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=restaurant-order-service

# 检查网络连接
docker exec restaurant-order ping nacos
```

**解决方案**:
- 确认Nacos服务正常运行
- 检查网络连接
- 验证Nacos配置（地址、命名空间）
- 检查防火墙规则

#### 3. 数据库连接失败

**症状**: 应用无法连接数据库

**排查步骤**:
```bash
# 测试数据库连接
docker exec restaurant-order mysql -h mysql-master -urestaurant -prestaurant123 -e "SELECT 1"

# 检查数据库状态
docker exec mysql-master mysqladmin -uroot -proot123 status

# 查看数据库日志
docker logs mysql-master
```

**解决方案**:
- 确认数据库服务正常
- 检查用户名密码
- 验证数据库权限
- 检查网络连接

#### 4. Redis连接失败

**症状**: 应用无法连接Redis

**排查步骤**:
```bash
# 测试Redis连接
docker exec restaurant-order redis-cli -h redis -a redis123 ping

# 检查Redis状态
docker exec redis redis-cli -a redis123 info
```

**解决方案**:
- 确认Redis服务正常
- 检查密码配置
- 验证网络连接

#### 5. 消息队列异常

**症状**: 消息发送或消费失败

**排查步骤**:
```bash
# 检查RabbitMQ状态
docker exec rabbitmq rabbitmqctl status

# 查看队列信息
docker exec rabbitmq rabbitmqctl list_queues

# 查看连接
docker exec rabbitmq rabbitmqctl list_connections
```

**解决方案**:
- 确认RabbitMQ服务正常
- 检查队列配置
- 验证消息格式
- 查看死信队列

### 性能问题

#### 1. 响应时间慢

**排查步骤**:
1. 查看SkyWalking链路追踪
2. 分析慢查询日志
3. 检查缓存命中率
4. 查看系统资源使用

**优化方案**:
- 优化SQL查询
- 增加缓存
- 调整连接池大小
- 扩容服务实例

#### 2. 内存溢出

**排查步骤**:
```bash
# 生成堆转储
jmap -dump:format=b,file=heapdump.hprof <pid>

# 分析堆转储
jhat heapdump.hprof

# 或使用MAT工具分析
```

**解决方案**:
- 增加堆内存
- 修复内存泄漏
- 优化对象创建
- 调整GC参数

---

## 性能调优

### JVM调优

**ZGC配置**:
```bash
-XX:+UseZGC
-XX:+UseStringDeduplication
-XX:MaxGCPauseMillis=200
-XX:ConcGCThreads=4
-XX:ParallelGCThreads=8
```

### 数据库调优

**MySQL配置**:
```ini
[mysqld]
# 连接数
max_connections = 1000

# 缓冲池
innodb_buffer_pool_size = 8G

# 日志
innodb_log_file_size = 512M

# 查询缓存
query_cache_size = 256M
```

### Redis调优

**Redis配置**:
```ini
# 最大内存
maxmemory 4gb

# 淘汰策略
maxmemory-policy allkeys-lru

# 持久化
save 900 1
save 300 10
save 60 10000
```

---

## 备份恢复

### 数据库备份

**自动备份脚本**:
```bash
#!/bin/bash
# backup-mysql.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/backup/mysql
MYSQL_USER=root
MYSQL_PASSWORD=root123
DATABASE=restaurant_db

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD $DATABASE | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete

echo "Backup completed: backup_$DATE.sql.gz"
```

**定时任务**:
```bash
# 添加到crontab
0 2 * * * /path/to/backup-mysql.sh
```

### 数据恢复

```bash
# 解压备份文件
gunzip backup_20260209_020000.sql.gz

# 恢复数据库
mysql -uroot -proot123 restaurant_db < backup_20260209_020000.sql
```

---

## 升级指南

### 滚动升级

```bash
# 1. 构建新版本镜像
docker build -t restaurant-order:v2.1.0 .

# 2. 更新docker-compose.yml
vim docker-compose.yml
# 修改镜像版本

# 3. 逐个升级服务
docker-compose up -d --no-deps order-service

# 4. 验证服务
curl http://localhost:8081/actuator/health

# 5. 升级其他服务
docker-compose up -d --no-deps dish-service
docker-compose up -d --no-deps member-service
```

### 回滚

```bash
# 回滚到上一个版本
docker-compose down order-service
docker-compose up -d order-service
```

---

## 安全加固

### 1. 网络安全

- 使用防火墙限制端口访问
- 配置SSL/TLS加密
- 使用VPN或专线

### 2. 应用安全

- 定期更新依赖
- 使用强密码
- 启用认证授权
- 限制API访问频率

### 3. 数据安全

- 敏感数据加密
- 定期备份
- 访问审计
- 数据脱敏

---

## 附录

### 常用命令

```bash
# Docker Compose
docker-compose up -d          # 启动所有服务
docker-compose down           # 停止所有服务
docker-compose ps             # 查看服务状态
docker-compose logs -f        # 查看日志
docker-compose restart        # 重启服务

# Docker
docker ps                     # 查看运行中的容器
docker logs -f <container>    # 查看容器日志
docker exec -it <container> bash  # 进入容器
docker stats                  # 查看资源使用

# 系统
top                          # 查看进程
htop                         # 增强版top
netstat -tlnp                # 查看端口
df -h                        # 查看磁盘
free -h                      # 查看内存
```

### 联系方式

- 技术支持: support@restaurant.example.com
- 紧急联系: +86 138-0013-8000
- 文档地址: https://docs.restaurant.example.com
