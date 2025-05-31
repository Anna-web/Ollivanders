-- Disable foreign key checks temporarily
PRAGMA foreign_keys = OFF;

-- Drop views first
DROP VIEW IF EXISTS current_inventory;
DROP VIEW IF EXISTS popular_woods;

-- Drop tables in reverse order of dependency
DROP TABLE IF EXISTS price_history;
DROP TABLE IF EXISTS inventory_log;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS delivery_items;
DROP TABLE IF EXISTS deliveries;
DROP TABLE IF EXISTS wands;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS cores;
DROP TABLE IF EXISTS wood_types;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS component_inventory;
DROP TABLE IF EXISTS inventory_deliveries;

-- Re-enable foreign key checks
PRAGMA foreign_keys = ON;
