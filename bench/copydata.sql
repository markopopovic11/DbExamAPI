-- PROTOTYPE (wayfinder #24) — copy seed schema data into the current student
-- schema (search_path must already point at it). FK-safe order.
INSERT INTO categories  SELECT * FROM seed.categories;
INSERT INTO products    SELECT * FROM seed.products;
INSERT INTO customers   SELECT * FROM seed.customers;
INSERT INTO stores      SELECT * FROM seed.stores;
INSERT INTO employees   SELECT * FROM seed.employees;
INSERT INTO orders      SELECT * FROM seed.orders;
INSERT INTO order_items SELECT * FROM seed.order_items;
INSERT INTO payments    SELECT * FROM seed.payments;
INSERT INTO reviews     SELECT * FROM seed.reviews;
