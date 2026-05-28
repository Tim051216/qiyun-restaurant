# SkyWalking 链路追踪配置指南

本文档说明如何为七云菜馆微服务系统集成Apache SkyWalking链路追踪。

## 概述

SkyWalking是一个开源的APM（Application Performance Monitoring）系统，提供：
- 分布式链路追踪
- 服务性能指标监控
- 服务拓扑图
- 告警功能

## 架构组件

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Gateway   │────▶│    Order    │────▶│    Dish     │
│   Service   │     │   Service   │     │   Service   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       │ SkyWalking Agent  │                   │
       └───────────────────┴───────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  SkyWalking OAP │
                  │   (Collector)   │
                  └────────┬─────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  SkyWalking UI  │
                  │  (Dashboard)    │
                  └─────────────────┘
```

## 快速开始

### 1. 下载SkyWalking Agent

```bash
# 下载SkyWalking Agent（版本9.6.0）
cd restaurant-server/monitoring/skywalking
wget https://archive.apache.org/dist/skywalking/java-agent/9.6.0/apache-skywalking-java-agent-9.6.0.tgz

# 解压
tar -zxvf apache-skywalking-java-agent-9.6.0.tgz

# 目录结构
# skywalking-agent/
# ├── skywalking-agent.jar
# ├── config/
# │   └── agent.config
# ├── plugins/
# └── optional-plugins/
```

### 2. 配置Agent

编辑 `skywalking-agent/config/agent.config`：

```properties
# 服务名称（每个微服务不同）
agent.service_name=${SW_AGENT_NAME:restaurant-order-service}

# SkyWalking OAP服务器地址
collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:skywalking-oap:11800}

# 采样率（生产环境建议1000-5000，开发环境可以设置为1）
agent.sample_n_per_3_secs=${SW_AGENT_SAMPLE:1}

# 日志级别
logging.level=${SW_LOGGING_LEVEL:INFO}

# 日志输出目录
logging.dir=${SW_LOGGING_DIR:}

# 日志文件名
logging.file_name=${SW_LOGGING_FILE_NAME:skywalking-api.log}

# 最大日志文件大小
logging.max_file_size=${SW_LOGGING_MAX_FILE_SIZE:300 * 1024 * 1024}

# 最大历史日志文件数
logging.max_history_files=${SW_LOGGING_MAX_HISTORY_FILES:5}

# 忽略的后缀
agent.ignore_suffix=${SW_AGENT_IGNORE_SUFFIX:.jpg,.jpeg,.js,.css,.png,.bmp,.gif,.ico,.mp3,.mp4,.html,.svg}

# 是否收集HTTP参数
plugin.http.http_params_length_threshold=${SW_PLUGIN_HTTP_HTTP_PARAMS_LENGTH_THRESHOLD:2048}

# 是否收集SQL参数
plugin.jdbc.trace_sql_parameters=${SW_PLUGIN_JDBC_TRACE_SQL_PARAMETERS:true}

# SQL参数最大长度
plugin.jdbc.sql_parameters_max_length=${SW_PLUGIN_JDBC_SQL_PARAMETERS_MAX_LENGTH:512}
```

### 3. 启动SkyWalking OAP和UI

使用Docker Compose启动：

```yaml
# docker-compose-skywalking.yml
version: '3.8'

services:
  # Elasticsearch（SkyWalking存储）
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: skywalking-elasticsearch
    environment:
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - skywalking-network

  # SkyWalking OAP Server
  skywalking-oap:
    image: apache/skywalking-oap-server:9.6.0
    container_name: skywalking-oap
    depends_on:
      - elasticsearch
    environment:
      SW_STORAGE: elasticsearch
      SW_STORAGE_ES_CLUSTER_NODES: elasticsearch:9200
      SW_HEALTH_CHECKER: default
      SW_TELEMETRY: prometheus
      JAVA_OPTS: "-Xms512m -Xmx512m"
    ports:
      - "11800:11800"  # gRPC端口
      - "12800:12800"  # HTTP端口
    networks:
      - skywalking-network
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:12800/internal/l7check || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  # SkyWalking UI
  skywalking-ui:
    image: apache/skywalking-ui:9.6.0
    container_name: skywalking-ui
    depends_on:
      - skywalking-oap
    environment:
      SW_OAP_ADDRESS: http://skywalking-oap:12800
      SW_ZIPKIN_ADDRESS: http://skywalking-oap:9412
    ports:
      - "8090:8080"
    networks:
      - skywalking-network

