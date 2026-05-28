# Grafana 仪表板配置

本目录包含七云菜馆微服务系统的Grafana监控仪表板配置文件。

## 仪表板列表

### 1. JVM 详细监控 (jvm-dashboard.json)
监控Java虚拟机的详细运行状态：
- 堆内存和非堆内存使用情况
- GC暂停时间和次数
- 线程状态（包括Java 21虚拟线程）
- 类加载统计
- 缓冲池使用情况

**适用场景**: JVM性能调优、内存泄漏排查、GC问题诊断

### 2. Spring Boot 监控 (spring-boot-dashboard.json)
监控Spring Boot应用的核心指标：
- JVM内存使用
- GC次数
- HTTP请求QPS
- HTTP请求响应时间（P95）
- 线程数
- CPU使用率

**适用场景**: 应用性能监控、容量规划、性能瓶颈分析

### 3. 业务指标监控 (business-metrics-dashboard.json)
监控订单服务的业务指标：
- 订单创建/支付/取消总数
- 订单队列长度
- 订单创建和支付速率
- 订单处理耗时（P50/P95/P99）
- 订单转化率
- 订单状态分布

**适用场景**: 业务运营监控、用户行为分析、业务异常告警

## 导入方法

### 方法一：通过Grafana UI导入

1. 登录Grafana（默认地址：http://localhost:3000）
   - 默认用户名：admin
   - 默认密码：admin123

2. 点击左侧菜单 "+" → "Import"

3. 选择 "Upload JSON file" 或直接粘贴JSON内容

4. 选择数据源为 "Prometheus"

5. 点击 "Import" 完成导入

### 方法二：通过Docker Compose自动导入

在docker-compose-full.yml中配置Grafana的provisioning：

```yaml
grafana:
  image: grafana/grafana:10.2.0
  volumes:
    - ./monitoring/grafana-dashboards:/etc/grafana/provisioning/dashboards
    - grafana-data:/var/lib/grafana
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin123
    - GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/etc/grafana/provisioning/dashboards/spring-boot-dashboard.json
```

### 方法三：使用Grafana API导入

```bash
# 设置Grafana地址和认证信息
GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASSWORD="admin123"

# 导入JVM仪表板
curl -X POST \
  -H "Content-Type: application/json" \
  -u ${GRAFANA_USER}:${GRAFANA_PASSWORD} \
  -d @jvm-dashboard.json \
  ${GRAFANA_URL}/api/dashboards/db

# 导入Spring Boot仪表板
curl -X POST \
  -H "Content-Type: application/json" \
  -u ${GRAFANA_USER}:${GRAFANA_PASSWORD} \
  -d @spring-boot-dashboard.json \
  ${GRAFANA_URL}/api/dashboards/db

# 导入业务指标仪表板
curl -X POST \
  -H "Content-Type: application/json" \
  -u ${GRAFANA_USER}:${GRAFANA_PASSWORD} \
  -d @business-metrics-dashboard.json \
  ${GRAFANA_URL}/api/dashboards/db
```

## 配置Prometheus数据源

在Grafana中添加Prometheus数据源：

1. 点击左侧菜单 "Configuration" → "Data Sources"

2. 点击 "Add data source"

3. 选择 "Prometheus"

4. 配置：
   - Name: Prometheus
   - URL: http://prometheus:9090
   - Access: Server (default)

5. 点击 "Save & Test"

## 自定义仪表板

### 常用PromQL查询示例

**订单相关指标：**
```promql
# 订单创建速率（每秒）
rate(order_create_total[1m])

# 订单处理P95耗时
histogram_quantile(0.95, rate(order_process_duration_seconds_bucket[5m]))

# 订单支付转化率
(order_paid_total / order_create_total) * 100
```

**JVM指标：**
```promql
# 堆内存使用率
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# GC暂停时间（毫秒）
rate(jvm_gc_pause_seconds_sum[5m]) * 1000

# 线程数
jvm_threads_live_threads
```

**HTTP指标：**
```promql
# HTTP请求QPS
rate(http_server_requests_seconds_count[1m])

# HTTP请求P99响应时间
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))

# HTTP错误率
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m])
```

## 告警配置

可以在Grafana中为关键指标配置告警规则：

1. 编辑面板 → Alert标签
2. 设置告警条件（如：订单队列长度 > 1000）
3. 配置通知渠道（邮件、钉钉、企业微信等）

### 推荐告警规则

- **订单队列积压**: order_queue_size > 1000
- **订单处理耗时过长**: P95 > 5秒
- **JVM堆内存使用率过高**: > 85%
- **GC频率过高**: > 10次/分钟
- **HTTP错误率过高**: > 1%
- **服务不可用**: up == 0

## 性能优化建议

根据仪表板数据进行性能优化：

1. **JVM调优**: 根据堆内存使用情况调整-Xms和-Xmx参数
2. **GC优化**: 如果GC暂停时间过长，考虑使用ZGC或Shenandoah GC
3. **线程池优化**: 根据线程数趋势调整线程池大小
4. **缓存优化**: 如果订单查询QPS高，增加缓存
5. **数据库优化**: 如果订单处理耗时长，检查数据库查询性能

## 故障排查

使用仪表板快速定位问题：

1. **服务响应慢**: 查看HTTP响应时间和订单处理耗时
2. **内存泄漏**: 查看堆内存使用趋势，是否持续增长
3. **CPU飙高**: 查看CPU使用率和线程数
4. **订单积压**: 查看订单队列长度和处理速率
5. **GC问题**: 查看GC次数和暂停时间

## 参考资料

- [Grafana官方文档](https://grafana.com/docs/)
- [Prometheus查询语法](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Spring Boot Actuator指标](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics)
- [Micrometer文档](https://micrometer.io/docs)
