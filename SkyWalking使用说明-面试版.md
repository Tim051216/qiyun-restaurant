# SkyWalking 使用说明 - 面试版

## 项目现状

**项目中已集成 SkyWalking 进行分布式链路追踪和性能监控**

### 使用情况总览

| 组件 | 版本 | 状态 | 说明 |
|------|------|------|------|
| SkyWalking Agent | 9.0.0 / 9.1.0 | ✅ 已集成 | Java Agent 字节码增强 |
| SkyWalking OAP | 9.6.0 | ✅ 已配置 | 数据收集和分析服务器 |
| SkyWalking UI | 9.6.0 | ✅ 已配置 | 可视化监控面板 |
| Elasticsearch | 8.11.0 | ✅ 已配置 | 链路数据存储 |

---

## 一、SkyWalking 是什么？

### 1.1 定义

Apache SkyWalking 是一个开源的 APM（Application Performance Monitoring）系统，专为微服务、云原生和容器化架构设计。

### 1.2 核心功能

1. **分布式链路追踪**：追踪请求在微服务间的完整调用链路
2. **性能指标监控**：监控服务响应时间、吞吐量、错误率等
3. **服务拓扑图**：自动生成服务依赖关系图
4. **告警功能**：性能异常时自动告警

### 1.3 架构组件

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
                  │ Elasticsearch   │
                  │   (Storage)     │
                  └────────┬─────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  SkyWalking UI  │
                  │  (Dashboard)    │
                  └─────────────────┘
```

---

## 二、项目中的集成方式

### 2.1 Maven 依赖

**文件位置**：`restaurant-order/pom.xml`、`restaurant-server/pom.xml`

```xml
<!-- SkyWalking Toolkit - 用于自定义追踪 -->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>9.0.0</version>
</dependency>

<!-- SkyWalking Logback - 日志关联 -->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-logback-1.x</artifactId>
    <version>9.0.0</version>
</dependency>
```

### 2.2 Docker Compose 配置

**文件位置**：`restaurant-server/monitoring/skywalking/docker-compose-skywalking.yml`

```yaml
version: '3.8'

services:
  # Elasticsearch（SkyWalking 存储）
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: skywalking-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  # SkyWalking OAP Server（数据收集和分析）
  skywalking-oap:
    image: apache/skywalking-oap-server:9.6.0
    container_name: skywalking-oap
    depends_on:
      - elasticsearch
    environment:
      SW_STORAGE: elasticsearch
      SW_STORAGE_ES_CLUSTER_NODES: elasticsearch:9200
    ports:
      - "11800:11800"  # gRPC 端口（Agent 连接）
      - "12800:12800"  # HTTP 端口（UI 连接）

  # SkyWalking UI（可视化面板）
  skywalking-ui:
    image: apache/skywalking-ui:9.6.0
    container_name: skywalking-ui
    depends_on:
      - skywalking-oap
    environment:
      SW_OAP_ADDRESS: http://skywalking-oap:12800
    ports:
      - "8090:8080"
```

**启动命令**：
```bash
cd restaurant-server/monitoring/skywalking
docker-compose -f docker-compose-skywalking.yml up -d
```

**访问地址**：http://localhost:8090

---

## 三、代码中的实际使用

### 3.1 使用 @Trace 注解（自动追踪）

**文件位置**：`restaurant-order/src/main/java/com/qiyun/order/service/impl/OrderServiceImpl.java`

```java
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    /**
     * 创建订单 - 使用 @Trace 和 @Tag 注解
     * 
     * @Trace: 自动创建 Span，追踪方法执行
     * @Tag: 添加业务标签，便于查询和过滤
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Trace  // 自动创建 Span
    @Tag(key = "order.userId", value = "arg[0].userId")  // 添加用户 ID 标签
    @Tag(key = "order.amount", value = "arg[0].totalAmount")  // 添加订单金额标签
    public Order createOrder(Order order) {
        // 获取 TraceId 用于日志关联
        String traceId = TraceContext.traceId();
        
        log.info("创建订单: userId={}, amount={}, traceId={}", 
            order.getUserId(), order.getTotalAmount(), traceId);
        
        // 添加自定义标签
        ActiveSpan.tag("business.type", "order-create");
        ActiveSpan.tag("user.id", String.valueOf(order.getUserId()));
        
        try {
            // 保存订单到数据库
            this.save(order);
            
            // 添加成功日志到 Span
            ActiveSpan.info("订单创建成功: orderId=" + order.getId());
            
            log.info("订单创建成功: orderId={}, traceId={}", order.getId(), traceId);
            
            return order;
        } catch (Exception e) {
            // 记录异常到 Span
            ActiveSpan.error(e);
            
            log.error("订单创建失败: userId={}, traceId={}", order.getUserId(), traceId, e);
            throw e;
        }
    }
}
```

### 3.2 消息队列消费追踪

**文件位置**：`restaurant-order/src/main/java/com/qiyun/order/service/impl/OrderServiceImpl.java`

```java
/**
 * 处理订单支付消息
 * 追踪 RabbitMQ 消息消费过程
 */
