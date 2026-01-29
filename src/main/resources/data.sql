-- LOCAL DEVELOPMENT ONLY - Sample data for testing
-- Not used in production

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
INSERT INTO renovations (renovation_identifier, description)
VALUES ('78cc74d2-0dae-4be9-be91-d3750311da94', 'This renovation includes a complete kitchen remodel with new cabinets, countertops, and appliances, as well as updated flooring throughout the main living areas.');

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
    project_identifier, project_name, project_description, status, start_date, end_date,
    primary_color, tertiary_color, buyer_color, buyer_name, image_identifier,
    customer_id, contractor_id, salesperson_id, location, lot_identifier, progress_percentage
) VALUES
      ('proj-001-foresta', 'Föresta', 'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.', 'IN_PROGRESS', '2025-01-15', '2025-12-31', '#A9B3AA', '#DFE1DF', '#003663', 'Marc Tremblay', 'a93f9fbc-44d6-4c0d-b763-0523ee42656d', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', '104 rue du Boisé, St-Alphonse de Granby, Granby, QC J2J 2X4', 'f3c8837d-bd65-4bc5-9f01-cb9082fc657e', 90),
      ('proj-002-panorama', 'Panorama', 'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.', 'IN_PROGRESS', '2025-02-01', '2026-01-31', '#737373', '#F6F4F1', '#545454', 'Sylvie Lapointe', '47d2b619-70cf-460c-b929-605d52ee9eb6', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'Sutton, Quebec', '5a82954c-8e2c-466a-8a8f-9983b79ede63', 80);

-- data for lots (first 10 for brevity - add more as needed)
INSERT INTO lots (lot_identifier, lot_number, civic_address, price, dimensions_square_feet, dimensions_square_meters, lot_status, project_id) VALUES
                                                                                                                                                  ('046f5c88-5188-467a-85d8-041460f7347a', '6 690 952', '104 rue du Boisé', 0.0, '16944.5', '1574.2', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('923df8f0-1090-48e0-a92c-0e78c4391630', '6 690 953', '108 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('575c8739-16a3-4a11-a89e-2131920dfa1d', '6 690 954', '112 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('143e1da0-e070-496e-a32b-340645601289', '6 690 955', '116 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('36195861-8406-4448-b4c6-e789839443c5', '6 690 956', '120 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('f2b3b793-cc07-4e6c-a87d-84224b172d3e', '6 690 957', '124 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('79477e68-07e5-4f30-81f1-c4be19c351b0', '6 690 958', '128 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('e5ca72c8-895c-44c1-8840-a15d65457ef4', '6 690 959', '132 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('225c957a-9a00-410a-8646-6f81f9b31953', '6 690 960', '136 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta'),
                                                                                                                                                  ('30c51f49-55da-48eb-a9eb-9da21b443213', '', '140 rue du Boisé', 0.0, '16145.9', '1500.0', 'AVAILABLE', 'proj-001-foresta');

-- data for schedules
INSERT INTO schedules (schedule_identifier, schedule_start_date, schedule_end_date, schedule_description, lot_number, project_id) VALUES
                                                                                                                                      ('SCH-001', '2026-01-20', '2026-01-20', 'Foundation Work - Pour Concrete', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-002', '2026-01-22', '2026-01-22', 'Framing - Install Floor Joists', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-003', '2026-01-25', '2026-01-25', 'Framing - Wall Installation', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-004', '2026-01-28', '2026-01-28', 'Roofing - Sheathing', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-005', '2026-02-01', '2026-02-01', 'Roofing - Shingles Installation', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-006', '2026-02-05', '2026-02-05', 'Plumbing - Rough-In', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-007', '2026-02-08', '2026-02-08', 'Electrical - Rough Wiring', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-008', '2026-02-12', '2026-02-12', 'HVAC Installation', 'Lot 53', 'proj-001-foresta'),
                                                                                                                                      ('SCH-009', '2026-01-23', '2026-01-23', 'Site Preparation - Excavation', 'Lot 57', 'proj-002-panorama'),
                                                                                                                                      ('SCH-010', '2026-01-27', '2026-01-27', 'Foundation - Footings', 'Lot 57', 'proj-002-panorama');

-- data for users
INSERT INTO users (user_id, first_name, last_name, email, secondary_email, phone, user_role, user_status, auth0user_id) VALUES
                                                                                                                            ('11111111-1111-1111-1111-111111111111', 'John', 'Owner', 'owner@test.com', NULL, '514-111-1111', 'OWNER', 'ACTIVE', 'auth0|69542f38c08232af729f3d41'),
                                                                                                                            ('22222222-2222-2222-2222-222222222222', 'Jane', 'Contractor', 'contractor@test.com', NULL, '514-222-2222', 'CONTRACTOR', 'ACTIVE', 'auth0|693faa68bf9e92b3d445c4ab'),
                                                                                                                            ('33333333-3333-3333-3333-333333333333', 'Bob', 'Sales', 'sales@test.com', NULL, '514-333-3333', 'SALESPERSON', 'ACTIVE', 'auth0|693faa7d743287e135a703e1'),
                                                                                                                            ('44444444-4444-4444-4444-444444444444', 'Alice', 'Customer', 'customer@test.com', NULL, '514-444-4444', 'CUSTOMER', 'ACTIVE', 'auth0|693faa95743287e135a703e8');

-- data for tasks
INSERT INTO tasks (task_identifier, task_status, task_title, period_start, period_end, task_description, task_priority, estimated_hours, hours_spent, task_progress, assigned_user_id, schedule_id) VALUES
                                                                                                                                                                                                        ('TASK-001', 'TO_DO', 'Install Foundation', '2025-12-05', '2025-12-08', 'Pour concrete foundation for Lot 53', 'HIGH', 16.0, 0.0, 0.0, '22222222-2222-2222-2222-222222222222', 'SCH-001'),
                                                                                                                                                                                                        ('TASK-002', 'IN_PROGRESS', 'Framing Work', '2025-12-09', '2025-12-15', 'Complete structural framing for Lot 53', 'VERY_HIGH', 40.0, 15.0, 37.5, '22222222-2222-2222-2222-222222222222', 'SCH-001'),
                                                                                                                                                                                                        ('TASK-003', 'TO_DO', 'Electrical Rough-In', '2025-12-12', '2025-12-14', 'Install electrical wiring for Lot 54', 'MEDIUM', 20.0, 0.0, 0.0, '22222222-2222-2222-2222-222222222222', 'SCH-001'),
                                                                                                                                                                                                        ('TASK-004', 'COMPLETED', 'Site Preparation', '2025-11-28', '2025-11-30', 'Clear and level site for Lot 57', 'HIGH', 12.0, 12.0, 100.0, '22222222-2222-2222-2222-222222222222', 'SCH-001'),
                                                                                                                                                                                                        ('TASK-005', 'TO_DO', 'Plumbing Installation', '2025-12-16', '2025-12-20', 'Install main plumbing lines for Lot 57', 'HIGH', 24.0, 0.0, 0.0, '22222222-2222-2222-2222-222222222222', 'SCH-001');

-- Project overview content
INSERT INTO project_overview_content (
    project_identifier, hero_title, hero_subtitle, hero_description,
    overview_section_title, overview_section_content, features_section_title,
    location_section_title, location_description, location_address,
    location_map_embed_url, gallery_section_title,
    location_latitude, location_longitude
) VALUES (
             'proj-001-foresta', 'FÖRESTA', 'Résidences Secondaires en Harmonie avec la Nature',
             'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant realizations.',
             'About Foresta', 'Foresta represents a unique opportunity to own a secondary residence in one of Quebec''s most tranquil settings. Each home is designed with sustainability and comfort in mind, featuring modern amenities while preserving the natural beauty of the surrounding forest.',
             'Project Features', 'Location',
             'Foresta is located in St-Alphonse-de-Granby, offering easy access to nature trails, local amenities, and major highways. The perfect balance between seclusion and convenience.',
             '104 rue du Boisé, St-Alphonse de Granby, Granby, QC J2J 2X4',
             'https://www.google.com/maps/place/104+Rue+du+Boisé,+St-Alphonse+de+Granby,+QC+J2J+2X4',
             'Project Gallery', 45.32027369731277, -72.79834091605535
         ), (
             'proj-002-panorama', 'PANORAMA', 'Condos Modernes avec Vue Imprenable',
             'Experience elevated living with Panorama condos. Featuring contemporary design and breathtaking views, this project combines urban convenience with natural serenity.',
             'About Panorama', 'Panorama condos offer a modern lifestyle with spacious layouts, premium finishes, and access to shared amenities. Each unit is designed to maximize natural light and views of the surrounding landscape.',
             'Condo Features', 'Location',
             'Strategically located to provide both tranquility and easy access to urban centers, Panorama offers the best of both worlds for discerning buyers.',
             'Sutton, Quebec',
             'https://www.google.com/maps/place/630+Rue+Maple,+Sutton,+QC+J0E+2K0',
             'Gallery', 45.102386912240014, -72.57423759390365
         );

-- Project features
INSERT INTO project_features (project_identifier, feature_title, feature_description, display_order) VALUES
                                                                                                         ('proj-001-foresta', 'New Realizations', 'Contemporary architecture with traditional charm', 1),
                                                                                                         ('proj-001-foresta', 'Living Environment', 'Surrounded by pristine forest and nature trails', 2),
                                                                                                         ('proj-001-foresta', 'Lots', 'A variety of lots available for all', 3),
                                                                                                         ('proj-002-panorama', 'New Realizations', 'Contemporary architecture with traditional charm', 1),
                                                                                                         ('proj-002-panorama', 'Living Environment', 'Surrounded by pristine forest and nature trails', 2),
                                                                                                         ('proj-002-panorama', 'Lots', 'A variety of lots available for all', 3);

-- Gallery images
INSERT INTO project_gallery_images (project_identifier, image_identifier, image_caption, display_order) VALUES
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Exterior View', 1),
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Interior Living Space', 2),
                                                                                                            ('proj-001-foresta', 'dcada4a5-aa19-4346-934e-1e57bc0f9e1f', 'Kitchen', 3),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Building Exterior', 1),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Condo Interior', 2),
                                                                                                            ('proj-002-panorama', 'ee576ed6-5d56-4d54-ba25-7157f7b75d0d', 'Amenities', 3);

-- Project management page content (English)
INSERT INTO project_management_page_content (language, content_json) VALUES
                                                                         ('en', '{"hero":{"line1":"PROJECT MANAGEMENT,","line2":"FOR PEACE OF MIND"},"intro":{"heading":{"title":"Planning, organization, and site follow-up without the friction","line1":"PLANNING","line2":"ORGANIZATION","line3":"WORK MONITORING"},"tagline":"We take care of it!","paragraph":"You own a piece of land and want to entrust project management to a qualified contractor? Les Constructions Dominic Cyr Inc. is here for you, whether for specific construction stages or a turnkey project.","image1":{"alt":"Professionals collaborating on project"},"image2":{"alt":"3D floor plan rendering"},"image3":{"alt":"Construction tools and materials"}},"advantages":{"heading":"There are many advantages to entrusting us with the management of your project:","item1":"Consulting service","item2":"Compliance with building regulations","item3":"Planning with all stakeholders","item4":"Establishment of the work schedule","item5":"Cost control","item6":"Rigorous monitoring","item7":"Compliance assurance","pricing":"Depending on the scope and complexity of the project, pricing could be at a fixed cost or at a marked-up price. Contact us to discuss.","contactLink":"Contact us"},"gallery":{"heading":"Some project management achievements by Les Constructions Dominic Cyr Inc.","caption":"Residence","image1":{"alt":"Residence"},"image2":{"alt":"Residence"},"image3":{"alt":"Residence"}}}'),
                                                                         ('fr', '{"hero":{"line1":"LA GESTION DE PROJET,","line2":"POUR UNE TRANQUILLITÉ D''ESPRIT"},"intro":{"heading":{"title":"Planification, organisation et suivi de site sans friction","line1":"PLANIFICATION","line2":"ORGANISATION","line3":"SUIVI DES TRAVAUX"},"tagline":"On s''en occupe !","paragraph":"Vous êtes propriétaire d''un terrain et vous souhaitez confier la gestion de projet à un entrepreneur qualifié? Les Constructions Dominic Cyr Inc. est là pour vous, que ce soit pour certaines étapes spécifiques de la construction ou pour un projet clé en main.","image1":{"alt":"Professionnels collaborant sur un projet"},"image2":{"alt":"Rendu de plan d''étage 3D"},"image3":{"alt":"Outils et matériaux de construction"}},"advantages":{"heading":"Il y a de nombreux avantages à nous confier la gestion de votre projet :","item1":"Service conseil","item2":"Conformité avec les règles du bâtiment","item3":"Planification avec tous les intervenants","item4":"Établissement de l''échéancier des travaux","item5":"Contrôle des coûts","item6":"Suivi rigoureux","item7":"Assurance conformité","pricing":"Selon l''ampleur et la complexité du projet, la tarification pourrait être à coût fixe ou à prix majoré. Contactez-nous pour en discuter.","contactLink":"Contactez-nous"},"gallery":{"heading":"Quelques réalisations en gestion de projet par Les Constructions Dominic Cyr Inc.","caption":"Résidence","image1":{"alt":"Résidence"},"image2":{"alt":"Résidence"},"image3":{"alt":"Résidence"}}}');

-- App theme
INSERT INTO app_theme (theme_name, primary_color, secondary_color, accent_color, card_background, background_color, text_primary, white) VALUES
    ('default', '#4C4D4F', '#E6E7E8', '#5A7D8C', '#AAB2A6', '#F5F7FA', '#4C4D4F', '#FFFFFF');

-- sample data to test inquiries owner page
INSERT INTO inquiries (name, email, phone, message, created_at) VALUES
                                                                    ('Jean Dupont', 'jean.dupont@email.com', '514-555-0123', 'Je suis intéressé par le projet Foresta. Pouvez-vous me fournir plus d''informations?', CURRENT_TIMESTAMP - INTERVAL '5 days'),
                                                                    ('Marie Lambert', 'marie.lambert@email.com', '450-555-0234', 'I would like to schedule a visit to see the available lots.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
                                                                    ('Pierre Tremblay', 'pierre.tremblay@email.com', '514-555-0345', 'Quels sont les délais de construction pour un nouveau projet?', CURRENT_TIMESTAMP - INTERVAL '2 days'),
                                                                    ('Sophie Martin', 'sophie.martin@email.com', '', 'Can you provide information about your renovation services?', CURRENT_TIMESTAMP - INTERVAL '1 day'),
                                                                    ('Luc Gagnon', 'luc.gagnon@email.com', '418-555-0456', 'Je voudrais obtenir un devis pour la construction d''une maison unifamiliale.', CURRENT_TIMESTAMP - INTERVAL '6 hours');

-- Living Environment Content - English
INSERT INTO living_environment_content (project_identifier, language, header_title, header_subtitle, header_subtitle_last, header_tagline, description_text, proximity_title, footer_text) VALUES
                                                                                                                                                                                               ('proj-001-foresta', 'en', 'FÖRESTA, AN', 'EXCEPTIONAL', 'LIVING ENVIRONMENT', 'At the Pace of Nature',
                                                                                                                                                                                                'An exceptional living environment for those who dream of escaping everyday life and reconnecting with what matters most, the Föresta project offers the opportunity to live in a home at the heart of nature. This new neighborhood is located 5 km from Highway 10 (A-10), 15 minutes from downtown Granby, and within reach of a wide range of services and activities. Outdoor enthusiasts will be delighted.',
                                                                                                                                                                                                'CLOSE TO EVERYTHING!', 'FÖRESTA is a project by Les Constructions Dominic Cyr inc.'),
                                                                                                                                                                                               ('proj-001-foresta', 'fr', 'FÖRESTA, UN', 'MILIEU DE VIE', 'EXCEPTIONNEL', 'Au rythme de la nature',
                                                                                                                                                                                                'Un milieu de vie exceptionnel pour ceux qui rêvent de s''évader du quotidien et de se reconnecter à l''essentiel, le projet Föresta offre l''opportunité d''habiter une demeure au cœur de la nature. Ce nouveau quartier est situé à 5 km de l''autoroute 10 (A-10), à 15 minutes du centre-ville de Granby et à portée de main d''une panoplie de services et d''activités. Les adeptes de plein air seront comblés.',
                                                                                                                                                                                                'À PROXIMITÉ DE TOUT!', 'FÖRESTA est un projet signé Les Constructions Dominic Cyr inc.'),
                                                                                                                                                                                               ('proj-002-panorama', 'en', 'AUTHENTICITY', 'AT THE HEART', 'OF THE MOUNTAIN', 'Mountain Living',
                                                                                                                                                                                                'The Panorama project is located in the charming village of Sutton in the Eastern Townships. The region is recognized for its warm atmosphere and exceptional natural setting. It is renowned for skiing, hiking, and outdoor activities in all seasons and attracts both nature lovers and visitors seeking tranquility. Its lively village center, its cafés, galleries, and local shops give it a friendly and authentic atmosphere, where it is good to live all year round!',
                                                                                                                                                                                                'FOR OUTDOOR LOVERS!', 'PANORAMA is a project by Les Constructions Dominic Cyr inc.'),
                                                                                                                                                                                               ('proj-002-panorama', 'fr', 'L''AUTHENTICITÉ', 'AU COEUR', 'DE LA MONTAGNE', 'La vie en montagne',
                                                                                                                                                                                                'Le projet Panorama est situé dans le charmant village de Sutton dans les Cantons-de-l''Est. La région est reconnue pour son ambiance chaleureuse et son cadre naturel exceptionnel. Elle est réputée pour le ski, la randonnée et le plein air en toutes saisons et attire autant les amateurs de nature que les visiteurs en quête de tranquillité. Son cœur villageois animé, ses cafés, galeries et commerces locaux lui donnent une atmosphère conviviale et authentique, où il fait bon vivre toute l''année!',
                                                                                                                                                                                                'POUR LES AMATEURS DE PLEIN-AIR!', 'PANORAMA est un projet signé Les Constructions Dominic Cyr inc.');

-- Living Environment Amenities - Foresta English
INSERT INTO living_environment_amenities (living_environment_content_id, amenity_key, amenity_label, display_order) VALUES
                                                                                                                        ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'ski', 'Skiing', 1),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'golf', 'Golf', 2),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'bike', 'Cycling', 3),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'bromont', 'Bromont', 4),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'yamaska', 'Yamaska National Park', 5),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'vineyards', 'Orchards & Vineyards', 6),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'restaurants', 'Restaurants', 7),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'groceries', 'Groceries', 8),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'stores', 'Shops', 9),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'hospitals', 'Hospitals', 10),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'schools', 'Schools', 11),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'en'), 'spas', 'Spas', 12);

