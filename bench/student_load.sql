-- PROTOTYPE (wayfinder #24) — pgbench script simulating one exam-room student:
-- an indexed lookup + an aggregate join, then think time. Mostly-idle profile.
\set cid random(1, 10000)
SELECT o.id, o.status, order_total(o.id)
FROM orders o WHERE o.customer_id = :cid
ORDER BY o.ordered_at DESC LIMIT 5;
\sleep 1200 ms
\set cid2 random(1, 10000)
SELECT c.name, count(*) AS orders, sum(oi.qty * oi.unit_price) AS total
FROM customers c
JOIN orders o ON o.customer_id = c.id
JOIN order_items oi ON oi.order_id = o.id
WHERE c.id = :cid2
GROUP BY c.name;
\sleep 1800 ms
