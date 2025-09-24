CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS warehouses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    address_country VARCHAR(255),
    address_city VARCHAR(255),
    address_street VARCHAR(255),
    address_house VARCHAR(255),
    address_flat VARCHAR(255),
    CONSTRAINT uq_warehouses_address UNIQUE (address_country, address_city, address_street, address_house, address_flat)
);

CREATE TABLE IF NOT EXISTS warehouses_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    dimension_height DOUBLE PRECISION,
    dimension_depth DOUBLE PRECISION,
    dimension_width DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    quantity BIGINT NOT NULL DEFAULT 0,
    fragile BOOLEAN,

    CONSTRAINT fk_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE
);