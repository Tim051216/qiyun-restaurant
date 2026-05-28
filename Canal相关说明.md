# Canal 相关说明

## 项目现状

### ❌ 项目中没有实际使用 Canal

经过代码搜索确认:
- ✅ 文档中提到了 Canal (作为技术方案参考)
- ❌ pom.xml 中没有 Canal 依赖
- ❌ 代码中没有 Canal 实现
- ❌ 配置文件中没有 Canal 配置

### 📄 Canal 在文档中的位置

**文件**: `restaurant-server/Redis高并发解决方案.md`

**内容**: 在"如何保证 Redis 和数据库的一致性"部分,列举了 4 种方案:
1. 先更新数据库,再删除缓存
2. 使用延迟双删
3. **使用 Canal 监听 binlog** ← 仅作为方案提及
4. 使用分布式事务(Seata)

---

## 面试中如何回答

### 场景一: 面试官问"你们项目用了 Canal 吗?"

**诚实回答(推荐)**:

> "我们项目没有使用 Canal。在保证 Redis 和数据库一致性方面,我们采用的是'先更新数据库,再删除缓存'的策略。
> 
> 不过我了解 Canal 的原理和使用场景。Canal 是阿里开源的 MySQL binlog 增量订阅和消费组件,可以监听数据库变更,实时同步到 Redis 或其他系统,保证数据一致性。
> 
> 如果项目需要更强的数据一致性保证,或者需要实现数据同步、实时计算等场景,Canal 是一个很好的选择。"

### 场景二: 面试官问"你了解 Canal 吗?"

**展示技术认知**:

> "了解。Canal 主要用于以下场景:
> 
> **1. 数据同步**
> - MySQL → Redis 实时同步
> - MySQL → Elasticsearch 实时同步
> - 主从数据库同步
> 
> **2. 缓存一致性**
> - 监听数据库变更,自动更新或删除缓存
> - 比手动维护更可靠
> 
> **3. 实时计算**
> - 监听订单表,实时统计销售额
> - 监听用户行为,实时推荐
> 
> **工作原理**:
> 1. Canal 伪装成 MySQL 的 slave
> 2. 订阅 MySQL 的 binlog
> 3. 解析 binlog 事件
> 4. 推送给消费者处理
> 
> 虽然我们项目没用,但我知道如何集成和使用。"

### 场景三: 面试官追问"为什么不用 Canal?"

**合理解释**:

> "主要基于以下考虑:
> 
> **1. 项目规模**
> - 我们是中小型项目,数据量不大
> - 简单的缓存策略就能满足需求
> 
> **2. 技术复杂度**
> - Canal 需要额外部署和维护
> - 需要配置 MySQL binlog
> - 增加了系统复杂度
> 
> **3. 团队熟悉度**
> - 团队对 Canal 不够熟悉
> - 学习和维护成本较高
> 
> **4. 成本考虑**
> - 需要额外的服务器资源
> - 增加运维成本
> 
> 如果项目发展到一定规模,数据一致性要求更高,我们会考虑引入 Canal。"

---

## Canal 技术详解(面试准备)

### 1. Canal 是什么?

Canal 是阿里巴巴开源的 MySQL binlog 增量订阅和消费组件。

**核心功能**:
- 监听 MySQL 数据库的 binlog
- 解析 binlog 事件(INSERT、UPDATE、DELETE)
- 将变更推送给消费者

### 2. Canal 工作原理

```
MySQL Master
    ↓ (binlog)
Canal Server (伪装成 slave)
    ↓ (解析)
Canal Client (消费者)
    ↓ (处理)
Redis / ES / MQ
```

**详细流程**:
1. Canal 模拟 MySQL slave 的交互协议
2. 向 MySQL master 发送 dump 请求
3. MySQL master 推送 binlog 给 Canal
4. Canal 解析 binlog 内容
5. Canal 将解析结果推送给客户端

### 3. 使用场景

#### 场景一: 缓存同步

```java
// 监听用户表变更
@CanalListener(destination = "example", schema = "restaurant", table = "t_user")
public class UserCacheSync {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @InsertListenPoint
    public void onInsert(User user) {
        // 用户新增,写入缓存
        redisTemplate.opsForValue().set("user:" + user.getId(), user);
    }
    
    @UpdateListenPoint
    public void onUpdate(User user) {
        // 用户更新,删除缓存
        redisTemplate.delete("user:" + user.getId());
    }
    
    @DeleteListenPoint
    public void onDelete(User user) {
        // 用户删除,删除缓存
        redisTemplate.delete("user:" + user.getId());
    }
}
```

#### 场景二: 数据同步到 Elasticsearch

```java
@CanalListener(destination = "example", schema = "restaurant", table = "t_dish")
public class DishSearchSync {
    
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    @InsertListenPoint
    public void onInsert(Dish dish) {
        // 同步到 ES
        esTemplate.save(dish);
    }
    
    @UpdateListenPoint
    public void onUpdate(Dish dish) {
        // 更新 ES
        esTemplate.save(dish);
    }
    
    @DeleteListenPoint
    public void onDelete(Dish dish) {
        // 从 ES 删除
        esTemplate.delete(dish.getId(), Dish.class);
    }
}
```

#### 场景三: 实时统计

