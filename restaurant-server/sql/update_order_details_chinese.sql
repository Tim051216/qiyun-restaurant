-- Update order details with Chinese dish names
USE restaurant_db;

-- Clear and reinsert order details
DELETE FROM order_detail;

-- Order 2 details (cooking)
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(2, 5, '水煮鱼', 88.00, 1, 88.00),
(2, 8, '红烧肉', 58.00, 1, 58.00),
(2, 52, '米饭', 3.00, 4, 12.00);

-- Order 3 details (pending)
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(3, 44, '烤羊肉串', 5.00, 10, 50.00),
(3, 45, '烤鸡翅', 8.00, 5, 40.00),
(3, 63, '可口可乐', 8.00, 3, 24.00);

-- Order 5 details (cooking)
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(5, 10, '酸菜鱼', 68.00, 1, 68.00),
(5, 52, '米饭', 3.00, 2, 6.00);

-- Order 1 details
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(1, 1, '麻婆豆腐', 38.00, 1, 38.00),
(1, 2, '宫保鸡丁', 28.00, 1, 28.00),
(1, 52, '米饭', 3.00, 2, 6.00);

-- Order 4 details
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(4, 3, '鱼香肉丝', 42.00, 1, 42.00),
(4, 4, '回锅肉', 48.00, 1, 48.00),
(4, 52, '米饭', 3.00, 2, 6.00);

-- Order 6 details
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(6, 15, '清蒸鲈鱼', 88.00, 1, 88.00),
(6, 20, '蒜蓉西兰花', 28.00, 1, 28.00),
(6, 52, '米饭', 3.00, 4, 12.00);

-- Order 7 details
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(7, 25, '小龙虾', 128.00, 1, 128.00),
(7, 30, '蒜蓉龙虾', 158.00, 1, 158.00),
(7, 63, '可口可乐', 8.00, 4, 32.00);

-- Order 8 details
INSERT INTO `order_detail` (`order_id`, `dish_id`, `dish_name`, `price`, `quantity`, `amount`) VALUES 
(8, 12, '干煸芸豆', 32.00, 1, 32.00),
(8, 18, '酸辣土豆丝', 18.00, 1, 18.00),
(8, 52, '米饭', 3.00, 2, 6.00);
