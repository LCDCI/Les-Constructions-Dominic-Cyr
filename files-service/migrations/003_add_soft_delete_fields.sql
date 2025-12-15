-- Add soft delete fields for audit trail
-- Note: is_active already exists in 001_create_files_table.sql, so we skip it here

DO $$
BEGIN
    -- Add deleted_at column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='files' AND column_name='deleted_at') THEN
        ALTER TABLE files ADD COLUMN deleted_at TIMESTAMP;
    END IF;

    -- Add deleted_by column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='files' AND column_name='deleted_by') THEN
        ALTER TABLE files ADD COLUMN deleted_by VARCHAR(36);
    END IF;

    -- Add CHECK constraint if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM pg_constraint 
                   WHERE conname='chk_files_deleted_by_uuid') THEN
        ALTER TABLE files
            ADD CONSTRAINT chk_files_deleted_by_uuid
            CHECK (
                deleted_by IS NULL OR
                deleted_by ~ '^[a-zA-Z0-9_-]{1,36}$'
            );
    END IF;
END $$;

-- Create composite index for faster queries by project, active status, and creation time
CREATE INDEX IF NOT EXISTS idx_files_project_active_created_at ON files(project_id, is_active, created_at);
CREATE INDEX IF NOT EXISTS idx_files_deleted_at ON files(deleted_at);
