CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Тип статуса доставки
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'delivery_status') THEN
        CREATE TYPE delivery_status AS ENUM (
            'CREATED',
            'IN_PROGRESS',
            'DELIVERED',
            'FAILED',
            'CANCELLED'
        );
    END IF;
END
$$;

-- Таблица доставок
CREATE TABLE deliveries (
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

    status delivery_status NOT NULL                       -- текущий статус доставки
);

-- Полезные индексы (необязательно, но ускоряют выборки по статусу/улицам)
CREATE INDEX IF NOT EXISTS idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_from_street ON deliveries(from_street);
CREATE INDEX IF NOT EXISTS idx_deliveries_to_street ON deliveries(to_street);

ALTER TABLE deliveries
  ADD CONSTRAINT chk_weight_non_negative CHECK (total_weight >= 0),
  ADD CONSTRAINT chk_volume_non_negative CHECK (total_volume >= 0);