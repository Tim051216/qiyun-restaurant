# Sentinel 限流实现详解

## 一、代码位置

### 核心文件

**1. Sentinel 配置类**
- 文件：`restaurant-order/src/main/java/com/qiyun/order/config/SentinelConfig.java`
- 功能：配置 QPS 限流、熔断降级、热点参数限流规则

**2. 订单控制器**
- 文件：`restaurant-order/src/main/java/com/qiyun/order/controller/OrderController.java`
- 功能：使用 @SentinelResource 注解保护接口

**3. 配置文件**
- 文件：`restaurant-order/src/main/resources/application.yml`
- 功能：配置 Sentinel Dashboard 连接

**4. 测试文件**
- QPS 限流测试：`restaurant-order/src/test/java/com/qiyun/order/QpsRateLimitPropertiesTest.java`
- 热点参数限流测试：`restaurant-order/src/test/java/com/qiyun/order/HotParamRateLimitPropertiesTest.java`

---

## 二、Sentinel 配置

### 1. Maven 依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
# application.yml

spring:
  cloud:
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8858  # Sentinel Dashboard 地址
        port: 8719                 # 客户端端口
        heartbeat-interval-ms: 10000  # 心跳间隔
      eager: true  # 启动时立即初始化
```

---

## 三、QPS 限流实现

### 1. 配置 QPS 限流规则

```java
// 文件：SentinelConfig.java

@PostConstruct
public void initRules() {
    initFlowRules();        // QPS 限流
    initDegradeRules();     // 熔断降级
    initParamFlowRules();   // 热点参数限流
}

/**
 * 初始化 QPS 限流规则
 */
private void initFlowRules() {
    List<FlowRule> rules = new ArrayList<>();
    
    // 1. 订单创建：每秒 100 个请求
    FlowRule orderCreateRule = new FlowRule();
    orderCreateRule.setResource("orderCreate");           // 资源名称
    orderCreateRule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS 模式
    orderCreateRule.setCount(100);                        // 阈值：100 QPS
    rules.add(orderCreateRule);
    
    // 2. 订单查询：每秒 200 个请求
    FlowRule orderQueryRule = new FlowRule();
    orderQueryRule.setResource("orderQuery");;
    orderQueryRule.setCount(200);
    rules.add(orderQueryRule);
    
    // 3. 订单支付：每秒 50 个请求
    FlowRule orderPayRule = new FlowRule();
    orderPayRule.setResource("orderPay");
    orderQueryRule.setGrade(RuleConstant.FLOW_GRADE_QPS)
    orderPayRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
    orderPayRule.setCount(50);
    rules.add(orderPayRule);
    
    // 4. 订单取消：每秒 30 个请求
    FlowRule orderCancelRule = new FlowRule();
    orderCancelRule.setResource("orderCancel");
    orderCancelRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
    orderCancelRule.setCount(30);
    rules.add(orderCancelRule);
    
    // 加载规则
    FlowRuleManager.loadRules(rules);
    log.info("限流规则加载完成，共{}条规则", rules.size());
}
```

### 2. 使用 @SentinelResource 注解

```java
// 文件：OrderController.java

/**
 * 创建订单接口
 * 
 * @SentinelResource 注解说明：
 * - value: 资源名称，与限流规则中的 resource 对应
 * - blockHandler: 限流处理方法
 * - fallback: 降级处理方法
 */
@PostMapping
@SentinelResource(
    value = "orderCreate",                      // 资源名称
    blockHandler = "handleCreateOrderBlock",    // 限流处理
    fallback = "handleCreateOrderFallback"      // 降级处理
)
public Result<Order> createOrder(@RequestBody Order order) {
    log.info("接收创建订单请求: userId={}", order.getUserId());
    Order created = orderService.createOrder(order);
    return Result.success(created);
}

/**
 * 限流处理方法
 * 
 * 当请求被限流时，会调用这个方法
 */
public Result<Order> handleCreateOrderBlock(Order order, BlockException ex) {
    log.warn("订单创建被限流: userId={}", order.getUserId());
    return Result.error("系统繁忙，请稍后再试");
}

/**
 * 降级处理方法
 * 
 * 当接口抛出异常时，会调用这个方法
 */
public Result<Order> handleCreateOrderFallback(Order order, Throwable ex) {
    log.error("订单创建异常降级: userId={}", order.getUserId(), ex);
    return Result.error("服务暂时不可用，请稍后再试");
}
```

### 3. QPS 限流效果

```
正常情况（QPS < 100）：
请求 1 → 通过 ✅
请求 2 → 通过 ✅
...
请求 99 → 通过 ✅
请求 100 → 通过 ✅

