-- data for lots
INSERT INTO lots (lot_identifier, image_identifier, location, price, dimensions, lot_status) VALUES
                                                                                                 ('f3c8837d-bd65-4bc5-9f01-cb9082fc657e', NULL, 'Montreal, QC', 120000.0, '55x110', 'AVAILABLE'),
                                                                                                 ('5a82954c-8e2c-466a-8a8f-9983b79ede63', NULL, 'Quebec City, QC', 180000.0, '65x125', 'SOLD'),
                                                                                                 ('cd465054-403e-4861-b9ab-1b672672c053', NULL, 'Sherbrooke, QC', 140000.0, '60x115', 'PENDING'),
                                                                                                 ('a51e7923-7a46-4e65-8cee-8783126e780b', NULL, 'Trois-Rivières, QC', 155000.0, '70x130', 'AVAILABLE'),
                                                                                                 ('64f2d3b1-eb36-49d6-8bc3-a816d97ddeb9', NULL, 'Gatineau, QC', 110000.0, '50x100', 'SOLD'),
                                                                                                 ('3b9b8bf2-7ea4-4b3a-9250-53ccb1a77f87', NULL, 'Drummondville, QC', 175000.0, '75x145', 'AVAILABLE'),
                                                                                                 ('02088623-dd3c-4fef-af67-2caf60dc1902', NULL, 'Saguenay, QC', 165000.0, '70x140', 'PENDING'),
                                                                                                 ('97fd170d-189b-4c4c-880d-31893a146712', NULL, 'Rimouski, QC', 130000.0, '55x120', 'AVAILABLE'),
                                                                                                 ('db43c148-68de-4882-818a-d15dc8d5fcdb', NULL, 'Chicoutimi, QC', 160000.0, '65x135', 'SOLD'),
                                                                                                 ('adb6f5b7-e036-49cf-899e-a39dcaecd91f', NULL, 'Baie-Comeau, QC', 145000.0, '60x125', 'AVAILABLE');

