
CREATE TABLE placed_order (
    id INTEGER PRIMARY KEY,
    order_name VARCHAR(50) NOT NULL
);

CREATE TABLE order_item (
    id INTEGER PRIMARY KEY,
    item_name VARCHAR(50) NOT NULL,
    order_id INTEGER NOT NULL REFERENCES placed_order(id)
);


CREATE TABLE target_location (
    id INTEGER PRIMARY KEY,
    address VARCHAR(50) NOT NULL,
    order_id INTEGER NOT NULL REFERENCES placed_order(id)
);

CREATE TABLE item_supplier (
    id INTEGER PRIMARY KEY,
    supplier_name VARCHAR(50) NOT NULL,
    item_id INTEGER NOT NULL REFERENCES order_item(id)
);

INSERT INTO placed_order VALUES (1, 'order 1');
INSERT INTO placed_order VALUES (2, 'order 2');
INSERT INTO placed_order VALUES (3, 'order 3');


INSERT INTO order_item VALUES (1, 'item 1', 1);
INSERT INTO order_item VALUES (2, 'item 2', 1);


INSERT INTO item_supplier VALUES (1, 'item 1', 1);
INSERT INTO item_supplier VALUES (2, 'item 1', 1);