-- PROTOTYPE (wayfinder #24) — triggers attached after seeding (unqualified names).
CREATE TRIGGER trg_audit_orders
    AFTER INSERT OR UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION fn_audit_orders();

CREATE TRIGGER trg_decrement_stock
    AFTER INSERT ON order_items
    FOR EACH ROW EXECUTE FUNCTION fn_decrement_stock();