-- data for realizations
INSERT INTO realizations (realization_identifier, realization_name, location, description, number_of_rooms, number_of_bedrooms, number_of_bathrooms, construction_year)
VALUES
    ('a3f1c0f1-8f2b-4c3d-9d5a-1b2a3c4d5e6f', 'Renovated Bungalow', 'Montreal, QC', 'Modern bungalow featuring sleek finishes, smart home technology, and eco-friendly materials, ideally located near parks and public transit.', 6, 3, 2, 2022),
    ('b7d2e1a4-2c6f-4b8e-9f3c-7a9b0c1d2e3f', 'Split-Level Family Home', 'Laval, QC', 'Contemporary split-level design with open living spaces, premium appliances, expansive backyard, and oversized garage with workshop area.', 8, 4, 3, 2023),
    ('c9e3f2b5-3d7a-4f9b-8e4d-0a1b2c3d4e5f', 'Historic Stone Residence', 'Quebec City, QC', 'Beautifully restored stone residence combining timeless architecture with cutting-edge modern amenities and stunning river views.', 10, 5, 3, 2021),
    ('d1a4b3c6-4e8b-5a0c-9f1d-2c3b4a5d6e7f', 'Cozy Family Home', 'Sherbrooke, QC', 'Energy-efficient family home in a peaceful neighborhood, featuring modern insulation, updated systems, and walking distance to top-rated schools.', 5, 3, 1, 2020),
    ('e2b5c4d7-5f9c-6b1d-0a2e-3d4c5b6a7f8e', 'Modern Townhouse', 'Gatineau, QC', 'State-of-the-art townhouse with solar panels, geothermal heating, premium finishes, and contemporary design throughout.', 5, 2, 2, 2024),
    ('f3c6d5e8-6a0d-7c2e-1b3f-4e5d6c7b8a9f', 'Detached Home with Basement', 'Longueuil, QC', 'Luxurious detached home with professionally finished basement, high-end fixtures, and modern open-concept living areas.', 7, 4, 2, 2021),
    ('04d7e6f9-7b1e-8d3f-2c4a-5f6e7d8c9b0a', 'Open-Concept House', 'Saint-Jean-sur-Richelieu, QC', 'Bright open-concept residence with floor-to-ceiling windows, custom deck, privacy fencing, and chef-inspired kitchen.', 6, 3, 2, 2023),
    ('158e07a1-8c2f-9e4d-3b5a-6c7d8e9f0a1b', 'Starter Home', 'Drummondville, QC', 'Perfect turnkey starter home with updated kitchen and bathrooms, located minutes from downtown shopping and dining.', 4, 2, 1, 2020),
    ('269f18b2-9d3a-0f5e-4c6b-7d8e9f0a1b2c', 'Elegant Family Residence', 'Trois-Rivières, QC', 'Sophisticated family residence featuring gourmet kitchen with quartz countertops, spa-like bathrooms, and expansive outdoor entertainment patio.', 9, 4, 3, 2022),
    ('37a029c3-0e4b-1f6d-5d7c-8e9f0a1b2c3d', 'Country-Style Bungalow', 'Saint-Hyacinthe, QC', 'Charming country bungalow on a generous lot with mature landscaping, modern amenities, and tranquil rural setting.', 6, 3, 2, 2021);


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
    image_identifier,
    customer_id,
    contractor_id,
    salesperson_id,
    location,
    lot_identifier,
    progress_percentage
) VALUES
      (
          'proj-001-foresta',
          'Föresta',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.',
          'IN_PROGRESS',
          '2025-01-15',
          '2025-12-31',
          '#A9B3AA',
          '#DFE1DF',
          '#003663',
          'Marc Tremblay',
          '473f9e87-3415-491c-98a9-9d017c251911',
          '44444444-4444-4444-4444-444444444444',
          '22222222-2222-2222-2222-222222222222',
          '33333333-3333-3333-3333-333333333333',
          '104 rue du Boisé, St-Alphonse de Granby, Granby, QC J2J 2X4',
          'f3c8837d-bd65-4bc5-9f01-cb9082fc657e',
          90
      ),
      (
          'proj-002-panorama',
          'Panorama',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.',
          'IN_PROGRESS',
          '2025-02-01',
          '2026-01-31',
          '#737373',
          '#F6F4F1',
          '#545454',
          'Sylvie Lapointe',
          '6c8127f5-4529-4118-9ab1-cbcb38c4266a',
          '44444444-4444-4444-4444-444444444444',
          '22222222-2222-2222-2222-222222222222',
          '33333333-3333-3333-3333-333333333333',
          'Sutton, Quebec',
          '5a82954c-8e2c-466a-8a8f-9983b79ede63',
          80
      );

INSERT INTO schedules (schedule_identifier, task_date, task_description, lot_number, day_of_week) VALUES
                                                                                                      ('SCH-001', '2025-12-05', 'Begin Excavation', 'Lot 53', 'Wednesday'),
                                                                                                      ('SCH-002', '2025-12-06', 'Plumbing', 'Lot 57', 'Wednesday'),
                                                                                                      ('SCH-003', '2025-12-12', 'Electrical', 'Lot 54', 'Thursday'),
                                                                                                      ('SCH-004', '2025-12-08', 'End of Excavation', 'Lot 53', 'Friday');

INSERT INTO tasks (task_identifier, task_date, task_description, lot_number, day_of_week, assigned_to) VALUES
                                                                                                           ('TASK-001', '2025-12-05', 'Foundation Work', 'Lot 53', 'Wednesday', '22222222-2222-2222-2222-222222222222'),
                                                                                                           ('TASK-002', '2025-12-06', 'Framing', 'Lot 57', 'Wednesday', '22222222-2222-2222-2222-222222222222'),
                                                                                                           ('TASK-003', '2025-12-12', 'Roofing', 'Lot 54', 'Thursday', '22222222-2222-2222-2222-222222222222'),
                                                                                                           ('TASK-004', '2025-12-08', 'Drywall Installation', 'Lot 53', 'Friday', NULL);




