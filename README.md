# 七云菜馆

基于 Spring Cloud 的餐饮业务全栈项目，包含微服务后端、管理后台和微信小程序三端，覆盖菜品管理、订单处理、会员体系、营销活动、监控观测与 AI 智能助手等核心场景。

## 项目亮点

- 微服务拆分：基于 Spring Cloud、Nacos、OpenFeign 完成服务注册发现与服务调用，覆盖网关、订单、菜品、会员、管理等核心模块。
- 高并发与一致性：使用 Redis + Lua 实现库存扣减与一人一单校验，结合 RabbitMQ、定时补偿保障异步下单链路稳定性。
- 缓存优化：构建 Caffeine + Redis 二级缓存体系，并结合逻辑过期、布隆过滤器、随机过期时间应对缓存击穿、穿透与雪崩。
- 可观测性：接入 Prometheus、Grafana、SkyWalking，支持接口监控、链路追踪与基础告警能力。
- 智能化能力：集成通义千问，支持餐饮问答、菜品推荐和多轮对话场景。
- 多端交付：同时提供 Vue3 管理后台与 uni-app 微信小程序/H5 客户端。

## 技术栈

### 后端

- Spring Boot
- Spring Cloud
- Spring Cloud Alibaba
- Nacos
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- ShardingSphere
- Sentinel
- Docker

### 前端

- Vue 3
- Element Plus
- uni-app
- 微信小程序

### 监控与运维

- Prometheus
- Grafana
- SkyWalking

## 系统结构

```text
F:\HBuilderProjects
├── restaurant-gateway           # 网关服务
├── restaurant-order             # 订单服务
├── restaurant-dish              # 菜品服务
├── restaurant-member            # 会员服务
├── restaurant-admin-service     # 管理端后端服务
├── restaurant-admin             # 管理后台前端
├── restaurant-server            # Docker / 监控 / 中间件配置
└── 私房菜点餐项目前端模版（微信小程序+H5）  # uni-app 小程序与 H5
```

## 运行环境

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis
- RabbitMQ
- Docker Desktop

## 核心功能

### 用户端

- 微信小程序点餐
- H5 点餐
- 购物车与下单结算
- 会员中心
- 优惠券与积分
- AI 智能点餐助手

### 管理端

- 数据概览看板
- 菜品管理
- 订单管理
- 桌台管理
- 营销管理
- 会员管理

### 平台能力

- 服务网关与统一鉴权
- 缓存加速与热点防护
- 消息异步削峰
- 分布式事务兜底
- 监控告警与链路追踪

## 项目截图

### 管理后台

![管理后台](docs/images/admin-dashboard.png)

### 微信小程序

![微信小程序](docs/images/mini-home.png)

## 启动说明

### 后端服务

1. 启动 MySQL、Redis、RabbitMQ、Nacos 等依赖。
2. 启动 `restaurant-gateway`、`restaurant-order`、`restaurant-dish`、`restaurant-member`、`restaurant-admin-service`。
3. 管理后台默认访问地址：`http://localhost:8090`

### 小程序 / H5

1. 打开目录 `私房菜点餐项目前端模版（微信小程序+H5）`
2. 安装依赖并构建对应平台产物
3. 微信小程序使用 `dist/build/mp-weixin` 导入微信开发者工具

## 仓库说明

当前仓库主要用于展示七云菜馆完整项目结构、核心实现思路与多端交付效果。若用于求职展示，建议重点突出：

- 微服务拆分与工程化能力
- Redis / RabbitMQ / Sentinel 等中间件实战
- 缓存、高并发、最终一致性方案
- 管理后台 + 小程序 + 监控体系的全链路落地

## License

MIT
