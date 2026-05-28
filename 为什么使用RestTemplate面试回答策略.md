# 为什么使用 RestTemplate - 面试回答策略

## 问题背景

面试官看到你的表格显示 RestTemplate 推荐度只有 ⭐⭐，并且标注"官方已废弃维护"，但你在 AI 助手功能中却使用了 RestTemplate，可能会质疑你的技术选型能力。

---

## 标准回答模板（推荐）

### 第一层：承认现状 + 说明历史原因

**回答示例**：

> "确实，RestTemplate 在 Spring 5.0 之后就进入了维护模式，官方推荐使用 WebClient。我在项目中使用 RestTemplate 主要是基于以下几个考虑：
> 
> 1. **项目时间线**：这个功能是在项目早期开发的，当时团队对 RestTemplate 更熟悉，为了快速上线选择了它
> 2. **功能稳定性**：RestTemplate 虽然不再新增功能，但非常稳定，对于简单的 HTTP 调用完全够用
> 3. **团队技术栈**：项目使用 Spring Boot 3.1.5，但团队成员对同步阻塞式编程更熟悉，RestTemplate 的学习成本更低"

### 第二层：展示技术认知

**回答示例**：

> "不过我也意识到这不是最佳实践。如果现在重新设计，我会选择：
> 
> - **首选方案**：阿里云通义千问官方 Java SDK（最稳定、最省心）
> - **备选方案**：WebClient（Spring 官方推荐，支持响应式编程）
> - **微服务场景**：OpenFeign（声明式调用，代码更简洁）
> 
> 实际上，我已经在项目文档中标注了这个技术债，并制定了迁移计划。"

### 第三层：展示改进能力（加分项）

**回答示例**：

> "如果要迁移到 WebClient，改动其实不大。我做过技术调研，主要改动点是：
> 
> 1. 将同步调用改为响应式调用
> 2. 使用 Mono/Flux 处理响应
> 3. 调整异常处理逻辑
> 
> 预计 2-3 天就能完成迁移，而且可以获得更好的性能和非阻塞特性。"

---

## 深度回答（如果面试官继续追问）

### Q1: RestTemplate 有什么具体问题？

**回答要点**：

1. **阻塞式 I/O**：每个请求占用一个线程，高并发场景下线程资源浪费
2. **不支持响应式**：无法利用 Spring WebFlux 的非阻塞特性
3. **维护模式**：不再新增功能，未来可能完全废弃
4. **性能瓶颈**：在高并发场景下，性能不如 WebClient

**示例**：

```java
// RestTemplate - 阻塞式
String response = restTemplate.postForObject(url, request, String.class);
// 线程在这里等待响应，无法处理其他请求

// WebClient - 非阻塞式
Mono<String> response = webClient.post()
    .uri(url)
    .bodyValue(request)
    .retrieve()
    .bodyToMono(String.class);
// 线程可以继续处理其他请求，响应到达时通过回调处理
```

### Q2: 为什么不一开始就用 WebClient？

**诚实回答**：

> "这是一个技术决策的权衡问题：
> 
> **当时的考虑**：
> - AI 助手功能不是高并发场景（平均 QPS < 10）
> - 团队对同步编程更熟悉，开发效率更高
> - 项目时间紧，需要快速上线
> 
> **现在的反思**：
> - 应该从一开始就使用更现代的技术
> - 技术债会越积越多，迁移成本越来越高
> - 这是一个很好的教训，让我更重视技术选型的前瞻性"

### Q3: 如果让你现在迁移，你会怎么做？

**展示技术能力**：

> "我会分三步走：
> 
> **第一步：引入依赖**
> ```xml
> <dependency>
>     <groupId>org.springframework.boot</groupId>
>     <artifactId>spring-boot-starter-webflux</artifactId>
> </dependency>
> ```
> 
> **第二步：创建 WebClient Bean**
> ```java
> @Configuration
> public class WebClientConfig {
>     @Bean
>     public WebClient webClient() {
>         return WebClient.builder()
>             .baseUrl("https://dashscope.aliyuncs.com")
>             .defaultHeader("Authorization", "Bearer " + apiKey)
>             .defaultHeader("Content-Type", "application/json")
>             .build();
>     }
> }
> ```
> 
> **第三步：改造调用代码**
> ```java
> // 原代码（RestTemplate）
> public String chat(String message) {
>     HttpHeaders headers = new HttpHeaders();
>     headers.set("Authorization", "Bearer " + apiKey);
>     HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
>     
>     ResponseEntity<String> response = restTemplate.postForEntity(
>         url, entity, String.class);
>     return response.getBody();
> }
> 
> // 新代码（WebClient）
> public Mono<String> chat(String message) {
>     return webClient.post()
>         .uri("/api/v1/services/aigc/text-generation/generation")
>         .bodyValue(request)
>         .retrieve()
>         .bodyToMono(String.class);
> }
> ```
> 
> **第四步：兼容性处理**
> - 如果上层代码不支持响应式，可以用 `.block()` 转为同步
> - 逐步改造上层代码，最终实现全链路响应式"