-- Living Environment Amenities - Foresta French
INSERT INTO living_environment_amenities (living_environment_content_id, amenity_key, amenity_label, display_order) VALUES
                                                                                                                        ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'ski', 'Ski', 1),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'golf', 'Golf', 2),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'bike', 'Vélo', 3),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'bromont', 'Bromont', 4),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'yamaska', 'Parc National de la Yamaska', 5),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'vineyards', 'Vergers & Vignobles', 6),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'restaurants', 'Restaurants', 7),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'groceries', 'Épiceries', 8),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'stores', 'Magasins', 9),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'hospitals', 'Hôpitaux', 10),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'schools', 'Écoles', 11),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-001-foresta' AND language = 'fr'), 'spas', 'Spas', 12);

-- Living Environment Amenities - Panorama English
INSERT INTO living_environment_amenities (living_environment_content_id, amenity_key, amenity_label, display_order) VALUES
                                                                                                                        ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'ski', 'Skiing at Sutton', 1),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'golf', 'Golf', 2),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'bike', 'Cycling', 3),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'vineyards', 'Vineyards', 4),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'crosscountry', 'Cross-Country Skiing & Snowshoeing', 5),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'en'), 'hiking', 'Hiking', 6);

-- Living Environment Amenities - Panorama French
INSERT INTO living_environment_amenities (living_environment_content_id, amenity_key, amenity_label, display_order) VALUES
                                                                                                                        ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'ski', 'Ski à Sutton', 1),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'golf', 'Golf', 2),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'bike', 'Vélo', 3),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'vineyards', 'Vignobles', 4),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'crosscountry', 'Ski de fond et raquette', 5),
    ((SELECT id FROM living_environment_content WHERE project_identifier = 'proj-002-panorama' AND language = 'fr'), 'hiking', 'Randonnées', 6);