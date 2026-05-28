-- 创建数据库
CREATE DATABASE IF NOT EXISTS restaurant_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- 管理员表
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(32) NOT NULL COMMENT '用户名',
  `password` VARCHAR(64) NOT NULL COMMENT '密码',
  `name` VARCHAR(32) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
  `status` INT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除 1:删除 0:未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员（密码：123456，MD5加密后）
INSERT INTO `admin` (`username`, `password`, `name`, `phone`, `status`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '13800138000', 1);

-- 会员表
CREATE TABLE `member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
  `level` VARCHAR(10) DEFAULT 'V1' COMMENT '会员等级',
  `points` INT DEFAULT 0 COMMENT '积分',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
  `total_consume` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费',
  `order_count` INT DEFAULT 0 COMMENT '订单数',
  `status` INT DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 菜品分类表
CREATE TABLE `dish_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` INT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 插入默认分类
INSERT INTO `dish_category` (`name`, `sort`) VALUES
('川味麻辣风', 1),
('家常小炒', 2),
('时蔬素菜', 3),
('汤品主食', 4),
('夜宵烧烤', 5),
('特色龙虾', 6),
('饮品酒水', 7);

-- 菜品表
CREATE TABLE `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '图片',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `stock` INT DEFAULT 999 COMMENT '库存',
  `status` INT DEFAULT 1 COMMENT '状态 1:上架 0:下架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 桌台表
CREATE TABLE `dining_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `table_no` VARCHAR(32) NOT NULL COMMENT '桌号',
  `capacity` INT NOT NULL COMMENT '容纳人数',
  `status` VARCHAR(20) DEFAULT 'available' COMMENT '状态 available:空闲 occupied:使用中 reserved:已预订',
  `qr_code` VARCHAR(255) DEFAULT NULL COMMENT '二维码地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_table_no` (`table_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台表';

-- 订单表
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `member_id` BIGINT NOT NULL COMMENT '会员ID',
  `table_id` BIGINT DEFAULT NULL COMMENT '桌台ID',
  `table_no` VARCHAR(32) DEFAULT NULL COMMENT '桌号',
  `diner_count` INT DEFAULT 1 COMMENT '就餐人数',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending:待接单 cooking:制作中 completed:已完成 cancelled:已取消',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 优惠券表
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '优惠券名称',
  `type` VARCHAR(20) DEFAULT 'discount' COMMENT '类型 discount:满减券',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '面额',
  `condition_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用条件金额',
  `total` INT NOT NULL COMMENT '发放总量',
  `received` INT DEFAULT 0 COMMENT '已领取数量',
  `used` INT DEFAULT 0 COMMENT '已使用数量',
  `valid_days` INT DEFAULT 7 COMMENT '有效天数',
  `status` INT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 活动表
CREATE TABLE `activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(128) NOT NULL COMMENT '活动标题',
  `category` VARCHAR(20) DEFAULT '优惠' COMMENT '分类',
  `content` TEXT COMMENT '活动内容',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '活动图片',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `participants` INT DEFAULT 0 COMMENT '参与人数',
  `status` VARCHAR(20) DEFAULT 'ongoing' COMMENT '状态 ongoing:进行中 upcoming:即将开始 ended:已结束',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';
