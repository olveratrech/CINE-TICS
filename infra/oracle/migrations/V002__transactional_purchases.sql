CREATE TABLE demo_wallets (
    customer_id VARCHAR2(40 CHAR) PRIMARY KEY,
    balance NUMBER(14,2) NOT NULL CHECK (balance >= 0),
    active NUMBER(1) DEFAULT 1 NOT NULL CHECK (active IN (0,1))
);
CREATE TABLE purchase_orders (
    id VARCHAR2(36 CHAR) PRIMARY KEY,
    customer_id VARCHAR2(40 CHAR) NOT NULL REFERENCES demo_wallets(customer_id),
    request_key VARCHAR2(64 CHAR) NOT NULL,
    request_hash VARCHAR2(64 CHAR) NOT NULL,
    branch_id NUMBER(19) NOT NULL REFERENCES branches(id),
    status VARCHAR2(32 CHAR) NOT NULL CHECK (status IN (
        'PROCESSING','APPROVED','INSUFFICIENT_FUNDS','OUT_OF_STOCK',
        'INACTIVE_ACCOUNT','PRODUCT_NOT_FOUND','INVALID_TOTAL')),
    total NUMBER(14,2) DEFAULT 0 NOT NULL CHECK (total >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_purchase_request UNIQUE (customer_id, request_key),
    CONSTRAINT ck_approved_total CHECK (status <> 'APPROVED' OR total > 0)
);
CREATE TABLE purchase_lines (
    order_id VARCHAR2(36 CHAR) NOT NULL REFERENCES purchase_orders(id),
    product_id NUMBER(19) NOT NULL REFERENCES products(id),
    quantity NUMBER(10) NOT NULL CHECK (quantity > 0),
    unit_price NUMBER(12,2) NOT NULL CHECK (unit_price >= 0),
    PRIMARY KEY (order_id, product_id)
);
CREATE TABLE demo_payments (
    order_id VARCHAR2(36 CHAR) PRIMARY KEY REFERENCES purchase_orders(id),
    amount NUMBER(14,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
CREATE INDEX ix_purchase_branch ON purchase_orders(branch_id);
CREATE INDEX ix_purchase_product ON purchase_lines(product_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON demo_wallets TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON purchase_orders TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON purchase_lines TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON demo_payments TO CINE_APP;