---

## 最佳实践对比表格

| 维度 | RestTemplate | WebClient | 阿里云 SDK |
|------|-------------|-----------|-----------|
| **推荐度** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **编程模型** | 同步阻塞 | 响应式非阻塞 | 同步阻塞 |
| **性能** | 中等 | 高 | 高 |
| **学习成本** | 低 | 中等 | 低 |
| **维护状态** | 维护模式 | 活跃开发 | 活跃开发 |
| **适用场景** | 简单调用、老项目 | 高并发、响应式项目 | 生产环境、长期项目 |
| **代码复杂度** | 简单 | 中等 | 简单 |
| **错误处理** | 简单 | 复杂 | 简单 |
| **官方支持** | 不推荐 | 推荐 | 官方 SDK |

---

## 技术选型决策树

```
是否是阿里云服务？
├─ 是 → 优先使用官方 SDK
│   └─ 阿里云通义千问 SDK
│
└─ 否 → 是否是 Spring Boot 项目？
    ├─ 是 → 是否需要高并发/响应式？
    │   ├─ 是 → WebClient
    │   └─ 否 → 是否是微服务？
    │       ├─ 是 → OpenFeign
    │       └─ 否 → WebClient（推荐）或 RestTemplate（快速开发）
    │
    └─ 否 → OkHttp 或 Apache HttpClient
```

---

## 实际项目中的改进计划

### 短期计划（1-2 周）

1. **评估影响范围**
   - 统计 RestTemplate 的使用位置
   - 评估迁移工作量
   - 制定迁移优先级

2. **引入阿里云 SDK**
   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-ai</artifactId>
   </dependency>
   ```

3. **编写迁移文档**
   - 记录迁移步骤
   - 编写测试用例
   - 制定回滚方案

### 中期计划（1 个月）

1. **逐步迁移**
   - 先迁移 AI 助手功能（影响最小）
   - 再迁移其他 HTTP 调用
   - 保留 RestTemplate 作为降级方案

2. **性能测试**
   - 对比迁移前后的性能
   - 监控错误率和响应时间
   - 收集用户反馈

3. **代码审查**
   - 团队 Code Review
   - 更新技术文档
   - 分享迁移经验

### 长期计划（3 个月）

1. **完全移除 RestTemplate**
   - 删除相关依赖
   - 更新项目文档
   - 培训团队成员

2. **建立技术规范**
   - 制定 HTTP 调用规范
   - 统一技术选型标准
   - 避免类似技术债

---

## 面试回答的注意事项

### ✅ 应该做的

1. **承认不足**：坦诚说明 RestTemplate 不是最佳选择
2. **解释原因**：说明当时的技术决策背景
3. **展示认知**：证明你了解更好的方案
4. **提出改进**：展示你的技术改进能力

### ❌ 不应该做的

1. **强行辩解**：不要说"RestTemplate 很好用，没必要换"
2. **推卸责任**：不要说"是别人写的，我只是维护"
3. **不懂装懂**：不要说"WebClient 我很熟"（如果不熟）
4. **贬低其他**：不要说"WebClient 太复杂，没必要用"

---

## 加分回答示例

**如果面试官问："你觉得这个项目还有哪些可以改进的地方？"**

> "除了 RestTemplate 迁移，我还发现了几个可以优化的点：
> 
> 1. **AI 助手的会话管理**
>    - 当前使用 Redis 存储会话历史，但没有设置过期时间
>    - 建议：设置 24 小时过期，避免 Redis 内存占用过高
> 
> 2. **Function Calling 的扩展性**
>    - 当前只支持查询菜品信息
>    - 建议：设计插件化架构，方便添加新的工具函数
> 
> 3. **错误处理**
>    - 当前 AI 调用失败时，用户体验不好
>    - 建议：添加重试机制和降级方案（返回预设回复）
> 
> 4. **性能优化**
>    - 当前每次都调用 AI API，成本较高
>    - 建议：对常见问题添加缓存，减少 API 调用次数
> 
> 这些都是我在实际使用中发现的问题，已经记录在技术债清单中。"

---

## 总结

### 核心策略

1. **诚实为上**：承认 RestTemplate 不是最佳选择
2. **展示认知**：证明你了解更好的技术方案
3. **强调改进**：展示你的学习和改进能力
4. **避免辩解**：不要强行为技术债辩护

### 关键话术

- "这是项目早期的技术选型，现在看来不是最佳实践"
- "如果重新设计，我会选择..."
- "我已经制定了迁移计划..."
- "这是一个很好的教训，让我更重视..."

### 面试官想听到的

- ✅ 你有技术判断力
- ✅ 你能承认错误
- ✅ 你有改进意识
- ✅ 你能从错误中学习

### 面试官不想听到的

- ❌ 强行辩解
- ❌ 推卸责任
- ❌ 不懂装懂
- ❌ 固执己见