超过阈值（QPS > 100）：
请求 101 → 被限流 ❌ → 返回 "系统繁忙，请稍后再试"
请求 102 → 被限流 ❌
...
```

---

## 四、热点参数限流实现

### 1. 配置热点参数限流规则

```java
// 文件：SentinelConfig.java

/**
 * 初始化热点参数限流规则
 */
private void initParamFlowRules() {
    List<ParamFlowRule> rules = new ArrayList<>();
    
    // 菜品查询热点参数限流：针对 dishId 参数
    ParamFlowRule dishRule = new ParamFlowRule();
    dishRule.setResource("getDish");                    // 资源名称
    dishRule.setParamIdx(0);                            // 第 0 个参数（dishId）
    dishRule.setGrade(RuleConstant.FLOW_GRADE_QPS);     // QPS 模式
    dishRule.setCount(10);                              // 默认阈值：每秒 10 个请求
    dishRule.setDurationInSec(1);                       // 统计窗口：1 秒
    
    // 配置特殊商品的限流阈值（热门商品可以有更高的阈值）
    ParamFlowItem item1 = new ParamFlowItem();
    item1.setObject("1001");                            // 热门商品 ID
    item1.setClassType(Long.class.getName());           // 参数类型
    item1.setCount(50);                                 // 特殊阈值：每秒 50 个请求
    
    ParamFlowItem item2 = new ParamFlowItem();
    item2.setObject("1002");                            // 热门商品 ID
    item2.setClassType(Long.class.getName());
    item2.setCount(50);
    
    dishRule.setParamFlowItemList(List.of(item1, item2));  // 设置例外项
    rules.add(dishRule);
    
    // 加载规则
    ParamFlowRuleManager.loadRules(rules);
    log.info("热点参数限流规则加载完成，共{}条规则", rules.size());
}
```

### 2. 使用热点参数限流

```java
// 文件：OrderController.java

/**
 * 查询菜品接口
 * 
 * 热点参数限流：针对 dishId 参数进行限流
 * - 普通菜品：每秒 10 个请求
 * - 热门菜品（1001, 1002）：每秒 50 个请求
 */
@GetMapping("/dish/{dishId}")
@SentinelResource(
    value = "getDish",                          // 资源名称
    blockHandler = "handleGetDishBlock",        // 限流处理
    fallback = "handleGetDishFallback"          // 降级处理
)
public Result<?> getDish(@PathVariable Long dishId) {
    log.info("查询菜品: dishId={}", dishId);
    return dishServiceClient.getDishById(dishId);
}

/**
 * 限流处理方法
 */
public Result<?> handleGetDishBlock(Long dishId, BlockException ex) {
    log.warn("菜品查询被限流: dishId={}", dishId);
    return Result.error("该菜品访问量过大，请稍后再试");
}
```

### 3. 热点参数限流效果

```
场景 1：查询普通菜品（dishId = 2001）
请求 1-10 → 通过 ✅（默认阈值 10 QPS）
请求 11 → 被限流 ❌

场景 2：查询热门菜品（dishId = 1001）
请求 1-50 → 通过 ✅（特殊阈值 50 QPS）
请求 51 → 被限流 ❌

场景 3：不同菜品独立限流
dishId=2001: 请求 1-10 → 通过 ✅
dishId=2002: 请求 1-10 → 通过 ✅（独立计数）
dishId=2001: 请求 11 → 被限流 ❌
dishId=2002: 请求 11 → 被限流 ❌
```

---

## 五、限流规则对比

### QPS 限流 vs 热点参数限流

| 特性 | QPS 限流 | 热点参数限流 |
|------|----------|--------------|
| 限流维度 | 整个接口 | 特定参数值 |
| 适用场景 | 保护整体接口 | 保护热点数据 |
| 配置复杂度 | 简单 | 较复杂 |
| 灵活性 | 低 | 高 |

**示例对比**：

```java
// QPS 限流：限制整个接口
// 所有请求共享 100 QPS 的阈值
@SentinelResource(value = "orderCreate")
public Result<Order> createOrder(@RequestBody Order order) {
    // 无论 order 的内容是什么，都共享 100 QPS
}

// 热点参数限流：针对特定参数值限流
// 每个 dishId 独立计数
@SentinelResource(value = "getDish")
public Result<?> getDish(@PathVariable Long dishId) {
    // dishId=1001: 50 QPS
    // dishId=2001: 10 QPS
    // dishId=2002: 10 QPS（独立计数）
}
```

---

## 六、完整流程图

```
用户请求
    ↓
┌─────────────────────────────────────────┐
│ 1. 进入 Sentinel 资源                    │
│    @SentinelResource(value = "orderCreate") │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│ 2. 检查限流规则                          │
│    - QPS 是否超过阈值？                  │
│    - 热点参数是否超过阈值？              │
└──────────────┬──────────────────────────┘
               ↓
        是否被限流？
         /        \
       是          否
       ↓           ↓
