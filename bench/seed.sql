-- PROTOTYPE (wayfinder #24) — seed data, unqualified names (see ddl.sql).
-- Target: ~25 MB — a plausible course-exam starting state.

INSERT INTO categories(name)
SELECT 'category_' || g FROM generate_series(1, 20) g;

INSERT INTO products(category_id, name, price, stock)
SELECT (g % 20) + 1, 'product_' || g, (g % 500) + 0.99, 100 + (g % 900)
FROM generate_series(1, 3000) g;

INSERT INTO customers(name, email, city)
SELECT 'customer ' || g, 'user' || g || '@example.com', 'city_' || (g % 40)
FROM generate_series(1, 10000) g;

INSERT INTO stores(city)
SELECT 'city_' || g FROM generate_series(1, 50) g;

INSERT INTO employees(store_id, name, salary)
SELECT (g % 50) + 1, 'employee ' || g, 30000 + (g % 40000)
FROM generate_series(1, 500) g;

INSERT INTO orders(customer_id, ordered_at, status)
SELECT (g % 10000) + 1,
       timestamptz '2025-01-01' + (g % 500) * interval '1 hour',
       (ARRAY['new','paid','shipped','done'])[(g % 4) + 1]
FROM generate_series(1, 20000) g;

INSERT INTO order_items(order_id, product_id, qty, unit_price)
SELECT (g % 20000) + 1, (g % 3000) + 1, (g % 5) + 1, ((g % 500) + 0.99)
FROM generate_series(1, 60000) g;

INSERT INTO payments(order_id, amount, paid_at)
SELECT g, ((g % 900) + 10.50), timestamptz '2025-01-02' + (g % 400) * interval '1 hour'
FROM generate_series(1, 20000) g;

INSERT INTO reviews(product_id, customer_id, rating, body)
SELECT (g % 3000) + 1, (g % 10000) + 1, (g % 5) + 1,
       'review text padding padding padding ' || g
FROM generate_series(1, 15000) g;

ANALYZE;
