-- 测试数据库初始化脚本

-- 创建订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    table_id BIGINT COMMENT '桌号ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    actual_amount DECIMAL(10, 2) NOT NULL COMMENT '实际支付金额',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-处理中，3-已完成，4-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 插入测试数据
INSERT INTO t_order (user_id, table_id, total_amount, actual_amount, status, create_time) VALUES
(100, 1, 88.00, 88.00, 0, NOW()),
(101, 2, 128.50, 128.50, 1, NOW()),
(102, 3, 256.80, 256.80, 2, NOW());
