INSERT INTO files (id, project_id, file_name, content_type, category, size, object_key, created_at, is_active, uploaded_by)
VALUES 
('dcada4a5-aa19-4346-934e-1e57bc0f9e1f', '', 'foresta.png', 'image/png', 'PHOTO', 22528, 'photos/global/2025-12-05/dcada4a5-aa19-4346-934e-1e57bc0f9e1f', NOW(), true, 'system'),
('4215eb96-af4b-492b-870a-6925e78b7fcc', '', 'Otryminc.png', 'image/png', 'PHOTO', 696320, 'photos/global/2025-12-05/4215eb96-af4b-492b-870a-6925e78b7fcc', NOW(), true, 'system'),
('ee576ed6-5d56-4d54-ba25-7157f7b75d0d', '', 'Naturest.png', 'image/png', 'PHOTO', 705536, 'photos/global/2025-12-05/ee576ed6-5d56-4d54-ba25-7157f7b75d0d', NOW(), true, 'system'),
('ae470899-8adf-49f9-b170-5c1ea611493a', '', 'dcyr.png', 'image/png', 'PHOTO', 211968, 'photos/global/2025-12-05/ae470899-8adf-49f9-b170-5c1ea611493a', NOW(), true, 'system'),
-- Add sample documents for testing
('doc-001-blueprint', 'BILL-223067', 'Blueprint_V1.pdf', 'application/pdf', 'DOCUMENT', 524288, 'documents/BILL-223067/doc-001-blueprint', NOW(), true, 'architect@example.com'),
('doc-002-contract', 'BILL-223067', 'Construction_Contract.pdf', 'application/pdf', 'DOCUMENT', 245760, 'documents/BILL-223067/doc-002-contract', NOW() - INTERVAL '5 days', true, 'owner@example.com'),
('doc-003-permit', 'BILL-223067', 'Building_Permit.pdf', 'application/pdf', 'DOCUMENT', 102400, 'documents/BILL-223067/doc-003-permit', NOW() - INTERVAL '10 days', true, 'admin@example.com')
ON CONFLICT (id) DO NOTHING;
