CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Тип ENUM для статусов заказов
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_state') THEN
        CREATE TYPE order_state AS ENUM (
            'NEW',
            'ON_PAYMENT',
            'ON_DELIVERY',
            'DONE',
            'DELIVERED',
            'ASSEMBLED',
            'PAID',
            'COMPLETED',
            'DELIVERY_FAILED',
            'ASSEMBLY_FAILED',
            'PAYMENT_FAILED',
            'PRODUCT_RETURNED',
            'CANCELED'
        );
    END IF;
END
$$;

-- 2) Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    delivery_weight DOUBLE PRECISION DEFAULT 0,
    delivery_volume DOUBLE PRECISION DEFAULT 0,
    fragile BOOLEAN DEFAULT true,
    address_country VARCHAR(255),
    address_city VARCHAR(255),
    address_street VARCHAR(255),
    address_house VARCHAR(255),
    address_flat VARCHAR(255),
    total_price DOUBLE PRECISION DEFAULT 0,
    delivery_price DOUBLE PRECISION DEFAULT 0,
    product_price DOUBLE PRECISION DEFAULT 0,
    state order_state NOT NULL DEFAULT 'NEW'
);

-- 3) Таблица позиций заказа (товары)
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    unit_price DOUBLE PRECISION,

    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT uq_order_item UNIQUE (order_id, product_id)
);