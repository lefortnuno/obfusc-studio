# ObfuscMapper

Application web de gestion du mapping d obfuscation entre un projet source lisible et un projet cible obfusque.
Voir AUTO_OBFUSCATION.md (specification) et PLAN.md (plan d execution).

## Stack

- Backend : Django 5 + DRF + Celery (Redis)
- Frontend : React 18 + TypeScript + Vite
- Parser : JAR Java 17 + JavaParser
- DB : SQLite en dev, PostgreSQL en prod

## Demarrage rapide

Pre-requis : Python 3.11+, Node 20+, JDK 17+, Maven 3.8+.

```
source scripts/dev-env.sh   # Windows git-bash : charge JAVA_HOME et mvn
make install                # cree venv + npm install + build JAR
make migrate                # cree la BDD SQLite
make runserver              # demarre Django sur :8000
# dans un autre shell :
make web                    # demarre Vite sur :5173
```

## Tests

```
make test
```

## Architecture

```
obfuscmapper/
  backend/         # Django + DRF + Celery + apps
  frontend/        # React + Vite
  parser/          # JAR Maven (JavaParser)
  docker-compose.yml  # Postgres + Redis (prod / dev avance)
```
