-- Create rating table for seller ratings
CREATE TABLE IF NOT EXISTS `rating` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '评价用户ID',
  `seller_id` bigint DEFAULT NULL COMMENT '卖家用户ID',
  `transaction_id` bigint DEFAULT NULL COMMENT '关联交易ID',
  `score` int DEFAULT NULL COMMENT '评分(1-5)',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
