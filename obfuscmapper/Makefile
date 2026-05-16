.PHONY: help install up down migrate runserver worker web test build-parser scan-pilot

help:
	@echo "Targets:"
	@echo "  install      - install backend + frontend + build JAR"
	@echo "  up           - docker compose up -d (postgres + redis)"
	@echo "  down         - docker compose down"
	@echo "  migrate      - django migrate"
	@echo "  runserver    - django runserver 0.0.0.0:8000"
	@echo "  worker       - celery worker"
	@echo "  web          - vite dev server"
	@echo "  test         - pytest backend + vitest frontend"
	@echo "  build-parser - mvn package the JAR"
	@echo "  scan-pilot   - run JAR against Converter-unobf"

install:
	cd backend && python -m venv .venv && .venv/Scripts/python -m pip install -r requirements-dev.txt
	cd frontend && npm install
	$(MAKE) build-parser

up:
	docker compose up -d
down:
	docker compose down

migrate:
	cd backend && .venv/Scripts/python manage.py migrate

runserver:
	cd backend && .venv/Scripts/python manage.py runserver 0.0.0.0:8000

worker:
	cd backend && .venv/Scripts/celery -A obfuscmapper worker -l info -c 2

web:
	cd frontend && npm run dev

test:
	cd backend && .venv/Scripts/python -m pytest -q
	cd frontend && npx tsc --noEmit
	cd parser && mvn -q test

build-parser:
	cd parser && mvn -q -DskipTests package

scan-pilot:
	cd parser && java -Xmx400m -jar target/obfusc-parser.jar -p "../../Converter-unobf" -o "target/scan.json"
