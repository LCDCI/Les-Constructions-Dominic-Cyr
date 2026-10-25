-- Remove metadata-only image records from the original seed migration.
-- These rows referenced objects that were never uploaded to Spaces.
DELETE FROM files
WHERE id IN (
    'dcada4a5-aa19-4346-934e-1e57bc0f9e1f',
    '4215eb96-af4b-492b-870a-6925e78b7fcc',
    'ee576ed6-5d56-4d54-ba25-7157f7b75d0d',
    'ae470899-8adf-49f9-b170-5c1ea611493a'
)
AND object_key LIKE 'photos/global/2025-12-05/%';
