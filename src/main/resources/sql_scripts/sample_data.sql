-- Insert wood types
INSERT INTO wood_types (name, rarity, description, optimal_length_min, optimal_length_max, common_characteristics) VALUES
('Holly', 'uncommon', 'Holly wands often choose owners engaged in dangerous pursuits', 11.0, 13.0, 'Protective, works best with phoenix feathers'),
('Elder', 'legendary', 'The rarest wand wood, reputed to be deeply unlucky', 12.0, 15.0, 'Powerful but difficult to master'),
('Oak', 'common', 'For good and evil, but more often good', 10.0, 12.0, 'Strong personality, durable'),
('Willow', 'uncommon', 'Wands with healing power', 9.0, 11.0, 'Graceful, good for charms'),
('Yew', 'rare', 'Yew wands give their owners power over life and death', 13.0, 15.0, 'Long-lived, powerful dark magic potential');

-- Insert core materials
INSERT INTO cores (material, power_level, adaptability, description, danger_level) VALUES
('Phoenix feather', 9, 10, 'Capable of the greatest range of magic', 2),
('Dragon heartstring', 10, 7, 'Produces wands with the most power', 3),
('Unicorn hair', 6, 8, 'Produces the most consistent magic', 1),
('Thestral tail hair', 8, 5, 'Powerful but tricky to master', 4),
('Veela hair', 7, 6, 'Temperamental but powerful', 3);

-- Insert items
INSERT INTO items (item_type, item_name) VALUES
('wood', 'Holly'),
('wood', 'Elder'),
('wood', 'Oak'),
('wood', 'Willow'),
('wood', 'Yew'),
('core', 'Phoenix feather'),
('core', 'Dragon heartstring'),
('core', 'Unicorn hair'),
('core', 'Thestral tail hair'),
('core', 'Veela hair');

-- Insert customers
INSERT INTO customers (first_name, last_name, birth_date, blood_status, house, species, wand_license) VALUES
('Harry', 'Potter', '1980-07-31', 'half', 'Gryffindor', 'human', 'HP-1980-001'),
('Hermione', 'Granger', '1979-09-19', 'muggle', 'Gryffindor', 'human', 'HG-1979-002'),
('Draco', 'Malfoy', '1980-06-05', 'pure', 'Slytherin', 'human', 'DM-1980-003'),
('Luna', 'Lovegood', '1981-02-13', 'pure', 'Ravenclaw', 'human', 'LL-1981-004'),
('Rubeus', 'Hagrid', '1928-12-06', 'half', 'Gryffindor', 'half-giant', 'RH-1928-005');

-- Insert wands
INSERT INTO wands (wood_id, core_id, length, flexibility, production_date, condition, special_features, price, status) VALUES
(1, 1, 11.0, 'supple', '2023-01-20', 'new', 'Slight holly berry scent', 35.0, 'sold'),
(1, 2, 11.5, 'flexible', '2023-01-20', 'new', 'Unusually warm to the touch', 40.0, 'in_stock'),
(2, 1, 13.0, 'unyielding', '2023-02-05', 'new', 'Elder flower carving', 75.0, 'in_stock'),
(3, 3, 10.0, 'rigid', '2023-02-05', 'new', 'Acorn-shaped pommel', 28.0, 'in_stock'),
(4, 1, 10.5, 'whippy', '2023-02-10', 'new', 'Willow leaf engraving', 32.0, 'in_stock'),
(5, 2, 14.0, 'solid', '2023-02-10', 'new', 'Yew berry inlay', 65.0, 'in_stock');

-- Insert sales
INSERT INTO sales (wand_id, customer_id, sale_date, sale_price, payment_method, warranty_until) VALUES
(1, 1, '2023-01-31', 35.0, 'gringotts', '2028-01-31'),
(2, 1, '2023-02-01', 40.0, 'gringotts', '2028-02-01'),
(3, 2, '2023-02-15', 75.0, 'gringotts', '2028-02-15'),
(4, 3, '2023-02-20', 28.0, 'cash', '2028-02-20'),
(5, 4, '2023-02-25', 65.0, 'cash', '2028-02-25');

-- Insert price history
INSERT INTO price_history (wand_id, old_price, new_price, change_date, reason) VALUES
(1, 30.0, 35.0, '2023-01-25', 'Increased demand for holly wands'),
(2, 35.0, 40.0, '2023-01-25', 'Dragon heartstring price adjustment'),
(3, 70.0, 75.0, '2023-02-10', 'Elder wood rarity premium'),
(4, 28.0, 28.0, '2023-02-20', 'No price change'),
(5, 60.0, 65.0, '2023-02-25', 'New feature added');

-- Insert component inventory
INSERT INTO component_inventory (item_type, material_id, quantity) VALUES
-- Woods
('wood', 1, 10),  -- Holly
('wood', 2, 3),   -- Elder
('wood', 3, 15),  -- Oak
('wood', 4, 8),   -- Willow
('wood', 5, 5),   -- Yew
-- Cores
('core', 1, 20),  -- Phoenix feather
('core', 2, 15),  -- Dragon heartstring
('core', 3, 25),  -- Unicorn hair
('core', 4, 5),   -- Thestral tail hair
('core', 5, 10);  -- Veela hair

-- Sample inventory deliveries
INSERT INTO inventory_deliveries (supplier_name, received_by, notes) VALUES
('Phoenix Ashes Shop', 'Garrick Ollivander', 'Monthly phoenix feather restock'),
('Lumber Mill Inc.', 'Marcus Ollivander', 'Emergency elder wood delivery'),
('Dragon Farm', 'Garrick Ollivander', 'Dragon heartstring bulk order');

-- Sample delivery items (linked to inventory_deliveries)
INSERT INTO delivery_items (delivery_id, item_type, material_id, quantity) VALUES
-- First delivery (phoenix feathers)
(1, 'core', 1, 10),
-- Second delivery (elder wood)
(2, 'wood', 2, 5),
-- Third delivery (dragon heartstrings)
(3, 'core', 2, 8);