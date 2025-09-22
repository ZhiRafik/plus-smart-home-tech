CREATE TABLE IF NOT EXISTS warehouses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    address_country VARCHAR(255),
    address_city VARCHAR(255),
    address_street VARCHAR(255),
    address_house VARCHAR(255),
    address_flat VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS warehouses_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    warehouse_id UUID NOT NULL,
    dimension_height DOUBLE PRECISION,
    dimension_depth DOUBLE PRECISION,
    dimension_length DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    quantity BIGINT,
    fragile BOOLEAN,

    CONSTRAINT fk_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);