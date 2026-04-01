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
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=WAIT_PAY 2=PAID 3=FAILED 4=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
) COMMENT='秒杀订单';

CREATE TABLE IF NOT EXISTS local_tx_event (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    order_id        BIGINT,
    user_id         BIGINT,
    product_id      BIGINT,
    quantity        INT DEFAULT 1,
    amount          DECIMAL(12,2),
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0=NEW 1=SENT',
    retry_count     INT NOT NULL DEFAULT 0,
    next_retry_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_id (event_id),
    KEY idx_event_status_retry (status, next_retry_time)
) COMMENT='本地消息表(outbox)';

CREATE TABLE IF NOT EXISTS processed_event (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id      VARCHAR(64) NOT NULL,
    consumer_name VARCHAR(64) NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_processed (event_id, consumer_name)
) COMMENT='消息去重表(inbox)';

CREATE TABLE IF NOT EXISTS payment_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    pay_status  TINYINT NOT NULL DEFAULT 1 COMMENT '1=SUCCESS',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pay_order (order_id, user_id)
) COMMENT='支付记录';
