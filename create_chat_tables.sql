-- ========================================
-- Create missing tables for chat system
-- ========================================

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `buyer_id` bigint DEFAULT NULL COMMENT '买家ID',
  `seller_id` bigint DEFAULT NULL COMMENT '卖家ID',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
  `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `buyer_unread` int DEFAULT '0' COMMENT '买家未读数',
  `seller_unread` int DEFAULT '0' COMMENT '卖家未读数',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint DEFAULT NULL COMMENT '对话ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `content` text COMMENT '消息内容',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
