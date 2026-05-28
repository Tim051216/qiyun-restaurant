# Seata 演示说明

当前项目已经接入了一套最小化的 Seata AT 模式演示链路，涉及两个服务：

- `restaurant-order-service`
- `restaurant-dish-service`

## 演示目标

这套演示会完成下面这条链路：

1. `restaurant-order-service` 开启全局事务。
2. 在本地表 `seata_demo_order` 中插入一条演示订单。
3. 通过 Feign 调用 `restaurant-dish-service` 扣减菜品库存。
4. 如果请求参数里 `failAfterDeduct=true`，订单服务会主动抛出异常。
5. Seata 会把“订单插入”和“库存扣减”一起回滚。

## 准备步骤

1. 先导入演示 SQL：

```sql
SOURCE restaurant-server/sql/seata_demo.sql;
```

2. 启动 Seata Server：

```powershell
docker compose -f F:\HBuilderProjects\restaurant-server\docker\seata\docker-compose-seata.yml up -d
```

3. 启动这些服务：

- `restaurant-dish`
- `restaurant-order`
- `nacos`（如果你希望继续通过服务发现调用）

4. 确认两个服务都能访问 `127.0.0.1:8091`，或者通过环境变量正确配置 `SEATA_SERVER_ADDR`。

## 演示接口

先查看库存：

```http
GET /dish/seata/demo/stock/{dishId}
```

发起一次成功提交的分布式事务：

```http
POST /order/seata/demo/place
Content-Type: application/json

{
  "userId": 1001,
  "dishId": 1,
  "count": 1,
  "amount": 38.00,
  "failAfterDeduct": false,
  "remark": "Seata 成功提交演示"
}
```

发起一次故意失败并触发回滚的事务：

```http
POST /order/seata/demo/place
Content-Type: application/json

{
  "userId": 1001,
  "dishId": 1,
  "count": 1,
  "amount": 38.00,
  "failAfterDeduct": true,
  "remark": "Seata 回滚演示"
}
```

查看演示订单：

```http
GET /order/seata/demo/orders
```

## 预期结果

- 当 `failAfterDeduct=false` 时：
  - `seata_demo_order` 会新增一条记录
  - 菜品库存会减少

- 当 `failAfterDeduct=true` 时：
  - `seata_demo_order` 不应保留这次新插入的数据
  - 菜品库存应恢复到调用前的值
