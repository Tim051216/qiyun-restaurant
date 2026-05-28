# 性能压测快速开始

## 一、前置准备

### 1. 下载安装 JMeter

**下载地址**: https://jmeter.apache.org/download_jmeter.cgi

**安装步骤**:
1. 下载 `apache-jmeter-5.6.3.zip`
2. 解压到 `C:\` 目录 (推荐)
3. 确保路径为: `C:\apache-jmeter-5.6.3\`

### 2. 启动后端服务

```powershell
# 在项目根目录执行
.\启动所有服务.ps1
```

等待所有服务启动完成 (约 1-2 分钟)

---

## 二、执行压测

### 方法一: 一键执行 (推荐)

```powershell
# 进入压测目录
cd performance-test

# 执行压测脚本
.\run-test.ps1
```

脚本会自动:
- 检查 JMeter 是否安装
- 检查服务是否运行
- 执行压测
- 生成 HTML 报告
- 自动打开报告

### 方法二: 手动执行

```powershell
# 进入 JMeter 目录
cd C:\apache-jmeter-5.6.3\bin

# 执行压测
.\jmeter.bat -n -t ..\..\performance-test\dish-list-test.jmx -l result.jtl -e -o report

# 打开报告
start report\index.html
```

---

## 三、查看结果

### 1. HTML 报告 (推荐)

压测完成后会自动打开 HTML 报告,包含:

- **Dashboard**: 核心指标概览
  - APDEX: 应用性能指数
  - Requests Summary: 请求汇总
  - Statistics: 统计数据

- **Charts**: 图表分析
  - Response Times Over Time: 响应时间趋势
  - Throughput Over Time: 吞吐量趋势
  - Active Threads Over Time: 活跃线程趋势

- **Statistics Table**: 详细统计表格
  - QPS (吞吐量)
  - 平均响应时间
  - P95/P99 响应时间
  - 错误率

### 2. 关键指标说明

| 指标 | 说明 | 优秀标准 |
|------|------|----------|
| **Throughput (QPS)** | 每秒处理请求数 | > 1000 |
| **Average Response Time** | 平均响应时间 | < 100ms |
| **P95 Response Time** | 95% 请求响应时间 | < 200ms |
| **P99 Response Time** | 99% 请求响应时间 | < 500ms |
| **Error Rate** | 错误率 | 0% |

---

## 四、压测场景

### 当前配置 (dish-list-test.jmx)

- **并发用户**: 50
- **持续时间**: 60 秒
- **目标接口**: GET /api/dishes
- **预期 QPS**: 500+

### 修改压测参数

编辑 `dish-list-test.jmx` 文件,找到以下配置:

```xml
<stringProp name="ThreadGroup.num_threads">50</stringProp>  <!-- 并发用户数 -->
<stringProp name="ThreadGroup.ramp_time">10</stringProp>    <!-- 启动时间(秒) -->
<stringProp name="ThreadGroup.duration">60</stringProp>     <!-- 持续时间(秒) -->
```

**推荐配置**:

| 场景 | 并发数 | 启动时间 | 持续时间 | 预期 QPS |
|------|--------|----------|----------|----------|
| 轻量测试 | 10 | 5 | 30 | 100 |
| 正常流量 | 50 | 10 | 60 | 500 |
| 高峰流量 | 100 | 20 | 120 | 1000 |
| 压力测试 | 200 | 30 | 180 | 2000 |

---

## 五、常见问题

### Q1: 找不到 JMeter

**错误**: `未找到 JMeter 安装目录`

**解决**:
1. 确认 JMeter 已解压到 `C:\apache-jmeter-5.6.3\`
2. 或设置环境变量: `$env:JMETER_HOME = "你的JMeter路径"`

### Q2: 服务未运行

**错误**: `服务未运行或无法访问`

**解决**:
```powershell
# 启动所有服务
.\启动所有服务.ps1

# 检查服务状态
curl http://localhost:9080/api/dishes
```

### Q3: 压测结果错误率高

**原因**:
- 并发数过高,服务器承受不住
- 数据库连接池耗尽
- 内存不足

**解决**:
1. 降低并发数
2. 增加数据库连接池大小
3. 优化代码性能

### Q4: 报告无法打开

**原因**: JTL 文件格式错误或为空

**解决**:
```powershell
# 检查 JTL 文件
Get-Content performance-test\results\result-*.jtl | Select-Object -First 10

# 重新生成报告
cd C:\apache-jmeter-5.6.3\bin
.\jmeter.bat -g ..\..\..\performance-test\results\result-*.jtl -o new-report
```

---

## 六、文件说明

```
performance-test/
├── dish-list-test.jmx      # JMeter 测试计划
├── run-test.ps1             # 一键压测脚本
├── README.md                # 本文档
└── results/                 # 压测结果目录
    ├── result-*.jtl         # 原始结果数据
    └── report-*/            # HTML 报告
        └── index.html       # 报告首页
```

---

## 七、下一步

### 1. 优化性能

根据压测结果,优化系统性能:
- 添加 Redis 缓存
- 优化数据库查询
- 增加连接池大小
- 使用异步处理

### 2. 对比测试

优化后重新压测,对比结果:
```powershell
# 优化前
.\run-test.ps1

# 实施优化...

# 优化后
.\run-test.ps1
```

### 3. 生成压测报告

将压测结果整理成文档,用于面试展示:
- 优化前后 QPS 对比
- 响应时间改善
- 错误率降低
- 系统资源使用情况

---

## 八、面试准备

### 压测数据示例

**优化前**:
- QPS: 200-300
- 平均响应时间: 100ms
- P99 响应时间: 500ms
- 错误率: 0.1%

**优化后**:
- QPS: 800-1000
- 平均响应时间: 20ms
- P99 响应时间: 100ms
- 错误率: 0%

### 面试回答模板

> "我使用 JMeter 对系统进行了性能压测。测试场景是 50 并发用户,持续 60 秒,测试菜品列表查询接口。
> 
> 优化前,系统 QPS 约 200-300,平均响应时间 100ms。通过添加 Redis 缓存、优化数据库查询、使用 RabbitMQ 异步处理等优化措施,系统 QPS 提升到 800-1000,响应时间降低到 20ms 以内。
> 
> 压测过程中,我还监控了 CPU、内存、数据库连接数等系统资源,确保系统在高负载下仍然稳定运行。"

---

## 联系支持

如有问题,请查看:
- JMeter 官方文档: https://jmeter.apache.org/usermanual/index.html
- 项目主文档: `../JMeter压测实战指南.md`
