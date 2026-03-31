USE message;

CREATE TABLE IF NOT EXISTS user (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phoneNumber VARCHAR(15)  NOT NULL UNIQUE,
    createdTime DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    price       DECIMAL(12,2) NOT NULL DEFAULT 0,
    stock       INT NOT NULL DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO product (name, description, price, stock) VALUES
('秒杀商品A', '限时秒杀商品A描述', 99.00, 100),
('秒杀商品B', '限时秒杀商品B描述', 199.00, 50),
('秒杀商品C', '限时秒杀商品C描述', 299.00, 30);

CREATE TABLE IF NOT EXISTS seckill_order (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花算法订单ID',
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=CONFIRMED 2=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
) COMMENT='秒杀订单';
