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