# ES 与 MySQL 最终一致性方案

## 问题背景

在使用 Elasticsearch (ES) 作为搜索引擎时,需要保证 ES 中的数据与 MySQL 中的数据保持一致。但由于 ES 和 MySQL 是两个独立的系统,如何保证数据一致性是一个常见的技术挑战。

---

## 一、常见方案对比

| 方案 | 实时性 | 可靠性 | 复杂度 | 适用场景 |
|------|--------|--------|--------|----------|
| **同步双写** | 高 | 低 | 低 | 小型项目、数据量小 |
| **异步双写(MQ)** | 中 | 中 | 中 | 中型项目、允许短暂延迟 |
| **Canal 监听 binlog** | 高 | 高 | 高 | 大型项目、数据量大 |
| **定时任务同步** | 低 | 中 | 低 | 对实时性要求不高 |
| **Logstash 同步** | 中 | 中 | 中 | 初始化数据、全量同步 |

---

## 二、方案一：同步双写

### 2.1 原理

在业务代码中,同时写入 MySQL 和 ES。

```
用户请求 → Service
           ↓
    1. 写入 MySQL
           ↓
    2. 写入 ES
           ↓
    返回成功
```

### 2.2 代码实现

```java
@Service
public class DishService {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    /**
     * 新增菜品 - 同步双写
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDish(Dish dish) {
        // 1. 写入 MySQL
        dishMapper.insert(dish);
        
        // 2. 写入 ES
        try {
            DishDocument doc = convertToDocument(dish);
            esTemplate.save(doc);
        } catch (Exception e) {
            log.error("写入 ES 失败: dishId={}", dish.getId(), e);
            // 记录失败日志,后续补偿
            recordSyncFailure(dish.getId(), "INSERT");
            // 不抛异常,避免影响主流程
        }
    }
    
    /**
     * 更新菜品 - 同步双写
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDish(Dish dish) {
        // 1. 更新 MySQL
        dishMapper.updateById(dish);
        
        // 2. 更新 ES
        try {
            DishDocument doc = convertToDocument(dish);
            esTemplate.save(doc);
        } catch (Exception e) {
            log.error("更新 ES 失败: dishId={}", dish.getId(), e);
            recordSyncFailure(dish.getId(), "UPDATE");
        }
    }
    
    /**
     * 删除菜品 - 同步双写
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDish(Long dishId) {
        // 1. 删除 MySQL
        dishMapper.deleteById(dishId);
        
        // 2. 删除 ES
        try {
            esTemplate.delete(dishId.toString(), DishDocument.class);
        } catch (Exception e) {
            log.error("删除 ES 失败: dishId={}", dishId, e);
            recordSyncFailure(dishId, "DELETE");
        }
    }
}
```

### 2.3 优缺点

**优点**:
- 实现简单,代码直观
- 实时性高,数据立即同步

**缺点**:
- 耦合度高,业务代码侵入性强
- ES 故障会影响主流程
- 无法保证强一致性(ES 写入失败时)
- 性能较差(同步等待 ES 响应)

### 2.4 改进：失败补偿机制

```java
/**
 * 记录同步失败的数据
 */
private void recordSyncFailure(Long dishId, String operation) {
    SyncFailureLog log = new SyncFailureLog();
    log.setEntityId(dishId);
    log.setEntityType("DISH");
    log.setOperation(operation);
    log.setRetryCount(0);
    log.setCreateTime(LocalDateTime.now());
    syncFailureMapper.insert(log);
}

/**
 * 定时任务：重试失败的同步
 */
@Scheduled(fixedDelay = 60000) // 每分钟执行一次
public void retryFailedSync() {
    List<SyncFailureLog> failures = syncFailureMapper.selectPendingRetries();
    
    for (SyncFailureLog failure : failures) {
        try {
            // 重新同步到 ES
            Dish dish = dishMapper.selectById(failure.getEntityId());
            if (dish != null) {
                DishDocument doc = convertToDocument(dish);
                esTemplate.save(doc);
                
                // 标记为成功
                syncFailureMapper.markAsSuccess(failure.getId());
            }
        } catch (Exception e) {
            // 增加重试次数
            syncFailureMapper.incrementRetryCount(failure.getId());
            
            // 超过最大重试次数,发送告警
            if (failure.getRetryCount() >= 3) {
                sendAlert("ES 同步失败: " + failure.getEntityId());
            }
        }
    }
}
```

---

## 三、方案二：异步双写(MQ)

### 3.1 原理

使用消息队列(RabbitMQ/Kafka)异步同步数据到 ES。

```
用户请求 → Service
           ↓
    1. 写入 MySQL
           ↓
    2. 发送消息到 MQ
           ↓
    返回成功
           ↓
    MQ Consumer 消费消息
           ↓
    写入 ES
```

