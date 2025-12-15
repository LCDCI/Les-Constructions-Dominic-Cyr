-- Add soft delete fields for audit trail
ALTER TABLE files ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE files ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE files ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(36);

-- Add a CHECK constraint to ensure deleted_by is a valid identifier if not null
ALTER TABLE files
    ADD CONSTRAINT IF NOT EXISTS chk_files_deleted_by_uuid
    CHECK (
        deleted_by IS NULL OR
        deleted_by ~ '^[a-zA-Z0-9_-]{1,36}$'
    );

-- Create composite index for faster queries by project, active status, and creation time
CREATE INDEX IF NOT EXISTS idx_files_project_active_created_at ON files(project_id, is_active, created_at);
CREATE INDEX IF NOT EXISTS idx_files_deleted_at ON files(deleted_at);