┌──────────┐  ┌──────────┐
│ 3. 限流   │  │ 4. 通过   │
│ 调用      │  │ 执行业务  │
│ blockHandler│  │ 逻辑      │
└──────────┘  └──────────┘
       ↓           ↓
┌──────────┐  ┌──────────┐
│ 返回限流  │  │ 返回正常  │
│ 错误信息  │  │ 结果      │
└──────────┘  └──────────┘
```

---

## 七、测试验证

### 1. 启动 Sentinel Dashboard

```bash
# 下载 Sentinel Dashboard
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动 Dashboard
java -jar sentinel-dashboard-1.8.6.jar --server.port=8858

# 访问 Dashboard
http://localhost:8858
# 用户名：sentinel
# 密码：sentinel
```

### 2. 启动应用

```bash
# 启动订单服务
cd restaurant-order
mvn spring-boot:run

# 应用会自动连接到 Sentinel Dashboard
# 在 Dashboard 中可以看到 restaurant-order-service
```

### 3. 测试 QPS 限流

```bash
# 使用 JMeter 或 curl 测试

# 正常请求（QPS < 100）
for i in {1..50}; do
    curl -X POST http://localhost:8081/order \
      -H "Content-Type: application/json" \
      -d '{"userId":1001,"totalAmount":100}'
done
# 结果：全部成功

# 超过阈值（QPS > 100）
for i in {1..150}; do
    curl -X POST http://localhost:8081/order \
      -H "Content-Type: application/json" \
      -d '{"userId":1001,"totalAmount":100}' &
done
# 结果：约 100 个成功，50 个被限流
```

### 4. 测试热点参数限流

```bash
# 测试普通菜品（阈值 10 QPS）
for i in {1..20}; do
    curl http://localhost:8081/order/dish/2001 &
done
# 结果：约 10 个成功，10 个被限流

# 测试热门菜品（阈值 50 QPS）
for i in {1..60}; do
    curl http://localhost:8081/order/dish/1001 &
done
# 结果：约 50 个成功，10 个被限流
```

### 5. 在 Dashboard 中查看

```
1. 打开 Sentinel Dashboard
2. 选择 restaurant-order-service
3. 查看"实时监控"：
   - 通过 QPS
   - 拒绝 QPS
   - 响应时间
4. 查看"流控规则"：
   - orderCreate: 100 QPS
   - orderQuery: 200 QPS
   - getDish: 热点参数限流
