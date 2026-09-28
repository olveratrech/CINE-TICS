ALTER TABLE seat_holds ADD CONSTRAINT uq_hold_customer UNIQUE (id, customer_id);
CREATE TABLE ticket_sales (
    id VARCHAR2(36 CHAR) PRIMARY KEY,
    hold_id VARCHAR2(36 CHAR) NOT NULL UNIQUE,
    customer_id VARCHAR2(40 CHAR) NOT NULL,
    screening_id NUMBER(19) NOT NULL,
    selection_signature VARCHAR2(240 CHAR) NOT NULL,
    total NUMBER(14,2) NOT NULL CHECK (total > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    FOREIGN KEY (hold_id, customer_id) REFERENCES seat_holds(id, customer_id),
    FOREIGN KEY (hold_id, screening_id) REFERENCES seat_holds(id, screening_id),
    UNIQUE (id, screening_id)
);
CREATE INDEX ix_ticket_sale_customer ON ticket_sales(customer_id);
CREATE INDEX ix_ticket_sale_show ON ticket_sales(screening_id);
CREATE TABLE ticket_payments (
    sale_id VARCHAR2(36 CHAR) PRIMARY KEY REFERENCES ticket_sales(id),
    amount NUMBER(14,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
CREATE TABLE tickets (
    id VARCHAR2(36 CHAR) PRIMARY KEY,
    sale_id VARCHAR2(36 CHAR) NOT NULL,
    screening_id NUMBER(19) NOT NULL,
    seat_number NUMBER(5) NOT NULL,
    fare_type VARCHAR2(10 CHAR) NOT NULL CHECK (fare_type IN ('ADULTO','NINO')),
    price NUMBER(12,2) NOT NULL CHECK (price > 0),
    UNIQUE (screening_id, seat_number),
    FOREIGN KEY (sale_id, screening_id) REFERENCES ticket_sales(id, screening_id),
    FOREIGN KEY (screening_id, seat_number) REFERENCES screening_seats(screening_id, seat_number)
);
CREATE INDEX ix_ticket_sale ON tickets(sale_id, screening_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON ticket_sales TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON ticket_payments TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON tickets TO CINE_APP;
