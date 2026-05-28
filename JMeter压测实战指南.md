# JMeter 压测实战指南

## 一、JMeter 安装与配置

### 1.1 下载安装

**下载地址**：https://jmeter.apache.org/download_jmeter.cgi

**Windows 安装步骤**：
```powershell
# 1. 下载 apache-jmeter-5.6.3.zip
# 2. 解压到任意目录，例如：C:\apache-jmeter-5.6.3
# 3. 配置环境变量（可选）
$env:JMETER_HOME = "C:\apache-jmeter-5.6.3"
$env:PATH += ";$env:JMETER_HOME\bin"

# 4. 启动 JMeter
cd C:\apache-jmeter-5.6.3\bin
.\jmeter.bat
```

### 1.2 中文界面配置

**方法一：临时切换**
- 启动 JMeter 后
- Options → Choose Language → Chinese (Simplified)

**方法二：永久配置**
- 编辑 `bin/jmeter.properties`
- 找到 `#language=en`
- 改为 `language=zh_CN`

---

## 二、创建压测计划

### 2.1 测试场景：菜品列表查询

我们以测试"菜品列表查询"接口为例，这是一个典型的读多写少场景。

**接口信息**：
- URL: `http://localhost:9080/api/dishes`
- 方法: GET
- 预期响应时间: < 100ms
- 目标 QPS: 1000

### 2.2 创建测试计划步骤

#### 步骤 1：创建线程组

1. 右键点击"测试计划" → 添加 → 线程（用户）→ 线程组
2. 配置参数：
   - **线程数（用户数）**：100
   - **Ramp-Up 时间（秒）**：10（10 秒内启动 100 个线程）
   - **循环次数**：100（每个线程执行 100 次请求）
   - **总请求数**：100 × 100 = 10,000 次

#### 步骤 2：添加 HTTP 请求

1. 右键点击"线程组" → 添加 → 取样器 → HTTP 请求
2. 配置参数：
   - **协议**：http
   - **服务器名称或 IP**：localhost
   - **端口号**：9080
   - **HTTP 请求方法**：GET
   - **路径**：/api/dishes

#### 步骤 3：添加监听器（查看结果）

右键点击"线程组" → 添加 → 监听器 → 选择以下监听器：

1. **查看结果树**：查看每个请求的详细信息
2. **聚合报告**：查看统计数据（QPS、响应时间等）
3. **图形结果**：查看响应时间趋势图
4. **用表格查看结果**：查看所有请求的列表

#### 步骤 4：添加断言（可选）

右键点击"HTTP 请求" → 添加 → 断言 → 响应断言
- **响应代码**：200
- **响应文本**：包含 "success" 或其他预期内容

#### 步骤 5：保存测试计划

- 文件 → 保存测试计划
- 保存为：`dish-list-test.jmx`

---

## 三、执行压测

### 3.1 GUI 模式执行（开发调试用）

**步骤**：
1. 点击工具栏的"启动"按钮（绿色三角形）
2. 等待测试完成
3. 查看各个监听器的结果

**注意**：GUI 模式会消耗大量资源，不适合大规模压测。

### 3.2 命令行模式执行（推荐）

**基本命令**：
```powershell
# 进入 JMeter bin 目录
cd C:\apache-jmeter-5.6.3\bin

# 执行压测
.\jmeter.bat -n -t dish-list-test.jmx -l result.jtl -e -o report

# 参数说明：
# -n: 非 GUI 模式
# -t: 测试计划文件
# -l: 结果文件（JTL 格式）
# -e: 生成 HTML 报告
# -o: HTML 报告输出目录
```

**执行过程**：
```
Creating summariser <summary>
Created the tree successfully using dish-list-test.jmx
Starting standalone test @ 2026 Mar 05 10:00:00 CST (1709604000000)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445
summary +    100 in 00:00:01 =  100.0/s Avg:    10 Min:     5 Max:    50 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary +    900 in 00:00:09 =  100.0/s Avg:    12 Min:     5 Max:   100 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary =   1000 in 00:00:10 =  100.0/s Avg:    11 Min:     5 Max:   100 Err:     0 (0.00%)
...
Tidying up ...    @ 2026 Mar 05 10:01:40 CST (1709604100000)
... end of run
```

---

## 四、查看压测结果

### 4.1 实时查看（命令行输出）

**关键指标解读**：
```
summary +   1000 in 00:00:10 =  100.0/s Avg:    11 Min:     5 Max:   100 Err:     0 (0.00%)
         ↑        ↑            ↑         ↑        ↑        ↑         ↑
      请求数   耗时(秒)      QPS    平均响应  最小响应  最大响应   错误率
```

### 4.2 HTML 报告（推荐）

**打开报告**：
```powershell
# 报告位置：report/index.html
# 用浏览器打开
start report/index.html
```

**报告内容**：

#### 1. Dashboard（仪表盘）

显示核心指标：
- **APDEX (Application Performance Index)**：应用性能指数
  - 1.0 = 完美
  - 0.94 = 优秀
  - 0.85 = 良好
  - < 0.7 = 需要优化

