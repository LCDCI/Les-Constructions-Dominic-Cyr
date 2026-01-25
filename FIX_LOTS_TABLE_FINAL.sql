-- RUN THIS SQL ON YOUR DATABASE NOW TO FIX THE SCHEMA
-- This will fix the project_id and assigned_customer_id columns

-- 1. Drop the lots table entirely and recreate it
DROP TABLE IF EXISTS lots CASCADE;

-- 2. Create the lots table with correct schema
CREATE TABLE lots (
    id SERIAL PRIMARY KEY,
    lot_identifier VARCHAR(255) NOT NULL UNIQUE,
    lot_number VARCHAR(255) NOT NULL,
    civic_address VARCHAR(500) NOT NULL,
    price NUMERIC(19,2),
    dimensions_square_feet VARCHAR(255),
    dimensions_square_meters VARCHAR(255),
    lot_status VARCHAR(50) NOT NULL,
    project_id BIGINT NOT NULL,
    assigned_customer_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lots_project FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT fk_lots_customer FOREIGN KEY (assigned_customer_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 3. Create indexes for performance
CREATE INDEX idx_lots_project_id ON lots(project_id);
CREATE INDEX idx_lots_assigned_customer ON lots(assigned_customer_id);
CREATE INDEX idx_lots_lot_identifier ON lots(lot_identifier);

-- Verify the schema
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'lots'
ORDER BY ordinal_position;

