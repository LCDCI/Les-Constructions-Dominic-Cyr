-- Remember to use PostgreSQL syntax in this file or else it won't work!

-- schema for lot
DROP TABLE IF EXISTS lots;

CREATE TABLE lots (
                     id SERIAL PRIMARY KEY,
                     lot_identifier UUID NOT NULL UNIQUE,
                     location VARCHAR(255) NOT NULL,
                     price REAL NOT NULL, -- Use REAL for FLOAT compatibility
                     dimensions VARCHAR(255) NOT NULL,
                     lot_status VARCHAR(50) NOT NULL
);

-- schema for house
DROP TABLE IF EXISTS houses;
CREATE TABLE houses (
                    id SERIAL PRIMARY KEY,
                    house_identifier UUID NOT NULL UNIQUE,
                    house_name VARCHAR(255) NOT NULL,
                    location VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    number_of_rooms INTEGER NOT NULL,
                    number_of_bedrooms INTEGER NOT NULL,
                    number_of_bathrooms INTEGER NOT NULL,
                    construction_year INTEGER NOT NULL
);

-- schema for footer
DROP TABLE IF EXISTS footer_content CASCADE;
DROP TABLE IF EXISTS projects CASCADE;

CREATE TABLE footer_content (
id SERIAL PRIMARY KEY,
section VARCHAR(50) NOT NULL,
label VARCHAR(100) NOT NULL,
value TEXT NOT NULL,
display_order INT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_footer_section ON footer_content(section);
CREATE INDEX idx_footer_display_order ON footer_content(display_order);

-- schema for projects
CREATE TABLE projects (
project_id BIGSERIAL PRIMARY KEY,
project_identifier VARCHAR(255) NOT NULL UNIQUE,
project_name VARCHAR(255) NOT NULL,
project_description TEXT,
status VARCHAR(50) NOT NULL,
start_date DATE NOT NULL,
end_date DATE,
completion_date DATE,
primary_color VARCHAR(7) NOT NULL,
tertiary_color VARCHAR(7) NOT NULL,
buyer_color VARCHAR(7) NOT NULL,
buyer_name VARCHAR(255) NOT NULL,
image_identifier VARCHAR(255),
customer_id VARCHAR(255) NOT NULL,
lot_identifier VARCHAR(255) NOT NULL,
progress_percentage INT,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_identifier ON projects(project_identifier);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_customer_id ON projects(customer_id);
CREATE INDEX idx_project_start_date ON projects(start_date);
CREATE INDEX idx_project_end_date ON projects(end_date);