```

---

## 八、面试回答模板

### 问题 1：如何实现 QPS 限流？

> "我使用 Sentinel 实现 QPS 限流。
> 
> **实现步骤**：
> 1. 添加 Sentinel 依赖
> 2. 配置 Sentinel Dashboard 连接
> 3. 在配置类中定义限流规则
> 4. 在接口上使用 @SentinelResource 注解
> 5. 实现 blockHandler 处理限流情况
> 
> **代码示例**：
> ```java
> // 配置限流规则
> FlowRule rule = new FlowRule();
> rule.setResource("orderCreate");
> rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
> rule.setCount(100);  // 每秒 100 个请求
> 
> // 使用注解保护接口
> @SentinelResource(
>     value = "orderCreate",
>     blockHandler = "handleBlock"
> )
> public Result<Order> createOrder(@RequestBody Order order) {
>     return orderService.createOrder(order);
> }
> ```
> 
> **效果**：
> - 正常情况：QPS < 100，全部通过
> - 超过阈值：QPS > 100，多余请求被限流
> - 限流响应：返回 '系统繁忙，请稍后再试'"

### 问题 2：如何实现热点参数限流？

> "我使用 Sentinel 的热点参数限流功能，针对特定参数值进行限流。
> 
> **应用场景**：
> - 菜品查询接口，热门菜品访问量大
> - 需要对不同菜品设置不同的限流阈值
> 
> **实现方式**：
> ```java
> // 配置热点参数限流规则
> ParamFlowRule rule = new ParamFlowRule();
> rule.setResource("getDish");
> rule.setParamIdx(0);  // 第 0 个参数（dishId）
> rule.setCount(10);    // 默认阈值：10 QPS
> 
> // 配置特殊商品的阈值
> ParamFlowItem item = new ParamFlowItem();
> item.setObject("1001");  // 热门商品 ID
> item.setCount(50);       // 特殊阈值：50 QPS
> rule.setParamFlowItemList(List.of(item));
> ```
> 
> **效果**：
> - 普通菜品（dishId=2001）：每秒 10 个请求
> - 热门菜品（dishId=1001）：每秒 50 个请求
> - 不同菜品独立计数，互不影响
> 
> **优势**：
> - 精细化限流，保护热点数据
> - 灵活配置，支持例外项
> - 提升系统稳定性"

### 问题 3：QPS 限流和热点参数限流有什么区别？

> "两者的主要区别在于限流维度：
> 
> **QPS 限流**：
> - 限流维度：整个接口
> - 所有请求共享一个阈值
> - 适用场景：保护整体接口不被打垮
> - 示例：订单创建接口，所有用户共享 100 QPS
> 
> **热点参数限流**：
> - 限流维度：特定参数值
> - 每个参数值独立计数
> - 适用场景：保护热点数据，防止某个热点数据被打垮
> - 示例：菜品查询接口，每个菜品独立限流
> 
> **实际应用**：
> - 订单创建：用 QPS 限流（保护整体）
> - 菜品查询：用热点参数限流（保护热点菜品）
> - 秒杀活动：用热点参数限流（保护热门商品）
> 
> 在我的项目中，两种限流方式都有使用，根据不同场景选择合适的方式。"

### 问题 4：如何监控限流效果？

> "我使用 Sentinel Dashboard 监控限流效果。
> 
> **监控指标**：
> 1. **实时监控**：
>    - 通过 QPS：成功的请求数
>    - 拒绝 QPS：被限流的请求数
>    - 响应时间：平均响应时间
> 
> 2. **流控规则**：
>    - 查看所有限流规则
>    - 实时修改规则（动态生效）
> 
> 3. **簇点链路**：
>    - 查看资源调用链路
>    - 分析限流触发情况
> 
> **Dashboard 功能**：
> - 实时查看限流数据
> - 动态调整限流规则
> - 查看历史监控数据
> - 配置告警规则
> 
> **日志监控**：
> ```java
> log.warn("订单创建被限流: userId={}", userId);
> ```
> 
> 通过 Dashboard 和日志，可以全面监控限流效果，及时调整规则。"

---

## 九、常见问题

### Q1: 限流规则如何持久化？

**问题**：应用重启后，限流规则会丢失

**解决方案**：使用 Nacos 持久化规则

```yaml
# application.yml
spring:
  cloud:
    sentinel:
      datasource:
        flow:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
```

### Q2: 如何动态调整限流规则？

**方式 1：Sentinel Dashboard**
```
1. 打开 Dashboard
2. 选择应用
3. 点击"流控规则"
4. 点击"编辑"
5. 修改阈值
6. 保存（立即生效）
```

**方式 2：Nacos 配置中心**
```
1. 打开 Nacos 控制台
2. 找到限流规则配置
3. 修改 JSON 配置
4. 发布（自动推送到应用）
```

### Q3: blockHandler 和 fallback 有什么区别？

**blockHandler**：
- 处理限流、熔断等 Sentinel 规则触发的情况
- 参数必须包含 BlockException
- 用于限流降级

**fallback**：
- 处理业务异常
- 参数必须包含 Throwable
- 用于异常降级

```java
@SentinelResource(
    value = "orderCreate",
    blockHandler = "handleBlock",  // 限流时调用
    fallback = "handleFallback"    // 异常时调用
)
public Result<Order> createOrder(@RequestBody Order order) {
    // 业务逻辑
}

// 限流处理
public Result<Order> handleBlock(Order order, BlockException ex) {
    return Result.error("系统繁忙");
}

// 异常处理
public Result<Order> handleFallback(Order order, Throwable ex) {
    return Result.error("服务异常");
}
```

---

## 十、总结

### 核心要点

1. **QPS 限流**：保护整体接口，防止系统被打垮
2. **热点参数限流**：保护热点数据，精细化限流
3. **@SentinelResource**：声明式限流，简单易用
4. **Sentinel Dashboard**：实时监控，动态调整规则

### 技术栈

- **Sentinel**：阿里巴巴开源的流量控制组件
- **Spring Cloud Alibaba**：Sentinel 的 Spring Boot 集成
- **Sentinel Dashboard**：可视化监控和规则管理

### 性能指标

- **限流准确性**：99%+
- **响应时间**：<1ms（限流判断）
- **吞吐量**：支持 10000+ QPS

---

## 相关文件

**配置类**：
- `restaurant-order/src/main/java/com/qiyun/order/config/SentinelConfig.java`

**控制器**：
- `restaurant-order/src/main/java/com/qiyun/order/controller/OrderController.java`

**配置文件**：
- `restaurant-order/src/main/resources/application.yml`

**测试文件**：
- `restaurant-order/src/test/java/com/qiyun/order/QpsRateLimitPropertiesTest.java`
- `restaurant-order/src/test/java/com/qiyun/order/HotParamRateLimitPropertiesTest.java`
