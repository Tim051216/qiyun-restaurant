# API接口文档

## 文档说明

本文档描述七云菜馆餐饮管理系统的RESTful API接口规范。

**版本**: 2.0.0  
**更新日期**: 2026-02-09  
**Base URL**: `http://localhost:8080/api`

---

## 通用说明

### 请求头

所有需要认证的接口都需要在请求头中携带Token：

```http
Authorization: Bearer {token}
Content-Type: application/json
```

### 响应格式

所有接口统一返回JSON格式：

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**失败响应**:
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

### 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器错误 |

---

## 订单服务 API

### 1. 创建订单

**接口**: `POST /api/order/create`

**请求参数**:
```json
{
  "userId": 1001,
  "dishId": 2001,
  "quantity": 2,
  "amount": 58.00
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10001,
    "userId": 1001,
    "dishId": 2001,
    "quantity": 2,
    "amount": 58.00,
    "status": "PENDING",
    "createTime": "2026-02-09 10:30:00"
  }
}
```

### 2. 查询订单详情

**接口**: `GET /api/order/{id}`

**路径参数**:
- `id`: 订单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10001,
    "userId": 1001,
    "dishId": 2001,
    "dishName": "宫保鸡丁",
    "quantity": 2,
    "amount": 58.00,
    "status": "PAID",
    "createTime": "2026-02-09 10:30:00",
    "updateTime": "2026-02-09 10:35:00"
  }
}
```

### 3. 查询订单列表

**接口**: `GET /api/order/list`

**查询参数**:
- `userId`: 用户ID（可选）
- `status`: 订单状态（可选）
- `page`: 页码，默认1
- `size`: 每页数量，默认10

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 10001,
        "userId": 1001,
        "dishName": "宫保鸡丁",
        "quantity": 2,
        "amount": 58.00,
        "status": "PAID",
        "createTime": "2026-02-09 10:30:00"
      }
    ]
  }
}
```

### 4. 更新订单状态

**接口**: `PUT /api/order/{id}/status`

**路径参数**:
- `id`: 订单ID

**请求参数**:
```json
{
  "status": "PAID"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 5. 取消订单

**接口**: `DELETE /api/order/{id}`

**路径参数**:
- `id`: 订单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 菜品服务 API

### 1. 查询菜品列表

**接口**: `GET /api/dish/list`

**查询参数**:
- `categoryId`: 分类ID（可选）
- `keyword`: 搜索关键词（可选）
- `page`: 页码，默认1
- `size`: 每页数量，默认10

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 2001,
        "name": "宫保鸡丁",
        "categoryId": 1,
        "categoryName": "川菜",
        "price": 29.00,
        "stock": 100,
        "status": 1,
        "imageUrl": "https://example.com/dish/2001.jpg"
      }
    ]
  }
}
```

### 2. 查询菜品详情

**接口**: `GET /api/dish/{id}`

