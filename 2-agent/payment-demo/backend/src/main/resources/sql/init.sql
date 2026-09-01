-- ========================================
-- 支付演示项目 - 数据库初始化脚本
-- 执行方式：mysql -u root -p < init.sql
-- ========================================

CREATE DATABASE IF NOT EXISTS payment_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE payment_demo;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no     VARCHAR(64)  NOT NULL COMMENT '订单号',
    product_name VARCHAR(256) NOT NULL COMMENT '商品名称',
    amount       DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    pay_type     VARCHAR(20)  NOT NULL COMMENT '支付方式: ALIPAY/WECHAT',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING/PAID/FAILED/CLOSED',
    trade_no     VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    user_id      VARCHAR(64)  DEFAULT 'guest' COMMENT '用户标识',
    create_time  DATETIME     NOT NULL COMMENT '创建时间',
    pay_time     DATETIME     DEFAULT NULL COMMENT '支付时间',
    update_time  DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 商品表（演示用）
CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(256)  NOT NULL COMMENT '商品名称',
    description VARCHAR(1024) DEFAULT NULL COMMENT '商品描述',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    image_url   VARCHAR(512)  DEFAULT NULL COMMENT '图片URL',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1上架 0下架',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 插入演示商品
INSERT INTO t_product (name, description, price, image_url, status) VALUES
('Java编程思想（第4版）', 'Java经典教材，适合有一定基础的开发者深入学习和参考', 99.00, 'https://placehold.co/200x200?text=Java+Book', 1),
('机械键盘 K8 Pro', '热插拔机械键盘，RGB背光，蓝牙/有线双模', 369.00, 'https://placehold.co/200x200?text=Keyboard', 1),
('无线蓝牙耳机', '主动降噪，30小时续航，HiFi音质', 199.00, 'https://placehold.co/200x200?text=Earbuds', 1),
('Vue3实战开发', '从零搭建企业级Vue3+TypeScript项目', 79.00, 'https://placehold.co/200x200?text=Vue3+Book', 1),
('人体工学椅', '腰背分离设计，自适应 lumbar support', 1299.00, 'https://placehold.co/200x200?text=Chair', 1),
('咖啡机', '意式全自动咖啡机，一键萃取', 899.00, 'https://placehold.co/200x200?text=Coffee', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);