INSERT INTO project_overview_content (
    project_identifier,
    hero_title,
    hero_subtitle,
    hero_description,
    overview_section_title,
    overview_section_content,
    features_section_title,
    location_section_title,
    location_description,
    location_address,
    location_map_embed_url,
    gallery_section_title,
    location_latitude,
    location_longitude
) VALUES (
             'proj-001-foresta',
             'FÖRESTA',
             'Résidences Secondaires en Harmonie avec la Nature',
             'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.',
             'About Foresta',
             'Foresta represents a unique opportunity to own a secondary residence in one of Quebec''s most tranquil settings. Each home is designed with sustainability and comfort in mind, featuring modern amenities while preserving the natural beauty of the surrounding forest.',
             'Project Features',
             'Location',
             'Foresta is located in St-Alphonse-de-Granby, offering easy access to nature trails, local amenities, and major highways. The perfect balance between seclusion and convenience.',
             '104 rue du Boisé, St-Alphonse de Granby, Granby, QC J2J 2X4',
             'https://www.google.com/maps/place/104+Rue+du+Boisé,+St-Alphonse+de+Granby,+QC+J2J+2X4',
             'Project Gallery',
             45.32027369731277,
             -72.79834091605535
         ),
         (
             'proj-002-panorama',
             'PANORAMA',
             'Condos Modernes avec Vue Imprenable',
             'Experience elevated living with Panorama condos. Featuring contemporary design and breathtaking views, this project combines urban convenience with natural serenity.',
             'About Panorama',
             'Panorama condos offer a modern lifestyle with spacious layouts, premium finishes, and access to shared amenities. Each unit is designed to maximize natural light and views of the surrounding landscape.',
             'Condo Features',
             'Location',
             'Strategically located to provide both tranquility and easy access to urban centers, Panorama offers the best of both worlds for discerning buyers.',
             'Sutton, Quebec',
             'https://www.google.com/maps/place/630+Rue+Maple,+Sutton,+QC+J0E+2K0/@45.1053459,-72.5670753,615m/data=!3m2!1e3!4b1!4m15!1m8!3m7!1s0x4cb61e78d1f51225:0x5040cadb2bb4a70!2sSutton,+QC!3b1!8m2!3d45.105894!4d-72.6166646!16s%2Fm%2F047dlz3!3m5!1s0x4cb61eb16f5de517:0x1691c2d8e333b6a!8m2!3d45.1053459!4d-72.5645004!16s%2Fg%2F11y7x5tmlr?entry=ttu&g_ep=EgoyMDI1MTIwOS4wIKXMDSoKLDEwMDc5MjA2N0gBUAM%3D',
             'Gallery',
             45.102386912240014,
             -72.57423759390365
         );

INSERT INTO project_features (project_identifier, feature_title, feature_description, display_order) VALUES
                                                                                                         ('proj-001-foresta', 'New Realizations', 'Contemporary architecture with traditional charm', 1),
                                                                                                         ('proj-001-foresta', 'Living Environment', 'Surrounded by pristine forest and nature trails', 2),
                                                                                                         ('proj-001-foresta', 'Lots', 'A variety of lots available for all', 3),
                                                                                                         ('proj-002-panorama','New Realizations', 'Contemporary architecture with traditional charm', 1),
                                                                                                         ('proj-002-panorama','Living Environment', 'Surrounded by pristine forest and nature trails', 2),
                                                                                                         ('proj-002-panorama','Lots', 'A variety of lots available for all', 3);

-- Gallery Images (using existing image identifiers or placeholders)
INSERT INTO project_gallery_images (project_identifier, image_identifier, image_caption, display_order) VALUES
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Exterior View', 1),
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Interior Living Space', 2),
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Kitchen', 3),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Building Exterior', 1),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Condo Interior', 2),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Amenities', 3);


