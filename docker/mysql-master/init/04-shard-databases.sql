-- 分片库与主库message分离
CREATE DATABASE IF NOT EXISTS message_s0;
CREATE DATABASE IF NOT EXISTS message_s1;

USE message_s0;

CREATE TABLE IF NOT EXISTS seckill_order_0 (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=WAIT_PAY 2=PAID 3=FAILED 4=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
);

CREATE TABLE IF NOT EXISTS seckill_order_1 (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=WAIT_PAY 2=PAID 3=FAILED 4=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
);

USE message_s1;

CREATE TABLE IF NOT EXISTS seckill_order_0 (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=WAIT_PAY 2=PAID 3=FAILED 4=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
);

CREATE TABLE IF NOT EXISTS seckill_order_1 (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=PENDING 1=WAIT_PAY 2=PAID 3=FAILED 4=CANCELLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seckill_user_product (user_id, product_id),
    KEY idx_seckill_user (user_id),
    KEY idx_seckill_product (product_id)
);
