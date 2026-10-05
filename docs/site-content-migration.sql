-- Site content migration and seed
-- Run against the application PostgreSQL database.
-- Safe to rerun: rows are upserted by page_group, page_key, and language.
-- Editable content excludes buttons, modals, and external components.

CREATE TABLE IF NOT EXISTS site_content (
    id BIGSERIAL PRIMARY KEY,
    page_group VARCHAR(100) NOT NULL,
    page_key VARCHAR(150) NOT NULL,
    language VARCHAR(10) NOT NULL,
    content_text TEXT,
    image_identifier VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_site_content_page_language UNIQUE (page_group, page_key, language)
);

CREATE INDEX IF NOT EXISTS idx_site_content_language_group
    ON site_content(language, page_group, sort_order);

INSERT INTO site_content (page_group, page_key, language, content_text, image_identifier, sort_order)
VALUES
    ('home', 'hero.title', 'en', 'Crafting Your Dream Space', NULL, 10),
    ('home', 'hero.title', 'fr', 'Créons votre espace de rêve', NULL, 10),
    ('home', 'hero.subtitle', 'en', 'Quality construction, timeless design, since 2006.', NULL, 20),
    ('home', 'hero.subtitle', 'fr', 'Construction de qualité et design intemporel depuis 2006.', NULL, 20),
    ('home', 'hero.image', 'en', NULL, '0313008f-b07c-4392-824f-45ff11a2d7a3', 30),
    ('home', 'hero.image', 'fr', NULL, '0313008f-b07c-4392-824f-45ff11a2d7a3', 30),

    ('residential-projects', 'hero.title', 'en', 'Residential Projects', NULL, 10),
    ('residential-projects', 'hero.title', 'fr', 'Projets résidentiels', NULL, 10),
    ('residential-projects', 'hero.subtitle', 'en', 'Explore our portfolio of residential projects showcasing quality construction and innovative design.', NULL, 20),
    ('residential-projects', 'hero.subtitle', 'fr', 'Découvrez notre portefeuille de projets résidentiels alliant construction de qualité et design innovant.', NULL, 20),

    ('project-overview:proj-001-foresta', 'hero.title', 'en', 'Foresta', NULL, 10),
    ('project-overview:proj-001-foresta', 'hero.title', 'fr', 'Foresta', NULL, 10),
    ('project-overview:proj-001-foresta', 'hero.subtitle', 'en', 'In rhythm with nature', NULL, 20),
    ('project-overview:proj-001-foresta', 'hero.subtitle', 'fr', 'Au rythme de la nature', NULL, 20),
    ('project-overview:proj-001-foresta', 'hero.image', 'en', NULL, 'a93f9fbc-44d6-4c0d-b763-0523ee42656d', 30),
    ('project-overview:proj-001-foresta', 'hero.image', 'fr', NULL, 'a93f9fbc-44d6-4c0d-b763-0523ee42656d', 30),
    ('project-overview:proj-001-foresta', 'location.title', 'en', 'Location', NULL, 40),
    ('project-overview:proj-001-foresta', 'location.title', 'fr', 'Emplacement', NULL, 40),
    ('project-overview:proj-001-foresta', 'features.livingEnvironment', 'en', 'Living Environment', NULL, 50),
    ('project-overview:proj-001-foresta', 'features.livingEnvironment', 'fr', 'Milieu de vie', NULL, 50),
    ('project-overview:proj-001-foresta', 'features.houses', 'en', 'Houses', NULL, 60),
    ('project-overview:proj-001-foresta', 'features.houses', 'fr', 'Maisons', NULL, 60),
    ('project-overview:proj-001-foresta', 'features.lots', 'en', 'Lots', NULL, 70),
    ('project-overview:proj-001-foresta', 'features.lots', 'fr', 'Terrains', NULL, 70),

    ('project-overview:proj-002-panorama', 'hero.title', 'en', 'Panorama', NULL, 10),
    ('project-overview:proj-002-panorama', 'hero.title', 'fr', 'Panorama', NULL, 10),
    ('project-overview:proj-002-panorama', 'hero.subtitle', 'en', 'Luxury and nature united', NULL, 20),
    ('project-overview:proj-002-panorama', 'hero.subtitle', 'fr', 'Luxe et nature réunis', NULL, 20),
    ('project-overview:proj-002-panorama', 'hero.image', 'en', NULL, '47d2b619-70cf-460c-b929-605d52ee9eb6', 30),
    ('project-overview:proj-002-panorama', 'hero.image', 'fr', NULL, '47d2b619-70cf-460c-b929-605d52ee9eb6', 30),
    ('project-overview:proj-002-panorama', 'features.livingEnvironment', 'en', 'Living Environment', NULL, 50),
    ('project-overview:proj-002-panorama', 'features.livingEnvironment', 'fr', 'Milieu de vie', NULL, 50),
    ('project-overview:proj-002-panorama', 'features.houses', 'en', 'Houses', NULL, 60),
    ('project-overview:proj-002-panorama', 'features.houses', 'fr', 'Maisons', NULL, 60),
    ('project-overview:proj-002-panorama', 'features.lots', 'en', 'Lots', NULL, 70),
    ('project-overview:proj-002-panorama', 'features.lots', 'fr', 'Terrains', NULL, 70)
ON CONFLICT (page_group, page_key, language) DO UPDATE SET
    content_text = EXCLUDED.content_text,
    image_identifier = EXCLUDED.image_identifier,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- Verification query
SELECT page_group, language, COUNT(*) AS content_items
FROM site_content
GROUP BY page_group, language
ORDER BY page_group, language;
