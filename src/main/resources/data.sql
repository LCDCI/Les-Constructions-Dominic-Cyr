-- data for lots
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('f3c8837d-bd65-4bc5-9f01-cb9082fc657e', NULL, 'Montreal, QC', 120000.0, '55x110', 'AVAILABLE');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('5a82954c-8e2c-466a-8a8f-9983b79ede63', NULL, 'Quebec City, QC', 180000.0, '65x125', 'SOLD');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('cd465054-403e-4861-b9ab-1b672672c053', NULL, 'Sherbrooke, QC', 140000.0, '60x115', 'PENDING');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('a51e7923-7a46-4e65-8cee-8783126e780b', NULL, 'Trois-Rivières, QC', 155000.0, '70x130', 'AVAILABLE');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('64f2d3b1-eb36-49d6-8bc3-a816d97ddeb9', NULL, 'Gatineau, QC', 110000.0, '50x100', 'SOLD');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('3b9b8bf2-7ea4-4b3a-9250-53ccb1a77f87', NULL, 'Drummondville, QC', 175000.0, '75x145', 'AVAILABLE');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('02088623-dd3c-4fef-af67-2caf60dc1902', NULL, 'Saguenay, QC', 165000.0, '70x140', 'PENDING');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('97fd170d-189b-4c4c-880d-31893a146712', NULL, 'Rimouski, QC', 130000.0, '55x120', 'AVAILABLE');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('db43c148-68de-4882-818a-d15dc8d5fcdb', NULL, 'Chicoutimi, QC', 160000.0, '65x135', 'SOLD');
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES ('adb6f5b7-e036-49cf-899e-a39dcaecd91f', NULL, 'Baie-Comeau, QC', 145000.0, '60x125', 'AVAILABLE');

-- data for houses
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('a3f1c0f1-8f2b-4c3d-9d5a-1b2a3c4d5e6f', 'Renovated Bungalow', 'Montreal, QC', 'Modern bungalow featuring sleek finishes, smart home technology, and eco-friendly materials, ideally located near parks and public transit.', 6, 3, 2, 2022);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('b7d2e1a4-2c6f-4b8e-9f3c-7a9b0c1d2e3f', 'Split-Level Family Home', 'Laval, QC', 'Contemporary split-level design with open living spaces, premium appliances, expansive backyard, and oversized garage with workshop area.', 8, 4, 3, 2023);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('c9e3f2b5-3d7a-4f9b-8e4d-0a1b2c3d4e5f', 'Historic Stone Residence', 'Quebec City, QC', 'Beautifully restored stone residence combining timeless architecture with cutting-edge modern amenities and stunning river views.', 10, 5, 3, 2021);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('d1a4b3c6-4e8b-5a0c-9f1d-2c3b4a5d6e7f', 'Cozy Family Home', 'Sherbrooke, QC', 'Energy-efficient family home in a peaceful neighborhood, featuring modern insulation, updated systems, and walking distance to top-rated schools.', 5, 3, 1, 2020);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('e2b5c4d7-5f9c-6b1d-0a2e-3d4c5b6a7f8e', 'Modern Townhouse', 'Gatineau, QC', 'State-of-the-art townhouse with solar panels, geothermal heating, premium finishes, and contemporary design throughout.', 5, 2, 2, 2024);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('f3c6d5e8-6a0d-7c2e-1b3f-4e5d6c7b8a9f', 'Detached Home with Basement', 'Longueuil, QC', 'Luxurious detached home with professionally finished basement, high-end fixtures, and modern open-concept living areas.', 7, 4, 2, 2021);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('04d7e6f9-7b1e-8d3f-2c4a-5f6e7d8c9b0a', 'Open-Concept House', 'Saint-Jean-sur-Richelieu, QC', 'Bright open-concept residence with floor-to-ceiling windows, custom deck, privacy fencing, and chef-inspired kitchen.', 6, 3, 2, 2023);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('158e07a1-8c2f-9e4d-3b5a-6c7d8e9f0a1b', 'Starter Home', 'Drummondville, QC', 'Perfect turnkey starter home with updated kitchen and bathrooms, located minutes from downtown shopping and dining.', 4, 2, 1, 2020);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('269f18b2-9d3a-0f5e-4c6b-7d8e9f0a1b2c', 'Elegant Family Residence', 'Trois-Rivières, QC', 'Sophisticated family residence featuring gourmet kitchen with quartz countertops, spa-like bathrooms, and expansive outdoor entertainment patio.', 9, 4, 3, 2022);
INSERT INTO houses (house_identifier, house_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year) VALUES ('37a029c3-0e4b-1f6d-5d7c-8e9f0a1b2c3d', 'Country-Style Bungalow', 'Saint-Hyacinthe, QC', 'Charming country bungalow on a generous lot with mature landscaping, modern amenities, and tranquil rural setting.', 6, 3, 2, 2021);

-- data for renovations
INSERT INTO renovations (renovation_identifier, description) VALUES ('78cc74d2-0dae-4be9-be91-d3750311da94', 'This renovation includes a complete kitchen remodel with new cabinets, countertops, and appliances, as well as updated flooring throughout the main living areas.');
-- data for footer
INSERT INTO footer_content (section, label, value, display_order) VALUES
('contact', 'name', 'Isabelle Misiazeck', 1),
('contact', 'phone', '514-123-4567', 2),
('contact', 'email', 'isabelle.misiazeck@foresta.ca', 3),
('hours', 'weekday', 'Monday to Wednesday: 1 p.m to 7 p.m', 1),
('hours', 'weekend', 'Saturday and Sunday: 11 a.m. to 5 p.m.', 2),
('office', 'address_line1', '104 rue du Boisé', 1),
('office', 'address_line2', 'St-Alphonse de Granby', 2),
('office', 'address_line3', 'Granby, QC J2J 2X4', 3);

-- data for projects
INSERT INTO projects (
    project_identifier,
    project_name,
    project_description,
    status,
    start_date,
    end_date,
    primary_color,
    tertiary_color,
    buyer_color,
    buyer_name,
    customer_id,
    lot_identifier,
    progress_percentage
) VALUES
      (
          'proj-001-foresta',
          'Foresta',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant houses.',
          'IN_PROGRESS',
          '2025-01-15',
          '2025-12-31',
          '#C8D5B9',
          '#8FA383',
          '#6B8E6F',
          'FORESTA RÉSIDENCES SECONDAIRES',
          'cust-001',
          'lot-001',
          0
      ),
      (
          'proj-002-Panorama',
          'Naturest',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant houses.',
          'IN_PROGRESS',
          '2025-02-01',
          '2026-01-31',
          '#8B7355',
          '#C19A6B',
          '#D4AF7A',
          'NATUREST RÉSIDENCES SECONDAIRES',
          'cust-002',
          'lot-002',
          0
      );

INSERT INTO schedules (schedule_identifier, task_date, task_description, lot_number, day_of_week) VALUES
('SCH-001', '2025-12-05', 'Begin Excavation', 'Lot 53', 'Wednesday'),
('SCH-002', '2025-12-06', 'Plumbing', 'Lot 57', 'Wednesday'),
('SCH-003', '2025-12-12', 'Electrical', 'Lot 54', 'Thursday'),
('SCH-004', '2025-12-08', 'End of Excavation', 'Lot 53', 'Friday');