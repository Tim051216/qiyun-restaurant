-- 插入测试数据

USE restaurant_db;

-- 插入会员数据
INSERT INTO `member` (`openid`, `nickname`, `phone`, `level`, `points`, `balance`, `total_consume`, `order_count`, `status`) VALUES ('wx_test_001', 'Kaiyuan_Q', '13800138888', 'V7', 777, 120.00, 8500.00, 45, 1);
INSERT INTO `member` (`openid`, `nickname`, `phone`, `level`, `points`, `balance`, `total_consume`, `order_count`, `status`) VALUES ('wx_test_002', '美食达人', '13900139999', 'V5', 520, 80.00, 5200.00, 32, 1);
INSERT INTO `member` (`openid`, `nickname`, `phone`, `level`, `points`, `balance`, `total_consume`, `order_count`, `status`) VALUES ('wx_test_003', '吃货小王', '13600136666', 'V3', 280, 50.00, 2800.00, 18, 1);
INSERT INTO `member` (`openid`, `nickname`, `phone`, `level`, `points`, `balance`, `total_consume`, `order_count`, `status`) VALUES ('wx_test_004', '美味探索者', '13700137777', 'V2', 150, 30.00, 1500.00, 12, 1);
INSERT INTO `member` (`openid`, `nickname`, `phone`, `level`, `points`, `balance`, `total_consume`, `order_count`, `status`) VALUES ('wx_test_005', '美食爱好者', '13500135555', 'V1', 80, 20.00, 800.00, 6, 1);

-- 插入桌台数据
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('A01', 2, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('A02', 2, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('A03', 4, 'occupied');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('B01', 4, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('B02', 4, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('B03', 6, 'occupied');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('C01', 6, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('C02', 8, 'reserved');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('C03', 8, 'available');
INSERT INTO `dining_table` (`table_no`, `capacity`, `status`) VALUES ('D01', 10, 'available');

-- 插入订单数据
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240208001', 1, 1, 'A01', 2, 88.00, 'completed', '少辣', '2024-02-08 12:30:15');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240208002', 2, 6, 'B03', 4, 228.00, 'cooking', '', '2024-02-08 12:28:30');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240208003', 3, 7, 'C01', 3, 165.00, 'pending', '要冰啤酒', '2024-02-08 12:25:10');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240208004', 1, 1, 'A01', 2, 198.00, 'completed', '', '2024-02-08 12:20:00');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240208005', 4, 4, 'B01', 2, 78.00, 'cooking', '', '2024-02-08 12:15:00');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240207001', 2, 3, 'A03', 4, 320.00, 'completed', '', '2024-02-07 18:30:00');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240207002', 3, 6, 'B03', 6, 450.00, 'completed', '', '2024-02-07 19:00:00');
INSERT INTO `orders` (`order_no`, `member_id`, `table_id`, `table_no`, `diner_count`, `amount`, `status`, `remark`, `create_time`) VALUES ('ORD20240207003', 5, 2, 'A02', 2, 120.00, 'completed', '', '2024-02-07 20:15:00');

-- 插入订单明细数据
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (1, 1, '宫保鸡丁', 38.00, 1, 38.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (1, 2, '麻婆豆腐', 28.00, 1, 28.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (1, 52, '米饭', 3.00, 2, 6.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (2, 5, '水煮鱼', 88.00, 1, 88.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (2, 8, '红烧肉', 58.00, 1, 58.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (2, 52, '米饭', 3.00, 4, 12.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (3, 44, '烤羊肉串', 5.00, 10, 50.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (3, 45, '烤鸡翅', 8.00, 5, 40.00);
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES (3, 63, '青岛啤酒', 8.00, 3, 24.00);

-- 插入优惠券数据
INSERT INTO `coupon` (`name`, `type`, `amount`, `condition_amount`, `total`, `received`, `used`, `valid_days`, `status`) VALUES ('新人专享券', 'discount', 20.00, 50.00, 1000, 234, 156, 7, 1);
INSERT INTO `coupon` (`name`, `type`, `amount`, `condition_amount`, `total`, `received`, `used`, `valid_days`, `status`) VALUES ('周末特惠券', 'discount', 30.00, 100.00, 500, 189, 98, 3, 1);
INSERT INTO `coupon` (`name`, `type`, `amount`, `condition_amount`, `total`, `received`, `used`, `valid_days`, `status`) VALUES ('会员专享券', 'discount', 50.00, 200.00, 200, 67, 34, 15, 1);
INSERT INTO `coupon` (`name`, `type`, `amount`, `condition_amount`, `total`, `received`, `used`, `valid_days`, `status`) VALUES ('满减优惠券', 'discount', 15.00, 80.00, 800, 345, 210, 10, 1);
INSERT INTO `coupon` (`name`, `type`, `amount`, `condition_amount`, `total`, `received`, `used`, `valid_days`, `status`) VALUES ('超值优惠券', 'discount', 100.00, 500.00, 100, 23, 12, 30, 1);

-- 插入活动数据
INSERT INTO `activity` (`title`, `category`, `content`, `start_time`, `end_time`, `participants`, `status`) VALUES ('新人专享优惠', '优惠', '新用户首单立减20元，满50元可用', '2024-02-01 00:00:00', '2024-02-29 23:59:59', 234, 'ongoing');
INSERT INTO `activity` (`title`, `category`, `content`, `start_time`, `end_time`, `participants`, `status`) VALUES ('周末特惠活动', '优惠', '每周末全场8.8折，部分菜品5折起', '2024-02-01 00:00:00', '2024-02-29 23:59:59', 567, 'ongoing');
INSERT INTO `activity` (`title`, `category`, `content`, `start_time`, `end_time`, `participants`, `status`) VALUES ('会员积分翻倍', '福利', '会员消费积分翻倍，最高可得1000积分', '2024-02-01 00:00:00', '2024-02-15 23:59:59', 189, 'ongoing');
INSERT INTO `activity` (`title`, `category`, `content`, `start_time`, `end_time`, `participants`, `status`) VALUES ('免费菜品试吃', '福利', '每日前10名顾客可免费试吃新品', '2024-02-08 00:00:00', '2024-02-20 23:59:59', 45, 'ongoing');
INSERT INTO `activity` (`title`, `category`, `content`, `start_time`, `end_time`, `participants`, `status`) VALUES ('每日签到抽奖', '互动', '每日签到即可参与抽奖，100%中奖', '2024-02-01 00:00:00', '2024-02-29 23:59:59', 892, 'ongoing');