INSERT INTO users (user_id, first_name, last_name, email, secondary_email, phone, user_role, auth0user_id)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'John', 'Owner', 'owner@test.com', NULL, '514-111-1111', 'OWNER', 'auth0|69542f38c08232af729f3d41'),
    ('22222222-2222-2222-2222-222222222222', 'Jane', 'Contractor', 'contractor@test.com', NULL, '514-222-2222', 'CONTRACTOR', 'auth0|693faa68bf9e92b3d445c4ab'),
    ('33333333-3333-3333-3333-333333333333', 'Bob', 'Sales', 'sales@test.com', NULL, '514-333-3333', 'SALESPERSON', 'auth0|693faa7d743287e135a703e1'),
    ('44444444-4444-4444-4444-444444444444', 'Alice', 'Customer', 'customer@test.com', NULL, '514-444-4444', 'CUSTOMER', 'auth0|693faa95743287e135a703e8');

-- data for project management page content (English)
INSERT INTO project_management_page_content (language, content_json) VALUES
    ('en', '{"hero":{"line1":"PROJECT MANAGEMENT,","line2":"FOR PEACE OF MIND"},"intro":{"heading":{"line1":"PLANNING","line2":"ORGANIZATION","line3":"WORK MONITORING"},"tagline":"We take care of it!","paragraph":"You own a piece of land and want to entrust project management to a qualified contractor? Les Constructions Dominic Cyr Inc. is here for you, whether for specific construction stages or a turnkey project.","image1":{"alt":"Professionals collaborating on project"},"image2":{"alt":"3D floor plan rendering"},"image3":{"alt":"Construction tools and materials"}},"advantages":{"heading":"There are many advantages to entrusting us with the management of your project:","item1":"Consulting service","item2":"Compliance with building regulations","item3":"Planning with all stakeholders","item4":"Establishment of the work schedule","item5":"Cost control","item6":"Rigorous monitoring","item7":"Compliance assurance","pricing":"Depending on the scope and complexity of the project, pricing could be at a fixed cost or at a marked-up price. Contact us to discuss.","contactLink":"Contact us"},"gallery":{"heading":"Some project management achievements by Les Constructions Dominic Cyr Inc.","caption":"Residence","image1":{"alt":"Residence"},"image2":{"alt":"Residence"},"image3":{"alt":"Residence"}}}');

-- data for project management page content (French)
INSERT INTO project_management_page_content (language, content_json) VALUES
    ('fr', '{"hero":{"line1":"LA GESTION DE PROJET,","line2":"POUR UNE TRANQUILLITÉ D''ESPRIT"},"intro":{"heading":{"line1":"PLANIFICATION","line2":"ORGANISATION","line3":"SUIVI DES TRAVAUX"},"tagline":"On s''en occupe !","paragraph":"Vous êtes propriétaire d''un terrain et vous souhaitez confier la gestion de projet à un entrepreneur qualifié? Les Constructions Dominic Cyr Inc. est là pour vous, que ce soit pour certaines étapes spécifiques de la construction ou pour un projet clé en main.","image1":{"alt":"Professionnels collaborant sur un projet"},"image2":{"alt":"Rendu de plan d''étage 3D"},"image3":{"alt":"Outils et matériaux de construction"}},"advantages":{"heading":"Il y a de nombreux avantages à nous confier la gestion de votre projet :","item1":"Service conseil","item2":"Conformité avec les règles du bâtiment","item3":"Planification avec tous les intervenants","item4":"Établissement de l''échéancier des travaux","item5":"Contrôle des coûts","item6":"Suivi rigoureux","item7":"Assurance conformité","pricing":"Selon l''ampleur et la complexité du projet, la tarification pourrait être à coût fixe ou à prix majoré. Contactez-nous pour en discuter.","contactLink":"Contactez-nous"},"gallery":{"heading":"Quelques réalisations en gestion de projet par Les Constructions Dominic Cyr Inc.","caption":"Résidence","image1":{"alt":"Résidence"},"image2":{"alt":"Résidence"},"image3":{"alt":"Résidence"}}}');


INSERT INTO app_theme (
    theme_name,
    primary_color,
    secondary_color,
    accent_color,
    card_background,
    background_color,
    text_primary,
    white
) VALUES (
             'default',
             '#4C4D4F',
             '#E6E7E8',
             '#5A7D8C',
             '#AAB2A6',
             '#F5F7FA',
             '#4C4D4F',
             '#FFFFFF'
         );
