CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- создаём таблицу для товаров
CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(255) NOT NULL,
    quantity_state VARCHAR(50),
    product_state VARCHAR(50),
    product_category VARCHAR(50),
    price DOUBLE PRECISION
);