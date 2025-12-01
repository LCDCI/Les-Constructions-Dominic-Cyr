-- schema for lot
CREATE TABLE lot (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     lot_identifier VARCHAR(36) NOT NULL UNIQUE,
                     location VARCHAR(255) NOT NULL,
                     price FLOAT NOT NULL,
                     dimensions VARCHAR(255) NOT NULL,
                     status VARCHAR(50) NOT NULL
);