### 3.2 代码实现

#### 生产者

```java
@Service
public class DishService {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 新增菜品 - 异步同步
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDish(Dish dish) {
        // 1. 写入 MySQL
        dishMapper.insert(dish);
        
        // 2. 发送消息到 MQ
        DishSyncMessage message = new DishSyncMessage();
        message.setDishId(dish.getId());
        message.setOperation("INSERT");
        message.setData(dish);
        
        rabbitTemplate.convertAndSend(
            "dish.sync.exchange",
            "dish.sync.key",
            message
        );
    }
}
```

#### 消费者

```java
@Component
public class DishSyncConsumer {
    
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    @Autowired
    private DishMapper dishMapper;
    
    @RabbitListener(queues = "dish.sync.queue")
    public void handleDishSync(DishSyncMessage message) {
        try {
            switch (message.getOperation()) {
                case "INSERT":
                case "UPDATE":
                    // 从 MySQL 查询最新数据
                    Dish dish = dishMapper.selectById(message.getDishId());
                    if (dish != null) {
                        DishDocument doc = convertToDocument(dish);
                        esTemplate.save(doc);
                    }
                    break;
                    
                case "DELETE":
                    esTemplate.delete(
                        message.getDishId().toString(),
                        DishDocument.class
                    );
                    break;
            }
            
            log.info("ES 同步成功: dishId={}, operation={}", 
                message.getDishId(), message.getOperation());
                
        } catch (Exception e) {
            log.error("ES 同步失败: dishId={}", message.getDishId(), e);
            // 消息会重新入队,自动重试
            throw new RuntimeException("ES 同步失败", e);
        }
    }
}
```

### 3.3 优缺点

**优点**:
- 解耦,ES 故障不影响主流程
- 性能好,异步处理不阻塞
- 可靠性高,MQ 保证消息不丢失
- 支持重试机制

**缺点**:
- 有延迟(通常几百毫秒)
- 需要维护 MQ
- 增加系统复杂度

---

## 四、方案三：Canal 监听 binlog(推荐)

### 4.1 原理

使用 Canal 监听 MySQL binlog,自动同步数据变更到 ES。

```
MySQL 数据变更
    ↓ (binlog)
Canal Server 监听
    ↓ (解析)
Canal Client 消费
    ↓ (处理)
写入 ES
```

### 4.2 代码实现

```java
@Component
public class DishCanalListener {
    
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    @Autowired
    private DishMapper dishMapper;
    
    /**
     * 监听菜品表变更
     */
    @CanalListener(
        destination = "example",
        schema = "restaurant",
        table = "t_dish"
    )
    public void onDishChange(CanalEntry.Entry entry) {
        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        EventType eventType = rowChange.getEventType();
        
        for (RowData rowData : rowChange.getRowDatasList()) {
            switch (eventType) {
                case INSERT:
                case UPDATE:
                    handleInsertOrUpdate(rowData);
                    break;
                    
                case DELETE:
                    handleDelete(rowData);
                    break;
            }
        }
    }
    
    private void handleInsertOrUpdate(RowData rowData) {
        // 获取主键
        Long dishId = getLongValue(rowData.getAfterColumnsList(), "id");
        
        // 从 MySQL 查询完整数据
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            DishDocument doc = convertToDocument(dish);
            esTemplate.save(doc);
            log.info("ES 同步成功(INSERT/UPDATE): dishId={}", dishId);
        }
    }
    
    private void handleDelete(RowData rowData) {
        // 获取主键
        Long dishId = getLongValue(rowData.getBeforeColumnsList(), "id");
        
        esTemplate.delete(dishId.toString(), DishDocument.class);
        log.info("ES 同步成功(DELETE): dishId={}", dishId);
    }
    
    private Long getLongValue(List<Column> columns, String columnName) {
        for (Column column : columns) {
            if (column.getName().equals(columnName)) {
                return Long.parseLong(column.getValue());
            }
        }
        return null;
    }
}
```

### 4.3 优缺点

**优点**:
- 完全解耦,业务代码无侵入
- 实时性高,毫秒级延迟
- 可靠性高,基于 binlog 不丢数据
- 通用性强,可用于多种场景

**缺点**:
- 部署复杂,需要额外维护 Canal
- 需要开启 MySQL binlog
- 学习成本较高

---

## 五、方案四：定时任务同步

### 5.1 原理

定时扫描 MySQL 数据,同步到 ES。

### 5.2 代码实现

