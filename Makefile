.PHONY: build up init-images rebuild

build:
	docker-compose up --build -d
	@echo "Waiting for services..."
	@sleep 30
	@make init-images

up:
	docker-compose up -d

init-images:
	@echo "Images must be uploaded through the application upload modal."

rebuild:
	docker-compose down
	@make build