volumes:
  elasticsearch-data:

networks:
  skywalking-network:
    driver: bridge
```

启动命令：

```bash
docker-compose -f docker-compose-skywalking.yml up -d
```

访问SkyWalking UI：http://localhost:8090

### 4. 配置微服务使用Agent

#### 方法一：修改Dockerfile

在每个微服务的Dockerfile中添加Agent：

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 复制SkyWalking Agent
COPY --from=apache/skywalking-java-agent:9.6.0-java17 /skywalking/agent /skywalking/agent

# 复制应用jar
COPY target/*.jar app.jar

# 配置环境变量
ENV SW_AGENT_NAME=restaurant-order-service
ENV SW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800
ENV SW_AGENT_SAMPLE=1

# 启动应用时加载Agent
ENTRYPOINT ["java", \
  "-javaagent:/skywalking/agent/skywalking-agent.jar", \
  "-XX:+UseZGC", \
  "-Xms512m", \
  "-Xmx1024m", \
  "-jar", \
  "app.jar"]
```

#### 方法二：使用启动脚本

创建启动脚本 `start-with-skywalking.sh`：

```bash
#!/bin/bash

# SkyWalking Agent路径
AGENT_PATH="/path/to/skywalking-agent/skywalking-agent.jar"

# 服务名称
SERVICE_NAME="restaurant-order-service"

# OAP服务器地址
OAP_ADDRESS="localhost:11800"

# 启动应用
java -javaagent:${AGENT_PATH} \
  -Dskywalking.agent.service_name=${SERVICE_NAME} \
  -Dskywalking.collector.backend_service=${OAP_ADDRESS} \
  -Dskywalking.agent.sample_n_per_3_secs=1 \
  -XX:+UseZGC \
  -Xms512m \
  -Xmx1024m \
  -jar app.jar
```

#### 方法三：在docker-compose中配置

```yaml
order-service:
  build:
    context: ./restaurant-order
  environment:
    - JAVA_TOOL_OPTIONS=-javaagent:/skywalking/agent/skywalking-agent.jar
    - SW_AGENT_NAME=restaurant-order-service
    - SW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800
    - SW_AGENT_SAMPLE=1
  volumes:
    - ./monitoring/skywalking/skywalking-agent:/skywalking/agent
  networks:
    - restaurant-network
    - skywalking-network
```

## 自定义Span

在代码中添加自定义Span来追踪关键业务逻辑。

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>9.0.0</version>
</dependency>
```

### 2. 使用@Trace注解

```java
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.Tag;

@Service
public class OrderService {
    
    /**
     * 使用@Trace注解自动创建Span
     * 使用@Tag注解添加标签
     */
    @Trace
    @Tag(key = "order.id", value = "arg[0]")
    @Tag(key = "user.id", value = "arg[1]")
    public Order createOrder(Long orderId, Long userId, OrderDTO dto) {
        // 业务逻辑
        return order;
    }
}
```

### 3. 手动创建Span

```java
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;

@Service
public class OrderService {
    
    public void processOrder(Order order) {
        // 创建自定义Span
        ActiveSpan span = ActiveSpan.tag("order.id", order.getId().toString());
        span.tag("order.amount", order.getAmount().toString());
        span.tag("business.type", "order-processing");
        
        try {
            // 业务逻辑
            validateOrder(order);
            calculateAmount(order);
            saveOrder(order);
            
            // 添加日志到Span
            ActiveSpan.info("Order processed successfully");
        } catch (Exception e) {
            // 记录异常
            ActiveSpan.error(e);
            throw e;
        }
    }
    
    /**
     * 获取TraceId用于日志关联
     */
    public void logWithTraceId() {
        String traceId = TraceContext.traceId();
        log.info("Processing order, traceId: {}", traceId);
    }
}
```

### 4. 跨线程传递上下文

```java
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;

@Service
public class AsyncOrderService {
    
    @Autowired
    private Executor executor;
    
    public void processOrderAsync(Order order) {
        // 包装Runnable以传递追踪上下文
        Runnable task = RunnableWrapper.of(() -> {
            // 异步任务中的操作会被追踪
            processOrder(order);
        });
        
        executor.execute(task);
    }
    
