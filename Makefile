.PHONY: build up init-images rebuild

build:
	docker-compose up --build -d
	@echo "Waiting for services..."
	@sleep 30
	@make init-images

up:
	docker-compose up -d

init-images:
	@echo "Initializing file records..."
	@docker exec -i files-db psql -U admin -d filesdb << 'SQL'
	INSERT INTO files (id, project_id, file_name, content_type, category, size, object_key, created_at, is_active, uploaded_by)
	VALUES 
	('dcada4a5-aa19-4346-934e-1e57bc0f9e1f', '', 'foresta.png', 'image/png', 'PHOTO', 22528, 'photos/global/2025-12-05/dcada4a5-aa19-4346-934e-1e57bc0f9e1f', '2025-12-05 05:02:19', true, 'demo-owner-001'),
	('4215eb96-af4b-492b-870a-6925e78b7fcc', '', 'Otryminc. png', 'image/png', 'PHOTO', 696320, 'photos/global/2025-12-05/4215eb96-af4b-492b-870a-6925e78b7fcc', '2025-12-05 05:02:45', true, 'demo-owner-001'),
	('ee576ed6-5d56-4d54-ba25-7157f7b75d0d', '', 'Naturest.png', 'image/png', 'PHOTO', 705536, 'photos/global/2025-12-05/ee576ed6-5d56-4d54-ba25-7157f7b75d0d', '2025-12-05 05:03:08', true, 'demo-owner-001')
	ON CONFLICT (id) DO NOTHING;
	SQL
	@echo "Linking images to projects..."
	@curl -s -X PUT http://localhost:8080/api/v1/projects/proj-001-foresta -H "Content-Type: application/json" -d '{"imageIdentifier": "dcada4a5-aa19-4346-934e-1e57bc0f9e1f"}' > /dev/null
	@curl -s -X PUT http://localhost:8080/api/v1/projects/proj-003-otryminc -H "Content-Type: application/json" -d '{"imageIdentifier": "4215eb96-af4b-492b-870a-6925e78b7fcc"}' > /dev/null
	@curl -s -X PUT http://localhost:8080/api/v1/projects/proj-002-naturest -H "Content-Type: application/json" -d '{"imageIdentifier": "ee576ed6-5d56-4d54-ba25-7157f7b75d0d"}' > /dev/null
	@echo "✅ Images initialized!"

rebuild:
	docker-compose down
	@make build
