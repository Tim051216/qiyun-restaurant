USE restaurant_db;

CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
  `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
  `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
  `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` DATETIME NOT NULL COMMENT 'create datetime',
  `log_modified` DATETIME NOT NULL COMMENT 'modify datetime',
  `ext` VARCHAR(100) DEFAULT NULL COMMENT 'reserved field',
  PRIMARY KEY (`branch_id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

CREATE TABLE IF NOT EXISTS `seata_demo_order` (
  `id` BIGINT NOT NULL COMMENT 'primary key',
  `user_id` BIGINT NOT NULL COMMENT 'demo user id',
  `dish_id` BIGINT NOT NULL COMMENT 'dish id',
  `count` INT NOT NULL COMMENT 'dish count',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'order amount',
  `status` VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'demo order status',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata demo order table';