```java
@Component
public class DishSyncScheduler {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    /**
     * 每 5 分钟同步一次
     */
    @Scheduled(fixedDelay = 300000)
    public void syncDishToES() {
        log.info("开始同步菜品数据到 ES");
        
        // 查询最近更新的数据
        LocalDateTime lastSyncTime = getLastSyncTime();
        List<Dish> dishes = dishMapper.selectUpdatedAfter(lastSyncTime);
        
        int successCount = 0;
        int failCount = 0;
        
        for (Dish dish : dishes) {
            try {
                DishDocument doc = convertToDocument(dish);
                esTemplate.save(doc);
                successCount++;
            } catch (Exception e) {
                log.error("同步失败: dishId={}", dish.getId(), e);
                failCount++;
            }
        }
        
        // 更新同步时间
        updateLastSyncTime(LocalDateTime.now());
        
        log.info("同步完成: 成功={}, 失败={}", successCount, failCount);
    }
    
    /**
     * 全量同步(初始化或修复数据)
     */
    public void fullSync() {
        log.info("开始全量同步");
        
        // 分页查询所有数据
        int pageSize = 1000;
        int pageNum = 1;
        
        while (true) {
            List<Dish> dishes = dishMapper.selectPage(pageNum, pageSize);
            if (dishes.isEmpty()) {
                break;
            }
            
            // 批量写入 ES
            List<DishDocument> docs = dishes.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
            
            esTemplate.save(docs);
            
            log.info("同步进度: page={}, count={}", pageNum, dishes.size());
            pageNum++;
        }
        
        log.info("全量同步完成");
    }
}
```

### 5.3 优缺点

**优点**:
- 实现简单
- 可以修复数据不一致问题
- 适合初始化数据

**缺点**:
- 实时性差
- 资源消耗大(全表扫描)
- 不适合频繁变更的数据

---

## 六、方案五：Logstash 同步

### 6.1 原理

使用 Logstash 的 JDBC Input 插件定时同步 MySQL 数据到 ES。

### 6.2 配置示例

```conf
# logstash-dish-sync.conf

input {
  jdbc {
    jdbc_driver_library => "/path/to/mysql-connector-java.jar"
    jdbc_driver_class => "com.mysql.cj.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://localhost:3306/restaurant"
    jdbc_user => "root"
    jdbc_password => "password"
    
    # 定时执行(每 5 分钟)
    schedule => "*/5 * * * *"
    
    # SQL 查询
    statement => "SELECT * FROM t_dish WHERE update_time > :sql_last_value"
    
    # 使用 update_time 作为增量标记
    use_column_value => true
    tracking_column => "update_time"
    tracking_column_type => "timestamp"
    
    # 记录上次同步时间
    last_run_metadata_path => "/var/log/logstash/dish_sync_last_run"
  }
}

filter {
  # 数据转换
  mutate {
    rename => {
      "id" => "dishId"
      "dish_name" => "dishName"
      "dish_price" => "dishPrice"
    }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "dish"
    document_id => "%{dishId}"
  }
  
  stdout {
    codec => rubydebug
  }
}
```

### 6.3 优缺点

**优点**:
- 配置简单,无需编码
- 支持增量同步
- 适合初始化和定时同步

**缺点**:
- 实时性差
- 需要额外部署 Logstash
- 灵活性不如代码实现

---

## 七、最终一致性保证机制

### 7.1 数据校验

定期校验 MySQL 和 ES 数据是否一致。

```java
@Component
public class DataConsistencyChecker {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    /**
     * 每天凌晨 2 点执行数据校验
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkConsistency() {
        log.info("开始数据一致性校验");
        
        List<Long> inconsistentIds = new ArrayList<>();
        
        // 分页查询 MySQL 数据
        int pageSize = 1000;
        int pageNum = 1;
        
        while (true) {
            List<Dish> dishes = dishMapper.selectPage(pageNum, pageSize);
            if (dishes.isEmpty()) {
                break;
            }
            
            for (Dish dish : dishes) {
                // 查询 ES 中的数据
                DishDocument esDoc = esTemplate.get(
                    dish.getId().toString(),
                    DishDocument.class
                );
                
                // 比较数据是否一致
                if (!isConsistent(dish, esDoc)) {
                    inconsistentIds.add(dish.getId());
                }
            }
            
            pageNum++;
        }
        
        // 修复不一致的数据
        if (!inconsistentIds.isEmpty()) {
            log.warn("发现不一致数据: count={}", inconsistentIds.size());
            repairInconsistentData(inconsistentIds);
        }
        
        log.info("数据一致性校验完成");
    }
    
    private boolean isConsistent(Dish dish, DishDocument esDoc) {
        if (esDoc == null) {
            return false;
        }
        
        return Objects.equals(dish.getDishName(), esDoc.getDishName())
            && Objects.equals(dish.getDishPrice(), esDoc.getDishPrice())
            && Objects.equals(dish.getUpdateTime(), esDoc.getUpdateTime());
    }
    
    private void repairInconsistentData(List<Long> dishIds) {
        for (Long dishId : dishIds) {
            try {
                Dish dish = dishMapper.selectById(dishId);
                if (dish != null) {
                    DishDocument doc = convertToDocument(dish);
                    esTemplate.save(doc);
                    log.info("修复数据: dishId={}", dishId);
                }
            } catch (Exception e) {
                log.error("修复失败: dishId={}", dishId, e);
            }
        }
    }
}
```

