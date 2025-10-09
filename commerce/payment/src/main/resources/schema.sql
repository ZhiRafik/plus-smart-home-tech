-- Тип статуса оплаты
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
        CREATE TYPE payment_status AS ENUM (
            'PENDING',
            'SUCCESS',
            'FAILED'
        );
    END IF;
END
$$;

CREATE TABLE payments (
    id UUID PRIMARY KEY, -- идентификатор платежа (paymentId), генерируем в Java-коде
    order_id UUID NOT NULL,
    status payment_status NOT NULL, -- текущий статус оплаты
    products_price DOUBLE PRECISION NOT NULL, -- стоимость товаров
    delivery_price DOUBLE PRECISION NOT NULL, -- стоимость доставки
    fee_amount DOUBLE PRECISION NOT NULL, -- рассчитанная сумма налога
    total_amount DOUBLE PRECISION NOT NULL -- итоговая сумма (products + delivery + fee)
);

-- Индекс для быстрого поиска по заказу
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
