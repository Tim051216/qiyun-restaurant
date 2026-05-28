# Elasticsearch 使用说明

## 项目现状

**项目中目前没有实际使用 Elasticsearch**，但在以下地方提到了 ES：

### 1. 文档中提到（技术方案参考）

- ✅ `ES与MySQL最终一致性方案.md` - 详细的技术方案文档
- ✅ `Canal相关说明.md` - 提到 Canal 可以同步数据到 ES
- ✅ `restaurant-server/技术栈升级方案-2026面试版.md` - 作为未来升级选项

### 2. SkyWalking 监控使用 ES（仅作为存储）

- 📊 `restaurant-server/monitoring/skywalking/docker-compose-skywalking.yml`
- SkyWalking 使用 Elasticsearch 8.11.0 作为链路追踪数据的存储
- 这是 SkyWalking 的内部存储，不是业务数据搜索

### 3. 代码中没有 ES 依赖

```bash
# 搜索结果：
- pom.xml 中没有 elasticsearch 依赖
- 配置文件中没有 ES 配置
- Java 代码中没有 ElasticsearchClient 或 RestHighLevelClient
```

---

## 面试回答策略

### 场景一：面试官问"项目中用了 Elasticsearch 吗？"

**标准回答（诚实为上）**：

"项目中目前没有使用 Elasticsearch。我们的搜索功能主要通过 MySQL 的模糊查询和 Redis 缓存来实现，对于餐厅点餐系统来说，数据量不大（几千条菜品数据），MySQL 的性能完全够用。

不过我研究过 ES 的使用场景，也写了一份《ES 与 MySQL 最终一致性方案》的技术文档，了解如何在需要的时候引入 ES。"

### 场景二：面试官追问"为什么不用 ES？"

**深度回答**：

"主要基于以下几点考虑：

1. **数据量不大**：餐厅菜品数据通常在几千条以内，MySQL + Redis 缓存完全能满足性能要求

2. **搜索需求简单**：主要是按菜品名称、分类查询，不需要复杂的全文搜索、分词、相关性排序

3. **技术复杂度**：引入 ES 需要考虑数据同步、一致性保证、集群维护等问题，增加系统复杂度

4. **成本考虑**：ES 需要额外的服务器资源，对于小型餐厅系统来说性价比不高

**但如果未来有以下需求，我会考虑引入 ES**：
- 菜品数据量达到百万级
- 需要复杂的全文搜索（如搜索菜品描述、食材、口味等）
- 需要智能推荐、相关性排序
- 需要实时数据分析和聚合统计"

### 场景三：面试官问"如果要加 ES，怎么做？"

**技术方案回答**：

"我会采用以下方案：

**1. 数据同步方案（推荐 MQ 异步双写）**
```
用户操作 → MySQL 写入 → 发送 MQ 消息 → ES 消费者同步到 ES
```

**优点**：
- 解耦：MySQL 和 ES 独立，互不影响
- 削峰：MQ 缓冲，保护 ES
- 可靠：消息持久化，支持重试

**2. 保证最终一致性**
- 幂等性：使用文档 ID 防止重复
- 重试机制：失败自动重试 3 次
- 补偿机制：定时任务对比 MySQL 和 ES 数据
- 监控告警：数据不一致时发送告警

**3. 技术选型**
- Spring Data Elasticsearch（简单易用）
- RabbitMQ（已有，直接复用）
- 定时任务（Spring @Scheduled）

我在项目文档中写了完整的实现方案，包括代码示例和异常处理。"

---

## 技术方案文档

你已经有一份完整的 ES 技术方案文档：

### 📄 `ES与MySQL最终一致性方案.md`

包含以下内容：
- ✅ 5 种数据同步方案对比
- ✅ 完整代码实现（同步双写、MQ 异步、Canal、定时任务、Logstash）
- ✅ 最终一致性保证机制
- ✅ 方案选择建议
- ✅ 面试回答模板

**这份文档可以直接用于面试准备！**

---

## 实际使用场景（SkyWalking）

虽然业务代码没用 ES，但项目的监控系统 SkyWalking 使用了 ES：

### SkyWalking + Elasticsearch

**用途**：存储链路追踪数据

**配置文件**：`restaurant-server/monitoring/skywalking/docker-compose-skywalking.yml`

```yaml
services:
  # Elasticsearch（SkyWalking存储）
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: skywalking-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  # SkyWalking OAP Server
  skywalking-oap:
    image: apache/skywalking-oap-server:9.6.0
    depends_on:
      - elasticsearch
    environment:
      SW_STORAGE: elasticsearch
      SW_STORAGE_ES_CLUSTER_NODES: elasticsearch:9200
```

**说明**：
- 这是 SkyWalking 的内部存储，不是业务搜索
- 用于存储分布式链路追踪数据
- 可以在面试中提到："虽然业务代码没用 ES，但我们的 APM 监控系统 SkyWalking 使用 ES 存储链路数据"

---

## 面试加分项

### 1. 展示技术深度

"虽然项目没用 ES，但我研究过 ES 的核心原理：
- 倒排索引：快速全文搜索
- 分片和副本：高可用和水平扩展
- 分词器：中文分词（IK Analyzer）
- 聚合查询：实时统计分析"

### 2. 展示技术广度

"我了解 ES 的典型应用场景：
- 电商：商品搜索、智能推荐
- 日志分析：ELK Stack（Elasticsearch + Logstash + Kibana）
- APM 监控：SkyWalking、Zipkin 的存储后端
- 内容平台：文章搜索、标签推荐"

### 3. 展示技术选型能力

"技术选型要根据实际需求：
- 数据量小、搜索简单 → MySQL 足够
- 数据量大、需要全文搜索 → Elasticsearch
- 实时性要求高 → Redis + ES
- 成本敏感 → 优先考虑 MySQL 优化"

---

## 总结

### 项目现状
- ❌ 业务代码没有使用 Elasticsearch
- ✅ SkyWalking 监控使用 ES 作为存储
- ✅ 有完整的 ES 技术方案文档

### 面试策略
1. **诚实说明**：项目没用 ES，但了解 ES 的使用场景
2. **展示准备**：研究过 ES，写了技术方案文档
3. **强调思考**：能说出为什么不用 ES，以及什么时候该用
4. **技术深度**：能讲清楚 ES 的核心原理和实现方案

### 关键文档
- 📄 `ES与MySQL最终一致性方案.md` - 完整技术方案
- 📄 `Canal相关说明.md` - 数据同步方案
- 📄 `restaurant-server/monitoring/skywalking/` - SkyWalking 配置

**这样回答既诚实，又能展示你的技术能力！**
