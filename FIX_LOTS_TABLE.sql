-- FIX LOTS TABLE SCHEMA
-- Run this SQL on your PostgreSQL database to fix the project_id and assigned_customer_id columns

-- Step 1: Drop existing foreign key constraints
ALTER TABLE lots DROP CONSTRAINT IF EXISTS lots_project_id_fkey;
ALTER TABLE lots DROP CONSTRAINT IF EXISTS fk_lots_project;
ALTER TABLE lots DROP CONSTRAINT IF EXISTS lots_assigned_customer_id_fkey;
ALTER TABLE lots DROP CONSTRAINT IF EXISTS fk_lots_assigned_customer;

-- Step 2: Drop the project_id column entirely and recreate it with correct type
ALTER TABLE lots DROP COLUMN IF EXISTS project_id;
ALTER TABLE lots ADD COLUMN project_id BIGINT;

-- Step 3: Update project_id to reference actual project IDs
-- This sets all lots to reference the first project (you may need to adjust)
UPDATE lots SET project_id = (SELECT project_id FROM projects LIMIT 1) WHERE project_id IS NULL;

-- Step 4: Make project_id NOT NULL after setting values
ALTER TABLE lots ALTER COLUMN project_id SET NOT NULL;

-- Step 5: Add the correct foreign key constraint
ALTER TABLE lots ADD CONSTRAINT lots_project_id_fkey
    FOREIGN KEY (project_id) REFERENCES projects(project_id);

-- Step 6: Ensure assigned_customer_id column exists with correct type
-- It should be UUID type to match users.user_id
ALTER TABLE lots DROP COLUMN IF EXISTS assigned_customer_id;
ALTER TABLE lots ADD COLUMN assigned_customer_id UUID;

-- Step 7: Add foreign key for assigned_customer_id (optional)
ALTER TABLE lots ADD CONSTRAINT lots_assigned_customer_id_fkey
    FOREIGN KEY (assigned_customer_id) REFERENCES users(user_id) ON DELETE SET NULL;

-- Verify the changes
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'lots'
ORDER BY ordinal_position;

