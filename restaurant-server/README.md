# 七云菜馆后端服务

基于 Spring Boot + MyBatis Plus + MySQL + Redis 开发的餐饮管理系统后端服务。

## 技术栈

- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis
- JWT
- Knife4j (API文档)
- Druid (数据库连接池)

## 项目结构

```
restaurant-server/
├── src/main/java/com/qiyun/restaurant/
│   ├── common/           # 通用类
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── dto/              # 数据传输对象
│   ├── entity/           # 实体类
│   ├── interceptor/      # 拦截器
│   ├── mapper/           # Mapper接口
│   ├── service/          # 服务接口
│   │   └── impl/        # 服务实现
│   ├── utils/            # 工具类
│   └── vo/               # 视图对象
├── src/main/resources/
│   ├── mapper/           # MyBatis XML文件
│   └── application.yml   # 配置文件
└── sql/
    └── init.sql          # 数据库初始化脚本
```

## 环境要求

### 必需环境

1. **JDK 1.8+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Redis 5.0+**

## 快速开始

### 1. 安装 MySQL

**Windows:**
- 下载 MySQL 8.0: https://dev.mysql.com/downloads/mysql/
- 安装并设置 root 密码为 `123456`
- 启动 MySQL 服务

**验证安装:**
```bash
mysql --version
```

### 2. 安装 Redis

**Windows:**
- 下载 Redis: https://github.com/tporadowski/redis/releases
- 解压后运行 `redis-server.exe`
- 默认端口 6379

**验证安装:**
```bash
redis-cli ping
# 应该返回 PONG
```

### 3. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/restaurant-server/sql/init.sql

# 或者直接在 MySQL 客户端中执行 init.sql 文件内容
```

### 4. 修改配置文件

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为你的 MySQL 密码
  
  redis:
    host: localhost
    port: 6379
    password:  # 如果 Redis 设置了密码，在这里填写
```

### 5. 启动项目

```bash
# 方式1: 使用 Maven
mvn spring-boot:run

# 方式2: 使用 IDE (IDEA/Eclipse)
# 直接运行 RestaurantApplication.java 的 main 方法
```

启动成功后，控制台会显示：
```
========================================
七云菜馆后端服务启动成功！
接口文档地址: http://localhost:8080/doc.html
========================================
```

### 6. 访问接口文档

打开浏览器访问: http://localhost:8080/doc.html

## 默认账号

- 用户名: `admin`
- 密码: `123456`

## API 接口

### 管理员接口

- POST `/admin/login` - 管理员登录
- POST `/admin/logout` - 退出登录

### 其他接口

后续会添加：
- 订单管理接口
- 菜品管理接口
- 会员管理接口
- 桌台管理接口
- 营销管理接口
- 数据统计接口

## 配置说明

### MySQL 配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/restaurant_db
    username: root
    password: 123456
```

### Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 如果有密码就填写
    database: 0
```

### JWT 配置

```yaml
jwt:
  secret: qiyun-restaurant-secret-key-2024
  expiration: 604800  # 7天
  admin-expiration: 86400  # 管理员1天
```

## 常见问题

### 1. MySQL 连接失败

**错误:** `Communications link failure`

**解决:**
- 检查 MySQL 服务是否启动
- 检查端口 3306 是否被占用
- 检查用户名密码是否正确

### 2. Redis 连接失败

**错误:** `Unable to connect to Redis`

**解决:**
- 检查 Redis 服务是否启动
- 检查端口 6379 是否被占用
- 如果设置了密码，检查配置文件中的密码是否正确

### 3. 端口 8080 被占用

**解决:**
修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

## 开发计划

- [x] 项目基础架构搭建
- [x] 管理员登录功能
- [ ] 订单管理模块
- [ ] 菜品管理模块
- [ ] 会员管理模块
- [ ] 桌台管理模块
- [ ] 营销管理模块
- [ ] 数据统计模块
- [ ] 微信小程序对接

## 许可证

MIT License
