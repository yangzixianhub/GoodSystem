
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price       DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '价格',
    stock       INT NOT NULL DEFAULT 0 COMMENT '库存',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO product (name, description, price, stock) VALUES
('秒杀商品A', '限时秒杀商品A描述', 99.00, 100),
('秒杀商品B', '限时秒杀商品B描述', 199.00, 50),
('秒杀商品C', '限时秒杀商品C描述', 299.00, 30);
