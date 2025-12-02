-- Remember to use PostgresSQL syntax in this file or else it won't work!

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