### 7.2 监控告警

监控同步延迟和失败率。

```java
@Component
public class SyncMonitor {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    /**
     * 记录同步成功
     */
    public void recordSuccess(String operation) {
        meterRegistry.counter("es.sync.success", "operation", operation).increment();
    }
    
    /**
     * 记录同步失败
     */
    public void recordFailure(String operation, String reason) {
        meterRegistry.counter("es.sync.failure", 
            "operation", operation,
            "reason", reason
        ).increment();
    }
    
    /**
     * 记录同步延迟
     */
    public void recordDelay(long delayMs) {
        meterRegistry.timer("es.sync.delay").record(delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 检查同步健康状态
     */
    @Scheduled(fixedDelay = 60000)
    public void checkHealth() {
        // 获取最近 1 分钟的失败率
        double failureRate = getFailureRate();
        
        if (failureRate > 0.1) { // 失败率超过 10%
            sendAlert("ES 同步失败率过高: " + failureRate);
        }
        
        // 获取平均延迟
        double avgDelay = getAverageDelay();
        
        if (avgDelay > 5000) { // 延迟超过 5 秒
            sendAlert("ES 同步延迟过高: " + avgDelay + "ms");
        }
    }
}
```

---

## 八、方案选择建议

### 8.1 小型项目(数据量 < 10 万)

**推荐方案**: 同步双写 + 失败补偿

**理由**:
- 实现简单,开发快
- 数据量小,性能够用
- 维护成本低

### 8.2 中型项目(数据量 10 万 - 100 万)

**推荐方案**: 异步双写(MQ)

**理由**:
- 性能好,不阻塞主流程
- 可靠性高,MQ 保证消息不丢
- 复杂度适中

### 8.3 大型项目(数据量 > 100 万)

**推荐方案**: Canal 监听 binlog

**理由**:
- 完全解耦,业务无侵入
- 实时性高,可靠性强
- 适合大规模数据同步

### 8.4 对实时性要求不高

**推荐方案**: 定时任务 + Logstash

**理由**:
- 实现简单
- 资源消耗可控
- 适合报表、分析等场景

---

## 九、面试回答模板

### 问题: "如何保证 ES 和 MySQL 的最终一致性?"

**标准回答**:

> "保证 ES 和 MySQL 最终一致性有几种常见方案:
> 
> **1. 同步双写**
> - 在业务代码中同时写入 MySQL 和 ES
> - 优点是实现简单,实时性高
> - 缺点是耦合度高,ES 故障会影响主流程
> 
> **2. 异步双写(MQ)**
> - 先写 MySQL,再通过 MQ 异步同步到 ES
> - 优点是解耦,性能好,可靠性高
> - 缺点是有延迟,需要维护 MQ
> 
> **3. Canal 监听 binlog**
> - 使用 Canal 监听 MySQL binlog,自动同步到 ES
> - 优点是完全解耦,实时性高,可靠性强
> - 缺点是部署复杂,需要开启 binlog
> 
> **我们项目采用的是异步双写方案**,因为:
> - 数据量适中,MQ 能满足需求
> - 允许短暂延迟(几百毫秒)
> - 实现复杂度适中,团队能掌握
> 
> 同时我们还实现了:
> - **失败补偿机制**: 记录同步失败的数据,定时重试
> - **数据校验**: 定期校验 MySQL 和 ES 数据一致性
> - **监控告警**: 监控同步延迟和失败率,及时发现问题
> 
> 这样可以保证数据的最终一致性。"

---

## 十、总结

### 核心要点

1. **没有完美方案**: 需要根据项目规模、团队能力、业务需求选择
2. **最终一致性**: ES 和 MySQL 允许短暂不一致,但最终会一致
3. **补偿机制**: 必须有失败重试和数据修复机制
4. **监控告警**: 及时发现和处理同步问题

### 技术选型

- **小项目**: 同步双写
- **中项目**: 异步双写(MQ)
- **大项目**: Canal 监听 binlog
- **低实时性**: 定时任务 + Logstash

### 面试加分项

- 能说出多种方案并对比优缺点
- 能根据场景选择合适方案
- 了解失败补偿和数据校验机制
- 展示实际项目经验
