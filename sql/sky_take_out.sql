-- Reconstructed from the supplied IDEA schema cache, not a live database dump.
-- Cache file ZIP timestamp: 2026-11-11 16:44 (timezone unspecified).
-- Includes only sky_take_out structure: no data, connection accounts or credentials.
-- No foreign-key metadata was present; no foreign keys have been invented here.
-- Use only in a new empty schema. Live SHOW CREATE TABLE remains authoritative.
CREATE DATABASE IF NOT EXISTS sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sky_take_out;

CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `consignee` varchar(50) NULL,
  `phone` varchar(11) NULL,
  `province_name` varchar(32) NULL,
  `city_name` varchar(32) NULL,
  `district_name` varchar(32) NULL,
  `detail` varchar(200) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

CREATE TABLE `cache_invalidation_ack` (
  `event_id` bigint NOT NULL COMMENT '缓存失效事件ID',
  `instance_id` varchar(128) NOT NULL COMMENT '消费事件的服务器实例ID',
  `processed_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '处理完成时间',
  PRIMARY KEY (`event_id`, `instance_id`),
  KEY `idx_instance_processed` (`instance_id`, `processed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='各服务器缓存失效事件消费记录表';

CREATE TABLE `cache_invalidation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `category_id` bigint NOT NULL COMMENT '需要失效的菜品分类ID',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id_id` (`category_id`, `id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='缓存失效事件表';

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int NOT NULL COMMENT '类型 1菜品分类 2套餐分类',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '修改时间',
  `create_user` bigint NULL COMMENT '创建人',
  `update_user` bigint NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

CREATE TABLE `dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '菜品名称',
  `category_id` bigint NOT NULL COMMENT '菜品分类id',
  `price` decimal(10,2) NOT NULL COMMENT '菜品价格',
  `image` varchar(255) NULL COMMENT '菜品图片',
  `description` varchar(255) NULL COMMENT '菜品描述',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 1起售 0停售',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '修改时间',
  `create_user` bigint NULL COMMENT '创建人',
  `update_user` bigint NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

CREATE TABLE `dish_flavor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品id',
  `name` varchar(32) NOT NULL COMMENT '口味名称',
  `value` varchar(255) NOT NULL COMMENT '口味数据',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品口味表';

CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `phone` varchar(11) NULL COMMENT '手机号',
  `sex` varchar(2) NULL COMMENT '性别',
  `id_number` varchar(18) NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '修改时间',
  `create_user` bigint NULL COMMENT '创建人',
  `update_user` bigint NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `name` varchar(32) NULL,
  `image` varchar(255) NULL,
  `dish_id` bigint NULL,
  `setmeal_id` bigint NULL,
  `number` int NULL,
  `amount` decimal(10,2) NULL,
  `dish_flavor` varchar(50) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `number` varchar(50) NULL,
  `status` int NOT NULL,
  `user_id` bigint NOT NULL,
  `address_book_id` bigint NULL,
  `order_time` datetime NULL,
  `checkout_time` datetime NULL,
  `pay_method` int NULL,
  `pay_status` int NULL,
  `amount` decimal(10,2) NULL,
  `remark` varchar(100) NULL,
  `phone` varchar(50) NULL,
  `address` varchar(50) NULL,
  `user_name` varchar(50) NULL,
  `consignee` varchar(50) NULL,
  `cancel_Reason` varchar(50) NULL,
  `rejection_reason` varchar(50) NULL,
  `cancel_time` datetime NULL,
  `estimated_delivery_time` datetime NULL,
  `delivery_status` int NULL,
  `delivery_time` datetime NULL,
  `pack_amount` int NULL,
  `tableware_number` int NULL,
  `tableware_status` int NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE `setmeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '套餐分类id',
  `name` varchar(32) NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '套餐价格',
  `image` varchar(255) NULL COMMENT '图片',
  `description` varchar(255) NULL COMMENT '描述',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 1起售 0停售',
  `create_time` datetime NULL,
  `update_time` datetime NULL,
  `create_user` bigint NULL,
  `update_user` bigint NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

CREATE TABLE `setmeal_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `setmeal_id` bigint NOT NULL COMMENT '套餐id',
  `dish_id` bigint NOT NULL COMMENT '菜品id',
  `copies` int NOT NULL COMMENT '菜品份数',
  `name` varchar(100) NULL,
  `price` decimal(10,2) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐菜品关系表';

CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NULL,
  `image` varchar(255) NULL,
  `user_id` bigint NOT NULL,
  `dish_id` bigint NULL,
  `setmeal_id` bigint NULL,
  `dish_flavor` varchar(50) NULL,
  `number` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `create_time` datetime NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(45) NULL COMMENT '微信openid',
  `name` varchar(32) NULL,
  `phone` varchar(11) NULL,
  `avatar` varchar(255) NULL,
  `create_time` datetime NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
