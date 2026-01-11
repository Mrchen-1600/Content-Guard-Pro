CREATE DATABASE IF NOT EXISTS safe_detection
    CHARACTER SET utf8mb4  -- 设定数据库字符集为utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE safe_detection;

CREATE TABLE `sensitive_words` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                   `word` varchar(255) NOT NULL COMMENT '敏感词内容',
                                   `type` varchar(50) NOT NULL COMMENT '类型: HIGH_RISK(高确信度), AMBIGUOUS(歧义/低风险)',
                                   `status` int(11) DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_word` (`word`),
                                   KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

INSERT INTO `sensitive_words` (`word`, `type`, `status`) VALUES
                                                             ('赌博', 'HIGH_RISK', 1),
                                                             ('恐怖主义', 'HIGH_RISK', 1),
                                                             ('色情', 'HIGH_RISK', 1),
                                                             ('政治', 'HIGH_RISK', 1),
                                                             ('暴力', 'HIGH_RISK', 1),
                                                             ('毒品', 'HIGH_RISK', 1),
                                                             ('非法集资', 'HIGH_RISK', 1);

INSERT INTO `sensitive_words` (`word`, `type`, `status`) VALUES
                                                             ('兼职', 'AMBIGUOUS', 1),
                                                             ('约茶', 'AMBIGUOUS', 1),
                                                             ('刷单', 'AMBIGUOUS', 1);