- **Requests Summary**：请求汇总
  - Total: 总请求数
  - OK: 成功请求数
  - KO: 失败请求数

- **Statistics**：统计数据
  - Throughput: 吞吐量（QPS）
  - Average Response Time: 平均响应时间
  - Min/Max Response Time: 最小/最大响应时间
  - Error Rate: 错误率

#### 2. Charts（图表）

- **Over Time（随时间变化）**
  - Response Times Over Time: 响应时间趋势
  - Throughput Over Time: 吞吐量趋势
  - Active Threads Over Time: 活跃线程数趋势

- **Throughput（吞吐量）**
  - Transactions Per Second: 每秒事务数
  - Response Time Vs Request: 响应时间与请求数关系

- **Response Times（响应时间）**
  - Response Time Percentiles: 响应时间百分位数
  - Response Time Distribution: 响应时间分布

#### 3. Statistics Table（统计表格）

| Label | # Samples | Average | Min | Max | Std. Dev. | Error % | Throughput | KB/sec |
|-------|-----------|---------|-----|-----|-----------|---------|------------|--------|
| 菜品列表 | 10000 | 11ms | 5ms | 100ms | 8.5ms | 0.00% | 1000/s | 500 |

**字段说明**：
- **# Samples**: 样本数（请求总数）
- **Average**: 平均响应时间
- **Min**: 最小响应时间
- **Max**: 最大响应时间
- **Std. Dev.**: 标准差（响应时间波动）
- **Error %**: 错误率
- **Throughput**: 吞吐量（QPS）
- **KB/sec**: 每秒接收数据量

### 4.3 JTL 结果文件分析

**使用 GUI 加载 JTL 文件**：
1. 启动 JMeter GUI
2. 添加监听器（聚合报告、图形结果等）
3. 点击监听器的"浏览"按钮
4. 选择 `result.jtl` 文件
5. 查看详细数据

---

## 五、完整压测脚本示例

### 5.1 菜品列表查询压测

**文件名**：`dish-list-test.jmx`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="菜品列表查询压测">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="用户线程组">
        <intProp name="ThreadGroup.num_threads">100</intProp>
        <intProp name="ThreadGroup.ramp_time">10</intProp>
        <longProp name="ThreadGroup.duration">60</longProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="查询菜品列表">
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">9080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.path">/api/dishes</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="响应断言">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">200</stringProp>
            </collectionProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 5.2 一键压测脚本（Windows）

**文件名**：`run-performance-test.ps1`

```powershell
# JMeter 压测自动化脚本

# 配置
$JMETER_HOME = "C:\apache-jmeter-5.6.3"
$TEST_PLAN = "dish-list-test.jmx"
$RESULT_DIR = "test-results"
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"

# 创建结果目录
New-Item -ItemType Directory -Force -Path $RESULT_DIR

# 执行压测
Write-Host "开始压测..." -ForegroundColor Green
& "$JMETER_HOME\bin\jmeter.bat" `
    -n `
    -t $TEST_PLAN `
    -l "$RESULT_DIR\result-$TIMESTAMP.jtl" `
    -e `
    -o "$RESULT_DIR\report-$TIMESTAMP"

# 打开报告
Write-Host "压测完成！正在打开报告..." -ForegroundColor Green
Start-Process "$RESULT_DIR\report-$TIMESTAMP\index.html"
```

**使用方法**：
```powershell
# 执行压测
.\run-performance-test.ps1
```

---

## 六、压测场景设计

### 6.1 场景一：正常流量测试

**目标**：测试系统在正常流量下的表现

**配置**：
- 线程数：50
- Ramp-Up 时间：10 秒
- 持续时间：10 分钟
- 预期 QPS：500

**JMeter 配置**：
```
线程组：
  - 线程数：50
  - Ramp-Up 时间：10
  - 调度器：启用
  - 持续时间：600 秒
  - 循环次数：永远（勾选"永远"）
```

### 6.2 场景二：高峰流量测试

**目标**：测试系统在高峰期的表现

**配置**：
- 线程数：200
- Ramp-Up 时间：20 秒
- 持续时间：5 分钟
- 预期 QPS：2000

### 6.3 场景三：秒杀场景测试

**目标**：测试系统在极限流量下的表现

**配置**：
- 线程数：1000
- Ramp-Up 时间：5 秒
- 持续时间：1 分钟
- 预期 QPS：10000

### 6.4 场景四：压力测试（找到系统极限）

**目标**：找到系统的性能瓶颈

**方法**：逐步增加并发
```
第一轮：100 线程，持续 5 分钟
第二轮：200 线程，持续 5 分钟
第三轮：500 线程，持续 5 分钟
第四轮：1000 线程，持续 5 分钟
...
直到系统出现明显性能下降或错误率上升
```

---

## 七、监控系统资源

### 7.1 监控 Spring Boot 应用

**使用 Spring Boot Actuator**：

1. 添加依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. 配置 `application.yml`：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

3. 访问监控端点：
```
http://localhost:8081/actuator/metrics
http://localhost:8081/actuator/metrics/jvm.memory.used
http://localhost:8081/actuator/metrics/http.server.requests
```

### 7.2 监控数据库

**MySQL 性能监控**：
```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW STATUS LIKE 'Slow_queries';