```java
@CanalListener(destination = "example", schema = "restaurant", table = "t_order")
public class OrderStatistics {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @InsertListenPoint
    public void onInsert(Order order) {
        // 实时统计今日订单数
        String key = "stats:order:count:" + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        
        // 实时统计今日销售额
        String amountKey = "stats:order:amount:" + LocalDate.now();
        redisTemplate.opsForValue().increment(amountKey, order.getTotalAmount().doubleValue());
    }
}
```

### 4. 集成步骤

#### 步骤一: 添加依赖

```xml
<dependency>
    <groupId>com.alibaba.otter</groupId>
    <artifactId>canal.client</artifactId>
    <version>1.1.7</version>
</dependency>

<!-- 或使用 Spring Boot Starter -->
<dependency>
    <groupId>top.javatool</groupId>
    <artifactId>canal-spring-boot-starter</artifactId>
    <version>1.2.1-RELEASE</version>
</dependency>
```

#### 步骤二: 配置 MySQL

```sql
-- 开启 binlog
SET GLOBAL binlog_format = 'ROW';
SET GLOBAL binlog_row_image = 'FULL';

-- 创建 Canal 用户
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;
```

#### 步骤三: 部署 Canal Server

```bash
# 下载 Canal
wget https://github.com/alibaba/canal/releases/download/canal-1.1.7/canal.deployer-1.1.7.tar.gz

# 解压
tar -zxvf canal.deployer-1.1.7.tar.gz

# 配置 conf/example/instance.properties
canal.instance.master.address=127.0.0.1:3306
canal.instance.dbUsername=canal
canal.instance.dbPassword=canal
canal.instance.filter.regex=restaurant\\..*

# 启动
sh bin/startup.sh
```

#### 步骤四: 编写客户端代码

```java
@Configuration
public class CanalConfig {
    
    @Bean
    public CanalConnector canalConnector() {
        CanalConnector connector = CanalConnectors.newSingleConnector(
            new InetSocketAddress("127.0.0.1", 11111),
            "example",
            "",
            ""
        );
        return connector;
    }
}

@Component
public class CanalClient {
    
    @Autowired
    private CanalConnector connector;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @PostConstruct
    public void start() {
        new Thread(() -> {
            connector.connect();
            connector.subscribe("restaurant\\..*");
            
            while (true) {
                Message message = connector.getWithoutAck(100);
                long batchId = message.getId();
                
                if (batchId != -1) {
                    List<Entry> entries = message.getEntries();
                    for (Entry entry : entries) {
                        if (entry.getEntryType() == EntryType.ROWDATA) {
                            handleRowData(entry);
                        }
                    }
                    connector.ack(batchId);
                }
            }
        }).start();
    }
    
    private void handleRowData(Entry entry) {
        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        EventType eventType = rowChange.getEventType();
        String tableName = entry.getHeader().getTableName();
        
        for (RowData rowData : rowChange.getRowDatasList()) {
            if (eventType == EventType.INSERT || eventType == EventType.UPDATE) {
                // 删除缓存
                String id = getColumnValue(rowData.getAfterColumnsList(), "id");
                redisTemplate.delete(tableName + ":" + id);
            }
        }
    }
}
```

### 5. 优缺点分析

#### 优点

1. **实时性高**: 毫秒级延迟
2. **可靠性强**: 基于 binlog,不会丢失数据
3. **解耦**: 业务代码无需关心缓存更新
4. **通用性**: 可用于多种场景(缓存、搜索、统计)

#### 缺点

1. **复杂度高**: 需要额外部署和维护
2. **资源消耗**: 需要额外的服务器和网络资源
3. **学习成本**: 需要了解 binlog、Canal 原理
4. **依赖 MySQL**: 必须开启 binlog,影响性能

### 6. 替代方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **手动删除缓存** | 简单、直接 | 容易遗漏、不可靠 | 小型项目 |
| **延迟双删** | 较可靠、实现简单 | 有延迟、可能不一致 | 中小型项目 |
| **Canal** | 实时、可靠、解耦 | 复杂、资源消耗大 | 大型项目 |
| **分布式事务** | 强一致性 | 性能差、复杂度高 | 金融等强一致场景 |

---

## 如果要在项目中加入 Canal

### 适合加入的场景

1. **数据量大**: 日订单量 > 10 万
2. **一致性要求高**: 缓存必须实时更新
3. **多系统同步**: 需要同步到 ES、MQ 等
4. **实时计算**: 需要实时统计分析

### 不适合加入的场景

1. **小型项目**: 数据量小,简单方案够用
2. **团队不熟悉**: 学习和维护成本高
3. **资源有限**: 没有额外服务器
4. **一致性要求不高**: 允许短暂不一致

---

## 总结

### 项目现状
- ❌ 没有使用 Canal
- ✅ 使用简单的缓存策略(先更新数据库,再删除缓存)
- ✅ 适合当前项目规模

### 面试策略
1. **诚实说明**: 项目没用 Canal
2. **展示认知**: 了解 Canal 原理和使用场景
3. **合理解释**: 说明为什么不用(项目规模、复杂度、成本)
4. **表达意愿**: 如果项目需要,愿意学习和使用

### 加分项
- 能说出 Canal 的工作原理
- 能对比不同方案的优缺点
- 能根据场景选择合适的方案
- 展示技术视野和学习能力
