CREATE TABLE suppliers
(
    id      UUID         PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    email   VARCHAR(255),
    phone   VARCHAR(50),
    address VARCHAR(500)
);

ALTER TABLE products
    ADD COLUMN supplier_id UUID REFERENCES suppliers (id);
