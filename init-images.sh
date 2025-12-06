#!/bin/bash

echo "Initializing images and logo..."

# Re-insert file records (including project images and company logo)
docker exec -i files-db psql -U admin -d filesdb << 'SQL'
INSERT INTO files (id, project_id, file_name, content_type, category, size, object_key, created_at, is_active, uploaded_by)
VALUES 
('dcada4a5-aa19-4346-934e-1e57bc0f9e1f', '', 'foresta.png', 'image/png', 'PHOTO', 22528, 'photos/global/2025-12-05/dcada4a5-aa19-4346-934e-1e57bc0f9e1f', NOW(), true, 'system'),
('4215eb96-af4b-492b-870a-6925e78b7fcc', '', 'Otryminc.png', 'image/png', 'PHOTO', 696320, 'photos/global/2025-12-05/4215eb96-af4b-492b-870a-6925e78b7fcc', NOW(), true, 'system'),
('ee576ed6-5d56-4d54-ba25-7157f7b75d0d', '', 'Naturest.png', 'image/png', 'PHOTO', 705536, 'photos/global/2025-12-05/ee576ed6-5d56-4d54-ba25-7157f7b75d0d', NOW(), true, 'system'),
('ae470899-8adf-49f9-b170-5c1ea611493a', '', 'dcyr.png', 'image/png', 'PHOTO', 211968, 'photos/global/2025-12-05/ae470899-8adf-49f9-b170-5c1ea611493a', NOW(), true, 'system')
ON CONFLICT (id) DO NOTHING;
SQL

echo "Waiting for services to be ready..."
sleep 30

echo "Linking images to projects..."
curl -X PUT http://localhost:8080/api/v1/projects/proj-001-foresta \
  -H "Content-Type: application/json" \
  -d '{"imageIdentifier": "dcada4a5-aa19-4346-934e-1e57bc0f9e1f"}' \
  > /dev/null 2>&1

curl -X PUT http://localhost:8080/api/v1/projects/proj-003-otryminc \
  -H "Content-Type: application/json" \
  -d '{"imageIdentifier": "4215eb96-af4b-492b-870a-6925e78b7fcc"}' \
  > /dev/null 2>&1

curl -X PUT http://localhost:8080/api/v1/projects/proj-002-naturest \
  -H "Content-Type: application/json" \
  -d '{"imageIdentifier": "ee576ed6-5d56-4d54-ba25-7157f7b75d0d"}' \
  > /dev/null 2>&1

echo "Images linked successfully!"
