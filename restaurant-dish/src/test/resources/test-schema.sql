-- 测试数据库初始化脚本

-- 创建菜品表
CREATE TABLE IF NOT EXISTS t_dish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    description VARCHAR(500) COMMENT '菜品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '菜品价格',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    image_url VARCHAR(500) COMMENT '图片URL',
    stock INT DEFAULT 0 COMMENT '库存数量',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-停售，1-在售',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 插入测试数据
INSERT INTO t_dish (name, description, price, category_id, status, create_time) VALUES
('宫保鸡丁', '经典川菜', 38.00, 1, 1, NOW()),
('鱼香肉丝', '传统川菜', 32.00, 1, 1, NOW()),
('麻婆豆腐', '麻辣豆腐', 28.00, 1, 1, NOW()),
('西湖醋鱼', '杭州名菜', 88.00, 2, 1, NOW()),
('东坡肉', '红烧肉', 68.00, 2, 1, NOW());
