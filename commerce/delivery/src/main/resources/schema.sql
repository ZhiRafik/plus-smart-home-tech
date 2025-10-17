CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
DROP TYPE IF EXISTS delivery_state CASCADE;

-- Тип статуса доставки
CREATE TYPE delivery_state AS ENUM (
    'CREATED',
    'IN_PROGRESS',
    'DELIVERED',
    'FAILED',
    'CANCELLED'
);


-- Таблица доставок
CREATE TABLE IF NOT EXISTS delivery (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),       -- идентификатор доставки (deliveryId)
    order_id UUID NOT NULL UNIQUE,                        -- ссылка на заказ (одна доставка на заказ)

    from_country VARCHAR(255) NOT NULL,                   -- страна
    from_city    VARCHAR(255) NOT NULL,                   -- город
    from_street  VARCHAR(255) NOT NULL,                   -- улица (участвует в расчёте ADDRESS_1/ADDRESS_2)
    from_house   VARCHAR(255) NOT NULL,                   -- дом
    from_flat    VARCHAR(255),                            -- квартира (nullable)

    to_country   VARCHAR(255) NOT NULL,                   -- страна
    to_city      VARCHAR(255) NOT NULL,                   -- город
    to_street    VARCHAR(255) NOT NULL,                   -- улица
    to_house     VARCHAR(255) NOT NULL,                   -- дом
    to_flat      VARCHAR(255),                            -- квартира (nullable)

    total_weight DOUBLE PRECISION NOT NULL DEFAULT 0,     -- общий вес (кг)
    total_volume DOUBLE PRECISION NOT NULL DEFAULT 0,     -- общий объём (м^3)
    fragile      BOOLEAN NOT NULL DEFAULT FALSE,          -- признак хрупкости

    status delivery_state NOT NULL                       -- текущий статус доставки
);