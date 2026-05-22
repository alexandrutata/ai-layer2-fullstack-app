INSERT INTO suppliers (id, name, email, phone, address)
VALUES ('add00001-0000-0000-0000-000000000001', 'TechSupply Co.', 'contact@techsupply.com', '+1-800-123-4567',
        '123 Silicon Valley Blvd, San Jose, CA 95101'),
       ('add00002-0000-0000-0000-000000000002', 'FashionWholesale Ltd.', 'orders@fashionwholesale.com',
        '+1-800-234-5678', '456 Garment District, New York, NY 10018'),
       ('add00003-0000-0000-0000-000000000003', 'HomeGoods Distributors', 'sales@homegoods-dist.com',
        '+1-800-345-6789', '789 Industrial Park, Chicago, IL 60601');

UPDATE products
SET supplier_id = 'add00001-0000-0000-0000-000000000001'
WHERE id IN ('fade0001-0000-0000-0000-000000000001', 'fade0002-0000-0000-0000-000000000002',
             'fade0003-0000-0000-0000-000000000003', 'fade000a-0000-0000-0000-00000000000a');

UPDATE products
SET supplier_id = 'add00002-0000-0000-0000-000000000002'
WHERE id IN ('fade0004-0000-0000-0000-000000000004', 'fade0005-0000-0000-0000-000000000005');

UPDATE products
SET supplier_id = 'add00003-0000-0000-0000-000000000003'
WHERE id IN ('fade0006-0000-0000-0000-000000000006', 'fade0007-0000-0000-0000-000000000007',
             'fade0008-0000-0000-0000-000000000008', 'fade0009-0000-0000-0000-000000000009');

ALTER TABLE products
    ALTER COLUMN supplier_id SET NOT NULL;
