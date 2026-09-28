CREATE TABLE auditoriums (
    id NUMBER(19) PRIMARY KEY,
    branch_id NUMBER(19) NOT NULL REFERENCES branches(id),
    name VARCHAR2(120 CHAR) NOT NULL,
    capacity NUMBER(5) NOT NULL CHECK (capacity BETWEEN 1 AND 10000),
    UNIQUE (branch_id, name)
);
CREATE TABLE screenings (
    id NUMBER(19) PRIMARY KEY,
    auditorium_id NUMBER(19) NOT NULL REFERENCES auditoriums(id),
    title VARCHAR2(160 CHAR) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (ends_at > starts_at)
);
CREATE INDEX ix_screening_room_time ON screenings(auditorium_id, starts_at);
CREATE TABLE seat_holds (
    id VARCHAR2(36 CHAR) PRIMARY KEY,
    screening_id NUMBER(19) NOT NULL REFERENCES screenings(id),
    customer_id VARCHAR2(40 CHAR) NOT NULL REFERENCES demo_wallets(customer_id),
    request_key VARCHAR2(64 CHAR) NOT NULL,
    seat_selection VARCHAR2(120 CHAR) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    cancelled NUMBER(1) DEFAULT 0 NOT NULL CHECK (cancelled IN (0,1)),
    UNIQUE (customer_id, request_key),
    UNIQUE (id, screening_id)
);
CREATE INDEX ix_hold_screening ON seat_holds(screening_id);
CREATE TABLE screening_seats (
    screening_id NUMBER(19) NOT NULL REFERENCES screenings(id),
    seat_number NUMBER(5) NOT NULL CHECK (seat_number BETWEEN 1 AND 10000),
    hold_id VARCHAR2(36 CHAR),
    PRIMARY KEY (screening_id, seat_number),
    FOREIGN KEY (hold_id, screening_id) REFERENCES seat_holds(id, screening_id)
);
CREATE INDEX ix_seat_hold ON screening_seats(hold_id, screening_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON auditoriums TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON screenings TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON seat_holds TO CINE_APP;
GRANT SELECT, INSERT, UPDATE, DELETE ON screening_seats TO CINE_APP;
