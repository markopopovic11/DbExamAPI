-- PROTOTYPE (wayfinder #24) — throwaway benchmark, wipe me.
-- Representative exam-environment schema. Names unqualified on purpose:
-- the same file builds the template database (search_path=public) and each
-- per-student schema in the schema-model comparison (search_path=s_NNNN).

CREATE TABLE categories (
    id      serial PRIMARY KEY,
    name    text NOT NULL
);

CREATE TABLE products (
    id          serial PRIMARY KEY,
    category_id int NOT NULL REFERENCES categories(id),
    name        text NOT NULL,
    price       numeric(10,2) NOT NULL,
    stock       int NOT NULL DEFAULT 100
);
CREATE INDEX idx_products_category ON products(category_id);

CREATE TABLE customers (
    id      serial PRIMARY KEY,
    name    text NOT NULL,
    email   text NOT NULL UNIQUE,
    city    text NOT NULL
);

CREATE TABLE stores (
    id      serial PRIMARY KEY,
    city    text NOT NULL
);

CREATE TABLE employees (
    id        serial PRIMARY KEY,
    store_id  int NOT NULL REFERENCES stores(id),
    name      text NOT NULL,
    salary    numeric(10,2) NOT NULL
);

CREATE TABLE orders (
    id          serial PRIMARY KEY,
    customer_id int NOT NULL REFERENCES customers(id),
    ordered_at  timestamptz NOT NULL,
    status      text NOT NULL DEFAULT 'new'
);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_at ON orders(ordered_at);

CREATE TABLE order_items (
    order_id   int NOT NULL REFERENCES orders(id),
    product_id int NOT NULL REFERENCES products(id),
    qty        int NOT NULL,
    unit_price numeric(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);
CREATE INDEX idx_items_product ON order_items(product_id);

CREATE TABLE payments (
    id       serial PRIMARY KEY,
    order_id int NOT NULL REFERENCES orders(id),
    amount   numeric(10,2) NOT NULL,
    paid_at  timestamptz
);
CREATE INDEX idx_payments_order ON payments(order_id);

CREATE TABLE reviews (
    id          serial PRIMARY KEY,
    product_id  int NOT NULL REFERENCES products(id),
    customer_id int NOT NULL REFERENCES customers(id),
    rating      int NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body        text
);
CREATE INDEX idx_reviews_product ON reviews(product_id);

CREATE TABLE audit_log (
    id         bigserial PRIMARY KEY,
    table_name text NOT NULL,
    action     text NOT NULL,
    at         timestamptz NOT NULL DEFAULT now(),
    payload    jsonb
);

CREATE FUNCTION order_total(p_order_id int) RETURNS numeric
LANGUAGE plpgsql STABLE AS $$
DECLARE v numeric;
BEGIN
    SELECT coalesce(sum(qty * unit_price), 0) INTO v
    FROM order_items WHERE order_id = p_order_id;
    RETURN v;
END $$;

CREATE FUNCTION fn_audit_orders() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO audit_log(table_name, action, payload)
    VALUES ('orders', TG_OP, to_jsonb(NEW));
    RETURN NEW;
END $$;

CREATE FUNCTION fn_decrement_stock() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE products SET stock = stock - NEW.qty WHERE id = NEW.product_id;
    RETURN NEW;
END $$;