    public Future<Order> processOrderWithResult(Order order) {
        // 包装Callable以传递追踪上下文
        Callable<Order> task = CallableWrapper.of(() -> {
            return processOrder(order);
        });
        
        return executor.submit(task);
    }
}
```

## 配置告警

在SkyWalking UI中配置告警规则：

### 1. 服务响应时间告警

```yaml
rules:
  service_resp_time_rule:
    metrics-name: service_resp_time
    op: ">"
    threshold: 1000  # 响应时间超过1秒
    period: 10       # 10分钟内
    count: 3         # 连续3次
    silence-period: 5
    message: "服务 {name} 响应时间过长"
```

### 2. 服务成功率告警

```yaml
rules:
  service_sla_rule:
    metrics-name: service_sla
    op: "<"
    threshold: 95    # 成功率低于95%
    period: 10
    count: 2
    silence-period: 5
    message: "服务 {name} 成功率过低"
```

### 3. 端点响应时间告警

```yaml
rules:
  endpoint_resp_time_rule:
    metrics-name: endpoint_resp_time
    op: ">"
    threshold: 2000  # 端点响应时间超过2秒
    period: 10
    count: 3
    silence-period: 5
    message: "端点 {name} 响应时间过长"
```

## 性能优化

### 1. 调整采样率

生产环境建议设置采样率，避免性能影响：

```properties
# 每3秒采样1000个请求
agent.sample_n_per_3_secs=1000
```

### 2. 禁用不需要的插件

```bash
# 移动不需要的插件到optional-plugins目录
mv skywalking-agent/plugins/apm-mongodb-*.jar skywalking-agent/optional-plugins/
```

### 3. 配置忽略路径

```properties
# 忽略健康检查等路径
apm.ignore_suffix=.jpg,.jpeg,.js,.css,.png,.bmp,.gif,.ico,.mp3,.mp4,.html,.svg,/actuator/health,/actuator/prometheus
```

## 故障排查

### 1. Agent无法连接到OAP

检查：
- OAP服务是否启动：`docker ps | grep skywalking-oap`
- 网络是否连通：`telnet skywalking-oap 11800`
- Agent配置是否正确：检查`agent.config`中的`collector.backend_service`

### 2. 没有追踪数据

检查：
- Agent是否正确加载：查看应用启动日志
- 采样率是否过低：调整`agent.sample_n_per_3_secs`
- 服务名称是否正确：检查`agent.service_name`

### 3. 性能影响

优化措施：
- 降低采样率
- 禁用不需要的插件
- 增加OAP服务器资源
- 使用异步上报

## 最佳实践

1. **服务命名规范**: 使用统一的命名格式，如`restaurant-{service}-service`

2. **合理设置采样率**: 
   - 开发环境：1（全量采样）
   - 测试环境：100-500
   - 生产环境：1000-5000

3. **添加业务标签**: 在关键业务方法上添加@Tag注解，便于问题定位

4. **关联日志**: 在日志中输出TraceId，实现日志和链路的关联

5. **配置告警**: 为关键指标配置告警，及时发现问题

6. **定期清理数据**: 配置Elasticsearch的数据保留策略，避免存储爆满

## 参考资料

- [SkyWalking官方文档](https://skywalking.apache.org/docs/)
- [Java Agent配置](https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/readme/)
- [自定义追踪](https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/application-toolkit-trace/)
- [告警配置](https://skywalking.apache.org/docs/main/latest/en/setup/backend/backend-alarm/)
## 项目默认接入方式

当前项目已经支持通过 Docker Compose 自动加载 SkyWalking Agent。

- 各微服务 Dockerfile 支持通过 `SKYWALKING_AGENT_ENABLED=true` 开启 Agent
- `docker-compose-full.yml` 已内置 `skywalking-elasticsearch`、`skywalking-oap`、`skywalking-ui`
- 各微服务会把 `./monitoring/skywalking/skywalking-agent` 挂载到容器内 `/skywalking/agent`

推荐启动顺序：

1. 下载并解压 SkyWalking Java Agent 到 `restaurant-server/monitoring/skywalking/skywalking-agent`
2. 在 `restaurant-server` 目录执行 `docker compose -f docker-compose-full.yml up -d`
3. 打开 `http://localhost:8090` 查看链路
