-- Add archive fields for marking files as archived instead of deleted
-- Archived files remain in the database for legal/audit purposes but are hidden from the UI

DO $$
BEGIN
    -- Add is_archived column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='files' AND column_name='is_archived') THEN
        ALTER TABLE files ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    -- Add archived_at column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='files' AND column_name='archived_at') THEN
        ALTER TABLE files ADD COLUMN archived_at TIMESTAMP;
    END IF;

    -- Add archived_by column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='files' AND column_name='archived_by') THEN
        ALTER TABLE files ADD COLUMN archived_by VARCHAR(36);
    END IF;

    -- Add CHECK constraint for archived_by UUID format
    IF NOT EXISTS (SELECT 1 FROM pg_constraint 
                   WHERE conname='chk_files_archived_by_uuid') THEN
        ALTER TABLE files
            ADD CONSTRAINT chk_files_archived_by_uuid
            CHECK (
                archived_by IS NULL OR
                archived_by ~ '^[a-zA-Z0-9_-]{1,36}$'
            );
    END IF;
END $$;

-- Create composite indexes for faster archive queries
CREATE INDEX IF NOT EXISTS idx_files_project_archived_created_at ON files(project_id, is_archived, created_at);
CREATE INDEX IF NOT EXISTS idx_files_archived_at ON files(archived_at);

-- Create index for querying only active (not archived) files
CREATE INDEX IF NOT EXISTS idx_files_active_not_archived ON files(is_active, is_archived) WHERE is_active = TRUE AND is_archived = FALSE;