-- 查看 QPS
SHOW GLOBAL STATUS LIKE 'Questions';
SHOW GLOBAL STATUS LIKE 'Uptime';
-- QPS = Questions / Uptime
```

### 7.3 监控 Redis

**Redis 性能监控**：
```bash
# 实时监控
redis-cli --stat

# 查看信息
redis-cli INFO stats
redis-cli INFO memory
redis-cli INFO cpu
```

### 7.4 监控系统资源（Windows）

**使用任务管理器**：
- 按 `Ctrl + Shift + Esc` 打开任务管理器
- 切换到"性能"选项卡
- 查看 CPU、内存、磁盘、网络使用情况

**使用 PowerShell**：
```powershell
# 查看 CPU 使用率
Get-Counter '\Processor(_Total)\% Processor Time'

# 查看内存使用
Get-Counter '\Memory\Available MBytes'

# 持续监控
while ($true) {
    $cpu = Get-Counter '\Processor(_Total)\% Processor Time'
    $mem = Get-Counter '\Memory\Available MBytes'
    Write-Host "CPU: $($cpu.CounterSamples[0].CookedValue)% | Memory: $($mem.CounterSamples[0].CookedValue) MB"
    Start-Sleep -Seconds 1
}
```

---

## 八、压测结果分析

### 8.1 性能指标解读

**QPS（每秒查询数）**：
- < 100：性能较差
- 100-500：一般
- 500-1000：良好
- 1000-5000：优秀
- > 5000：卓越

**响应时间**：
- < 100ms：优秀
- 100-500ms：良好
- 500-1000ms：一般
- > 1000ms：较差

**错误率**：
- 0%：完美
- < 0.01%：优秀
- 0.01%-0.1%：良好
- > 0.1%：需要优化

**P95/P99 响应时间**：
- P95 < 200ms：优秀
- P99 < 500ms：良好

### 8.2 性能瓶颈分析

**常见瓶颈**：

1. **数据库瓶颈**
   - 现象：响应时间随并发增加而线性增长
   - 解决：添加缓存、优化 SQL、增加数据库连接池

2. **CPU 瓶颈**
   - 现象：CPU 使用率 > 80%，响应时间增加
   - 解决：优化算法、减少计算量、增加服务器

3. **内存瓶颈**
   - 现象：内存使用率 > 90%，频繁 GC
   - 解决：增加内存、优化对象创建、调整 JVM 参数

4. **网络瓶颈**
   - 现象：网络带宽占满，响应时间增加
   - 解决：压缩响应数据、使用 CDN、增加带宽

### 8.3 优化前后对比

**对比表格**：

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| QPS | 200 | 1000 | 5 倍 |
| 平均响应时间 | 100ms | 20ms | 5 倍 |
| P95 响应时间 | 500ms | 50ms | 10 倍 |
| P99 响应时间 | 1000ms | 100ms | 10 倍 |
| 错误率 | 0.1% | 0% | 完全解决 |
| CPU 使用率 | 80% | 30% | 降低 50% |
| 数据库连接数 | 100 | 20 | 降低 80% |

---

## 九、常见问题

### Q1: JMeter 启动失败

**问题**：双击 `jmeter.bat` 后闪退

**解决**：
1. 检查 Java 是否安装：`java -version`
2. 检查 JAVA_HOME 环境变量
3. 使用命令行启动查看错误信息

### Q2: 压测时出现大量错误

**问题**：错误率 > 10%

**排查**：
1. 检查服务是否正常运行
2. 检查数据库连接池是否耗尽
3. 检查网络是否正常
4. 降低并发数重新测试

### Q3: QPS 上不去

**问题**：增加线程数，QPS 不增加

**原因**：
1. 服务器 CPU 已满
2. 数据库连接池已满
3. 网络带宽已满
4. JMeter 本身性能瓶颈

**解决**：
1. 优化代码，降低 CPU 使用
2. 增加数据库连接池大小
3. 使用分布式压测

### Q4: 如何进行分布式压测

**场景**：单台 JMeter 无法产生足够压力

**方法**：
1. 准备多台机器（主控机 + 多台压力机）
2. 配置 JMeter 远程执行
3. 在主控机上启动压测，压力机执行

---

## 十、总结

### 压测流程

1. **准备阶段**
   - 安装 JMeter
   - 确保服务正常运行
   - 准备测试数据

2. **设计阶段**
   - 确定测试场景
   - 设计测试计划
   - 配置监控

3. **执行阶段**
   - 执行压测
   - 实时监控
   - 记录数据

4. **分析阶段**
   - 查看 HTML 报告
   - 分析性能瓶颈
   - 提出优化方案

5. **优化阶段**
   - 实施优化
   - 重新压测
   - 对比结果

### 关键要点

- 使用命令行模式进行大规模压测
- 关注 QPS、响应时间、错误率三大指标
- 监控系统资源（CPU、内存、数据库）
- 逐步增加压力，找到系统极限
- 优化后重新压测，验证效果