@Override
@Transactional(rollbackFor = Exception.class)
@Trace
@Tag(key = "order.id", value = "arg[0].id")
public void processOrderPaid(Order order) {
    String traceId = TraceContext.traceId();
    
    log.info("处理订单支付消息: orderId={}, traceId={}", order.getId(), traceId);
    
    // 添加业务标签
    ActiveSpan.tag("business.type", "order-payment-processing");
    ActiveSpan.tag("message.type", "order-paid");
    ActiveSpan.tag("order.id", String.valueOf(order.getId()));
    
    try {
        // 更新订单状态为已支付
        Order existingOrder = this.getById(order.getId());
        if (existingOrder != null) {
            existingOrder.setStatus(1); // 1=已支付
            boolean success = this.updateById(existingOrder);
            
            if (success) {
                log.info("订单状态更新为已支付: orderId={}, traceId={}", order.getId(), traceId);
                ActiveSpan.info("订单支付状态更新成功");
            } else {
                ActiveSpan.error("订单不存在: orderId=" + order.getId());
            }
        }
        
        // TODO: 发送支付成功通知、更新会员积分、更新销售统计
    } catch (Exception e) {
        ActiveSpan.error(e);
        log.error("订单支付处理失败: orderId={}, traceId={}", order.getId(), traceId, e);
        throw e;
    }
}
```

### 3.3 定时任务追踪

**文件位置**：`restaurant-order/src/main/java/com/qiyun/order/task/OrderTimeoutTask.java`

```java
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTimeoutTask {
    
    private final OrderService orderService;
    
    @Value("${order.timeout.minutes:30}")
    private int timeoutMinutes;
    
    /**
     * 定时关闭超时订单
     * 每 5 分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Trace  // 追踪定时任务执行
    public void closeTimeoutOrders() {
        String traceId = TraceContext.traceId();
        
        log.info("开始执行订单超时关闭任务, traceId={}", traceId);
        
        // 添加业务标签
        ActiveSpan.tag("business.type", "order-timeout-task");
        ActiveSpan.tag("timeout.minutes", String.valueOf(timeoutMinutes));
        
        try {
            // 查询超时订单
            List<Order> timeoutOrders = orderService.findTimeoutOrders(timeoutMinutes);
            
            if (timeoutOrders.isEmpty()) {
                log.info("没有超时订单需要关闭, traceId={}", traceId);
                ActiveSpan.info("没有超时订单需要关闭");
                return;
            }
            
            log.info("发现{}个超时订单，开始关闭, traceId={}", timeoutOrders.size(), traceId);
            ActiveSpan.info("发现超时订单: count=" + timeoutOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 逐个关闭超时订单
            for (Order order : timeoutOrders) {
                try {
                    boolean success = orderService.closeTimeoutOrder(order.getId());
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("关闭超时订单失败: orderId={}, traceId={}", order.getId(), traceId, e);
                }
            }
            
            log.info("订单超时关闭任务完成: 总数={}, 成功={}, 失败={}, traceId={}", 
                timeoutOrders.size(), successCount, failCount, traceId);
            
            ActiveSpan.info(String.format("任务完成: 总数=%d, 成功=%d, 失败=%d", 
                timeoutOrders.size(), successCount, failCount));
            
        } catch (Exception e) {
            ActiveSpan.error(e);
            log.error("订单超时关闭任务执行异常, traceId={}", traceId, e);
        }
    }
}
```

---

## 四、核心功能说明

### 4.1 TraceId（链路追踪 ID）

**作用**：唯一标识一次完整的请求链路

**使用场景**：
```java
// 获取 TraceId
String traceId = TraceContext.traceId();

// 在日志中输出 TraceId，实现日志和链路的关联
log.info("处理订单: orderId={}, traceId={}", orderId, traceId);
```

**好处**：
- 可以通过 TraceId 在日志中查找完整的请求链路
- 可以在 SkyWalking UI 中通过 TraceId 查看详细的调用链

### 4.2 Span（跨度）

**定义**：表示一次操作或方法调用

**类型**：
- **Entry Span**：入口 Span（如 HTTP 请求）
- **Local Span**：本地 Span（如方法调用）
- **Exit Span**：出口 Span（如数据库查询、RPC 调用）

**自动创建**：
```java
@Trace  // 自动创建 Span
public void someMethod() {
    // 方法逻辑
}
```

### 4.3 Tag（标签）

**作用**：为 Span 添加业务标签，便于查询和过滤

**使用方式**：
```java
// 方式一：使用 @Tag 注解（参数标签）
@Tag(key = "order.id", value = "arg[0]")  // arg[0] 表示第一个参数
@Tag(key = "user.id", value = "arg[1]")   // arg[1] 表示第二个参数
public void createOrder(Long orderId, Long userId) {
    // ...
}

// 方式二：使用 ActiveSpan 动态添加标签
ActiveSpan.tag("business.type", "order-create");
ActiveSpan.tag("order.amount", "199.99");
```

### 4.4 日志关联

**作用**：在 Span 中记录日志，便于问题排查

```java
// 记录信息日志
ActiveSpan.info("订单创建成功: orderId=" + orderId);

// 记录错误日志
ActiveSpan.error(exception);
```

---

## 五、面试要点

### 5.1 为什么使用 SkyWalking？

**标准回答**：

"我们的项目是微服务架构，一个用户请求可能会经过 Gateway → Order Service → Dish Service 等多个服务。使用 SkyWalking 可以：

1. **追踪完整链路**：看到请求在各个服务间的调用关系和耗时
2. **快速定位问题**：当某个接口慢时，可以看到是哪个服务、哪个方法慢
3. **性能监控**：实时监控服务的 QPS、响应时间、错误率
4. **服务依赖分析**：自动生成服务拓扑图，了解服务间的依赖关系"

### 5.2 SkyWalking 的工作原理？

**技术回答**：

"SkyWalking 使用 Java Agent 字节码增强技术：

1. **Agent 启动**：应用启动时加载 SkyWalking Agent（-javaagent 参数）
2. **字节码增强**：Agent 拦截关键类（如 HTTP 客户端、数据库驱动），插入追踪代码
3. **数据收集**：拦截到的调用信息（TraceId、SpanId、耗时等）发送到 OAP Server
4. **数据存储**：OAP Server 将数据存储到 Elasticsearch
5. **数据展示**：SkyWalking UI 从 OAP Server 查询数据并展示

**优点**：
- 无侵入：不需要修改业务代码（除了自定义追踪）
- 性能影响小：异步上报，对业务影响很小
- 功能强大：自动追踪 HTTP、RPC、数据库、消息队列等"

### 5.3 如何保证 SkyWalking 不影响性能？

**优化措施**：

1. **调整采样率**：
   ```properties
   # 生产环境：每 3 秒采样 1000 个请求（不是全量采样）
   agent.sample_n_per_3_secs=1000
   ```

2. **异步上报**：Agent 异步发送数据到 OAP，不阻塞业务线程

3. **禁用不需要的插件**：移除不需要的插件，减少拦截点

4. **忽略静态资源**：
   ```properties
   agent.ignore_suffix=.jpg,.css,.js,.png
   ```

5. **增加 OAP 资源**：OAP Server 使用独立服务器，不影响业务服务

### 5.4 TraceId 如何在微服务间传递？

**传递机制**：

"SkyWalking Agent 会自动在 HTTP Header 中添加追踪信息：

```
sw8: 1-TraceId-SegmentId-SpanId-ServiceName-...
```

当请求从 Gateway 调用 Order Service 时：
1. Gateway 的 Agent 生成 TraceId，添加到 HTTP Header
2. Order Service 的 Agent 从 Header 中提取 TraceId
3. Order Service 继续调用 Dish Service 时，再次传递 TraceId

这样整个链路的 TraceId 都是一致的，可以串联起来。

**对于 RabbitMQ 消息**：
- 发送消息时，Agent 将 TraceId 放入消息 Header
- 消费消息时，Agent 从 Header 中提取 TraceId
- 实现跨消息队列的链路追踪"

### 5.5 项目中的实际应用场景？

**具体案例**：

"我们在项目中主要用 SkyWalking 解决了以下问题：

1. **订单创建慢问题**：
   - 通过链路追踪发现是库存扣减接口慢（调用 Dish Service）
   - 优化方案：改用 Redis + Lua 脚本扣减库存
   - 效果：响应时间从 2 秒降到 50ms

2. **消息消费失败排查**：
   - 通过 TraceId 关联日志，快速定位是哪条消息处理失败
   - 通过 Span 详情看到具体是哪个步骤出错
   - 修复后验证：通过 SkyWalking 监控消息消费成功率

3. **定时任务监控**：
   - 使用 @Trace 追踪定时任务执行
   - 监控任务执行时间、成功率
   - 及时发现任务执行异常"

---

## 六、实际操作演示

### 6.1 启动 SkyWalking

```bash
# 1. 启动 SkyWalking 基础设施
cd restaurant-server/monitoring/skywalking
docker-compose -f docker-compose-skywalking.yml up -d

# 2. 检查服务状态
docker ps | grep skywalking

# 3. 访问 SkyWalking UI
# 浏览器打开: http://localhost:8090
```

### 6.2 启动微服务（带 Agent）

```bash
# 方式一：使用 Docker Compose（推荐）
# 在 docker-compose.yml 中配置 Agent
environment:
  - JAVA_TOOL_OPTIONS=-javaagent:/skywalking/agent/skywalking-agent.jar
  - SW_AGENT_NAME=restaurant-order-service
  - SW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800

# 方式二：使用启动脚本
java -javaagent:/path/to/skywalking-agent.jar \
  -Dskywalking.agent.service_name=restaurant-order-service \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -jar app.jar
```

### 6.3 查看链路追踪

1. **访问 SkyWalking UI**：http://localhost:8090
2. **选择服务**：在顶部选择 `restaurant-order-service`
3. **查看链路**：点击 "Trace" 标签，查看最近的请求链路
4. **查看详情**：点击某个 Trace，查看完整的调用链和耗时分布

---

## 七、代码文件位置汇总

| 功能 | 文件路径 |
|------|----------|
| Maven 依赖 | `restaurant-order/pom.xml`<br>`restaurant-server/pom.xml` |
| Docker Compose 配置 | `restaurant-server/monitoring/skywalking/docker-compose-skywalking.yml` |
| 详细文档 | `restaurant-server/monitoring/skywalking/README.md` |
| 订单服务追踪 | `restaurant-order/src/main/java/com/qiyun/order/service/impl/OrderServiceImpl.java` |
| 定时任务追踪 | `restaurant-order/src/main/java/com/qiyun/order/task/OrderTimeoutTask.java` |
| 追踪测试 | `restaurant-order/src/test/java/com/qiyun/order/TracingPropertiesTest.java` |

---

## 八、总结

### 项目集成情况
- ✅ 已添加 SkyWalking 依赖
- ✅ 已配置 Docker Compose 环境
- ✅ 已在关键业务方法上使用 @Trace 注解
- ✅ 已实现 TraceId 日志关联
- ✅ 已追踪订单创建、支付、取消等核心流程
- ✅ 已追踪定时任务执行

### 核心价值
1. **快速定位问题**：通过链路追踪快速找到慢接口
2. **性能优化**：通过监控数据指导性能优化
3. **服务治理**：了解服务依赖关系，优化架构
4. **故障排查**：通过 TraceId 关联日志，快速定位问题

### 面试加分项
- 能说出 SkyWalking 的工作原理（字节码增强）
- 能讲清楚 TraceId 如何在微服务间传递
- 能举出实际使用案例（订单创建慢问题）
- 能说出性能优化措施（采样率、异步上报）

**这是一个完整的、可落地的 APM 监控方案！**
