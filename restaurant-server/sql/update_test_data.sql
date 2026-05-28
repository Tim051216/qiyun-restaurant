-- 更新订单日期为今天和昨天
USE restaurant_db;

-- 更新订单日期为今天
UPDATE orders SET create_time = CONCAT(CURDATE(), ' 12:30:15') WHERE order_no = 'ORD20240208001';
UPDATE orders SET create_time = CONCAT(CURDATE(), ' 12:28:30') WHERE order_no = 'ORD20240208002';
UPDATE orders SET create_time = CONCAT(CURDATE(), ' 12:25:10') WHERE order_no = 'ORD20240208003';
UPDATE orders SET create_time = CONCAT(CURDATE(), ' 12:20:00') WHERE order_no = 'ORD20240208004';
UPDATE orders SET create_time = CONCAT(CURDATE(), ' 12:15:00') WHERE order_no = 'ORD20240208005';

-- 更新订单日期为昨天
UPDATE orders SET create_time = CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:30:00') WHERE order_no = 'ORD20240207001';
UPDATE orders SET create_time = CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 19:00:00') WHERE order_no = 'ORD20240207002';
UPDATE orders SET create_time = CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 20:15:00') WHERE order_no = 'ORD20240207003';

-- 更新菜品销量（给前10个菜品添加销量）
UPDATE dish SET sales = 235 WHERE id = 1;  -- 宫保鸡丁
UPDATE dish SET sales = 198 WHERE id = 2;  -- 麻婆豆腐
UPDATE dish SET sales = 186 WHERE id = 5;  -- 水煮鱼
UPDATE dish SET sales = 165 WHERE id = 8;  -- 红烧肉
UPDATE dish SET sales = 142 WHERE id = 58; -- 麻辣小龙虾
UPDATE dish SET sales = 128 WHERE id = 3;  -- 辣子鸡
UPDATE dish SET sales = 115 WHERE id = 6;  -- 毛血旺
UPDATE dish SET sales = 98 WHERE id = 10;  -- 回锅肉
UPDATE dish SET sales = 87 WHERE id = 15;  -- 青椒肉丝
UPDATE dish SET sales = 76 WHERE id = 20;  -- 西红柿炒蛋

-- 更新会员注册时间
UPDATE member SET create_time = CONCAT(CURDATE(), ' 10:30:00') WHERE id = 1;
UPDATE member SET create_time = CONCAT(CURDATE(), ' 11:20:00') WHERE id = 2;
UPDATE member SET create_time = CONCAT(CURDATE(), ' 14:45:00') WHERE id = 3;