**路径参数**:
- `id`: 菜品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "name": "宫保鸡丁",
    "categoryId": 1,
    "categoryName": "川菜",
    "price": 29.00,
    "stock": 100,
    "status": 1,
    "description": "经典川菜，麻辣鲜香",
    "imageUrl": "https://example.com/dish/2001.jpg",
    "createTime": "2026-01-01 00:00:00"
  }
}
```

### 3. 创建菜品

**接口**: `POST /api/dish/create`

**请求参数**:
```json
{
  "name": "麻婆豆腐",
  "categoryId": 1,
  "price": 18.00,
  "stock": 100,
  "description": "经典川菜",
  "imageUrl": "https://example.com/dish/2002.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2002,
    "name": "麻婆豆腐",
    "categoryId": 1,
    "price": 18.00,
    "stock": 100,
    "status": 1,
    "createTime": "2026-02-09 10:30:00"
  }
}
```

### 4. 更新菜品

**接口**: `PUT /api/dish/{id}`

**路径参数**:
- `id`: 菜品ID

**请求参数**:
```json
{
  "name": "麻婆豆腐",
  "price": 20.00,
  "stock": 150,
  "description": "经典川菜，麻辣鲜香"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 5. 删除菜品

**接口**: `DELETE /api/dish/{id}`

**路径参数**:
- `id`: 菜品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 6. 批量查询菜品

**接口**: `POST /api/dish/batch`

**请求参数**:
```json
{
  "ids": [2001, 2002, 2003]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2001,
      "name": "宫保鸡丁",
      "price": 29.00
    },
    {
      "id": 2002,
      "name": "麻婆豆腐",
      "price": 18.00
    }
  ]
}
```

---

## 会员服务 API

### 1. 用户注册

**接口**: `POST /api/member/register`

**请求参数**:
```json
{
  "username": "user001",
  "password": "password123",
  "phone": "13800138000",
  "email": "user@example.com"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "user001",
    "phone": "13800138000",
    "createTime": "2026-02-09 10:30:00"
  }
}
```

### 2. 用户登录

**接口**: `POST /api/member/login`

**请求参数**:
```json
{
  "username": "user001",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1001,
    "username": "user001",
    "expireTime": "2026-02-10 10:30:00"
  }
}
```

### 3. 查询会员信息

**接口**: `GET /api/member/{id}`

**路径参数**:
- `id`: 会员ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "user001",
    "phone": "13800138000",
    "email": "user@example.com",
    "points": 1000,
    "level": "VIP",
    "createTime": "2026-01-01 00:00:00"
  }
}
```

### 4. 更新会员信息

**接口**: `PUT /api/member/{id}`

**路径参数**:
- `id`: 会员ID

**请求参数**:
```json
{
  "phone": "13900139000",
  "email": "newemail@example.com"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 5. 查询积分记录

**接口**: `GET /api/member/{id}/points`

**路径参数**:
- `id`: 会员ID

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 5001,
        "userId": 1001,
        "points": 100,
        "type": "EARN",
        "description": "订单消费",
        "createTime": "2026-02-09 10:30:00"
      }
    ]
  }
}
```

---

## 管理服务 API

### 1. 查询统计数据

**接口**: `GET /api/admin/statistics`

**查询参数**:
- `startDate`: 开始日期
- `endDate`: 结束日期

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 1000,
    "totalAmount": 50000.00,
    "totalUsers": 500,
    "avgOrderAmount": 50.00,
    "topDishes": [
      {
        "dishId": 2001,
        "dishName": "宫保鸡丁",
        "orderCount": 200
      }
    ]
  }
}
```

### 2. 查询员工列表

**接口**: `GET /api/admin/staff/list`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 3001,
        "name": "张三",
        "role": "ADMIN",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2026-01-01 00:00:00"
      }
    ]
  }
}
```

---

## 健康检查 API

### 1. 服务健康检查

**接口**: `GET /actuator/health`

**响应示例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 2. Prometheus指标

**接口**: `GET /actuator/prometheus`

**响应**: Prometheus格式的指标数据

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 1001 | 参数验证失败 |
| 1002 | 资源不存在 |
| 1003 | 资源已存在 |
| 2001 | 认证失败 |
| 2002 | Token过期 |
| 2003 | 无权限 |
| 3001 | 订单状态错误 |
| 3002 | 库存不足 |
| 3003 | 订单已取消 |
| 4001 | 用户名已存在 |
| 4002 | 密码错误 |
| 4003 | 积分不足 |
| 5001 | 系统繁忙 |
| 5002 | 服务降级 |
| 5003 | 请求限流 |

---

## 限流说明

为保护系统稳定性，部分接口实施了限流策略：

| 接口 | 限流规则 |
|------|----------|
| POST /api/order/create | 100 QPS |
| GET /api/dish/{id} | 10 QPS/商品 |
| POST /api/member/login | 10 QPS/IP |

超过限流阈值将返回429状态码。

---

## 使用示例

### cURL示例

```bash
# 创建订单
curl -X POST http://localhost:8080/api/order/create \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "dishId": 2001,
    "quantity": 2,
    "amount": 58.00
  }'

# 查询菜品列表
curl -X GET "http://localhost:8080/api/dish/list?page=1&size=10" \
  -H "Authorization: Bearer {token}"
```

### JavaScript示例

```javascript
// 创建订单
fetch('http://localhost:8080/api/order/create', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 1001,
    dishId: 2001,
    quantity: 2,
    amount: 58.00
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## 在线文档

系统集成了Knife4j，可以通过以下地址访问在线API文档：

**开发环境**: http://localhost:8080/doc.html  
**测试环境**: http://test.restaurant.example.com/doc.html

在线文档提供：
- 接口列表
- 参数说明
- 在线测试
- 响应示例

---

## 更新日志

### v2.0.0 (2026-02-09)
- 微服务架构重构
- 新增限流保护
- 优化响应格式
- 完善错误码

### v1.0.0 (2025-01-01)
- 初始版本
