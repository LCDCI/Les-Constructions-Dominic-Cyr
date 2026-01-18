-- Add uploader_role column to files table for role-based filtering
ALTER TABLE files ADD COLUMN IF NOT EXISTS uploader_role VARCHAR(20);

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_files_uploader_role ON files(uploader_role);
