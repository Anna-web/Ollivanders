-- Enable foreign key constraints
PRAGMA foreign_keys = ON;

-- Wood types table
CREATE TABLE IF NOT EXISTS wood_types (
    wood_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    rarity TEXT CHECK(rarity IN ('common', 'uncommon', 'rare', 'legendary')),
    description TEXT,
    optimal_length_min REAL DEFAULT 8.0,
    optimal_length_max REAL DEFAULT 14.0,
    common_characteristics TEXT
);

-- Core materials table
CREATE TABLE IF NOT EXISTS cores (
    core_id INTEGER PRIMARY KEY AUTOINCREMENT,
    material TEXT UNIQUE NOT NULL,
    power_level INTEGER CHECK(power_level BETWEEN 1 AND 10),
    adaptability INTEGER CHECK(adaptability BETWEEN 1 AND 10),
    description TEXT,
    danger_level INTEGER DEFAULT 1 CHECK(danger_level BETWEEN 1 AND 5)
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    birth_date TEXT,
    blood_status TEXT CHECK(blood_status IN ('pure', 'half', 'muggle', 'unknown')),
    house TEXT CHECK(house IN ('Gryffindor', 'Hufflepuff', 'Ravenclaw', 'Slytherin', 'Other')),
    species TEXT DEFAULT 'human',
    wand_license TEXT UNIQUE,
    registration_date TEXT DEFAULT (datetime('now')),
    notes TEXT,
    CONSTRAINT unique_customer UNIQUE (first_name, last_name, birth_date)
);

-- Items table
CREATE TABLE IF NOT EXISTS items (
    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_type TEXT NOT NULL CHECK(item_type IN ('wood', 'core')),
    item_name TEXT NOT NULL
);


-- Inventory Table (Tracks current stock)
CREATE TABLE IF NOT EXISTS component_inventory (
    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_type TEXT NOT NULL CHECK(item_type IN ('wood', 'core')),
    material_id INTEGER NOT NULL, -- references wood_types or cores
    quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0),
    last_updated TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (item_id) REFERENCES items(item_id)
);

-- Simplified Deliveries (Just a record of inventory additions)
CREATE TABLE IF NOT EXISTS inventory_deliveries (
    delivery_id INTEGER PRIMARY KEY AUTOINCREMENT,
    delivery_date TEXT DEFAULT (datetime('now')),
    supplier_name TEXT NOT NULL,
    received_by TEXT NOT NULL,
    notes TEXT
);

-- Delivery Items (What was actually delivered)
CREATE TABLE IF NOT EXISTS delivery_items (
    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    delivery_id INTEGER NOT NULL,
    item_type TEXT NOT NULL CHECK(item_type IN ('wood', 'core')),
    material_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK(quantity > 0),
    FOREIGN KEY (delivery_id) REFERENCES inventory_deliveries(delivery_id),
    FOREIGN KEY (item_id) REFERENCES items(item_id)
);


-- Wands table
CREATE TABLE IF NOT EXISTS wands (
    wand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    wood_id INTEGER NOT NULL,
    core_id INTEGER NOT NULL,
    length REAL NOT NULL CHECK(length BETWEEN 5 AND 20),
    flexibility TEXT CHECK(flexibility IN ('rigid', 'unyielding', 'solid', 'stiff', 'flexible', 'whippy', 'supple')),
    production_date TEXT DEFAULT (datetime('now')),
    condition TEXT DEFAULT 'new' CHECK(condition IN ('new', 'used', 'refurbished', 'damaged')),
    special_features TEXT,
    price REAL NOT NULL CHECK(price > 0),
    status TEXT DEFAULT 'in_stock' CHECK(status IN ('in_stock', 'sold', 'reserved', 'defective')),
    notes TEXT,
    FOREIGN KEY (wood_id) REFERENCES wood_types(wood_id) ON DELETE RESTRICT,
    FOREIGN KEY (core_id) REFERENCES cores(core_id) ON DELETE RESTRICT
);

-- Sales table
CREATE TABLE IF NOT EXISTS sales (
    sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
    wand_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    sale_date TEXT DEFAULT (datetime('now')),
    sale_price REAL NOT NULL CHECK(sale_price > 0),
    payment_method TEXT CHECK(payment_method IN ('cash', 'gringotts', 'credit', 'galleons')),
    warranty_until TEXT,
    notes TEXT,
    FOREIGN KEY (wand_id) REFERENCES wands(wand_id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT
);

-- Price history table
CREATE TABLE IF NOT EXISTS price_history (
    price_change_id INTEGER PRIMARY KEY AUTOINCREMENT,
    wand_id INTEGER NOT NULL,
    old_price REAL NOT NULL,
    new_price REAL NOT NULL,
    change_date TEXT DEFAULT (datetime('now')),
    reason TEXT,
    changed_by TEXT,
    FOREIGN KEY (wand_id) REFERENCES wands(wand_id) ON DELETE CASCADE
);

-- Views and indexes
CREATE VIEW IF NOT EXISTS current_inventory AS
SELECT
    w.wand_id,
    w.length,
    w.flexibility,
    w.condition,
    w.price AS current_price,
    w.status,
    wt.name AS wood_name,
    c.material AS core_material
FROM
    wands w
JOIN
    wood_types wt ON w.wood_id = wt.wood_id
JOIN
    cores c ON w.core_id = c.core_id
WHERE
    w.status = 'in_stock';

CREATE VIEW IF NOT EXISTS popular_woods AS
SELECT
    wt.name AS wood_name,
    COUNT(s.wand_id) AS sales_count
FROM
    wood_types wt
JOIN
    wands w ON wt.wood_id = w.wood_id
JOIN
    sales s ON w.wand_id = s.wand_id
GROUP BY
    wt.name
ORDER BY
    sales_count DESC;

CREATE INDEX IF NOT EXISTS idx_wands_wood ON wands(wood_id);
CREATE INDEX IF NOT EXISTS idx_wands_core ON wands(core_id);
CREATE INDEX IF NOT EXISTS idx_wands_status ON wands(status);
CREATE INDEX IF NOT EXISTS idx_sales_customer ON sales(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date);
CREATE INDEX IF NOT EXISTS idx_component_inventory_item ON component_inventory(item_id);
CREATE INDEX IF NOT EXISTS idx_delivery_requests_item ON delivery_requests(item_id);

