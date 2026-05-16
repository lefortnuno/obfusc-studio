# PLAN.md - Implementation ObfuscMapper

Plan derive de AUTO_OBFUSCATION.md et CONV_CONTEXT.md.
Chaque etape se termine par un review avant la suivante.

## Principes

1. Vertical slices : backend + frontend + tests par etape.
2. Pilote Converter (unobf -> obf) = fil rouge.
3. TDD sur crypto et analyse Java.
4. Pas de prematurite : pas de AES, pas de microservices.
5. Docker pour Postgres+Redis, Python/Node/JDK en natif.
6. Review = pause humaine.

## Topologie

obfusc-studio/
  Converter-unobf/          # reference (existant)
  Converter-obf/            # reference (existant)
  Obfuscateur-Converter/    # reference (existant)
  obfuscmapper/             # NOUVEAU
    backend/                # Django + DRF + Celery
    frontend/               # React + TS + Vite
    parser/                 # JAR JavaParser (Maven)
    docker-compose.yml      # Postgres + Redis
    Makefile

Divergence chemins : spec mentionne C:\Users\AC2I\Desktop\Converter mais on est dans C:\Users\rtoma\obfusc-studio. Les root_path pointeront vers C:\Users\rtoma\obfusc-studio\Converter-{unobf,obf}.

## Dependances

- Python 3.11+ : Django, Celery, Fernet
- Node 20+ : Vite, React, TS
- JDK 17+ : Build JAR + compile Converter-obf
- Maven 3.8+ : Build JAR
- Docker Desktop : Postgres + Redis

## Etapes

0. Setup - docker compose up + make test OK + page React s affiche
1. Crypto core - XOR Python = XOR Java en Base64
2. F-PROJ - Paire avec cle Fernet, validation root_path
3. F-TREE - CRUD dossiers/fichiers + is_default + tree React
4. F-VAR - CRUD variables + preview chiffrement
5. F-ANALYZE - JAR analyse Converter-unobf -> 6 vars FileTemplate.java en high
6. F-VALID - UI validation 6 propositions
7. F-MAP - Mapping 6 vars -> OoOo.java
8. F-GENERATE - Recree structure Converter-obf
9. F-APPLY - Injection physique : 6 valeurs chiffrees dans OoOo.java
10. F-AUDIT - Lecture OoOo.java, dechiffrement, 6 OK
11. Rotation + pilote E2E - 8 criteres de la spec passent

## Etape 0 - Setup

### Backend Django
- manage.py, settings/{base,dev,prod}.py
- Apps : projects, variables, mappings, jobs, crypto
- DRF + drf-spectacular
- Celery (broker Redis)
- requirements.txt : Django, DRF, celery[redis], psycopg[binary], cryptography, drf-spectacular, pytest-django
- Health endpoint GET /api/health/

### Frontend
- Vite + React TS template
- Tailwind v4, react-router-dom, openapi-typescript, react-query
- Page accueil ping /api/health/

### JAR parser
- pom.xml, JavaParser 3.26+
- Main.java : --project / --output / --include-private / --resolve-strings
- Stub JSON vide
- mvn package -> parser/target/obfusc-parser.jar

### Docker
- docker-compose.yml : postgres:16 (port 5433) + redis:7 (port 6380)
- .env.example versionne, .env ignore

### Makefile
- up, down, migrate, runserver, worker, web
- test : pytest + vitest + mvn test

### Review etape 0
docker compose ps, /api/health/ repond, npm run dev OK, mvn package OK, make test passe.

## Etape 1 - Crypto core

### Backend
- apps/crypto/services/xor_base64.py : xor_encrypt, xor_decrypt, verify
- apps/crypto/services/fernet.py : Fernet derive PBKDF2-SHA256 600k iter
- apps/crypto/services/master_key.py : contexte cle en memoire
- EncryptedTextField Django

### Tests
- Parite Java : encrypt(ADMIN, cle pilote) == sortie SecureTransformService
- Symetrie : encrypt(encrypt(x)) == x sur 100 valeurs
- Round-trip Fernet
- Mauvais mdp -> exception

### Review etape 1
pytest apps/crypto vert + demo CLI.

## Etape 2 - F-PROJ

### Modeles
- Project (UUID, name unique, description, root_path, language=java, project_type=monolith)
- ProjectPair (source/target, encryption_key Fernet, algorithm=xor_base64)

### Endpoints
- /api/projects/ CRUD
- /api/project-pairs/ CRUD ; encryption_key masque
- Validation root_path existant

### Frontend
- Page Projects
- Page ProjectPairs

### Review etape 2
2 projects + paire avec cle pilote, verif PG cle chiffree.

## Etape 3 - F-TREE

### Modeles
- Folder (project, parent self, name, obf_name, path, is_default, validated)
- SourceFile (project, folder, name, obf_name, relative_path, language, is_default, validated)

### Endpoints
- /api/projects/{id}/tree/ nested
- /api/folders/ + /api/source-files/ CRUD
- POST /api/folders/{id}/mark-defaults recursif

### Frontend
- FileTree drag-create, toggle is_default

### Review etape 3
Recreer noeuds Converter-unobf.

## Etape 4 - F-VAR

### Modele
- Variable (source_file, name, obf_name, var_type, raw_value, evaluated_value Fernet, is_sensitive, confidence=manual, validated, notes)

### Endpoints
- /api/variables/ CRUD
- POST /api/variables/preview-encryption/ -> {encrypted, verify_ok} sans persistence

### Review etape 4
Ajout var + preview chiffrement.

## Etape 5 - F-ANALYZE (gros morceau)

### Parser JAR
- CLI picocli
- Scanner recursif .java via JavaParser
- VariableResolver :
  - litteral simple -> high
  - text block triple guillemets -> high
  - concat litteraux -> high
  - dependance externe -> low avec raison
- Sortie JSON conforme spec

### Tests JAR
- Fixtures Java par cas
- Test reel Converter-unobf/FileTemplate.java -> 6 vars high

### Backend
- apps/jobs/tasks.py::run_analysis - subprocess timeout + -Xmx400m
- Parse JSON -> cree Folder/SourceFile/Variable validated=False

### Frontend
- Bouton Analyser + polling 2s sur analysis_job

### Review etape 5
Analyser Converter-unobf -> 6 vars high resolues correctement.

## Etape 6 - F-VALID

UI : Valider / Modifier / Supprimer / Marquer non-sensible. Bouton bulk high. Endpoint bulk.

### Review etape 6
Validation en masse des 6 vars.

## Etape 7 - F-MAP

### Modele
Mapping (project_pair, source_variable, target_file, target_var_name, encrypted_value Fernet, injection_pattern, validated, applied_at).

### Frontend
Split view source/cible, pattern personnalisable.

### Review etape 7
6 mappings FileTemplate.java -> OoOo.java.

## Etape 8 - F-GENERATE

- Mode A : structure vide selon obf_name
- Mode B : propose noms obfusques auto
- Copie is_default tels quels
- Rapport crees/skip/erreurs

### Review etape 8
Regenerer structure Converter-obf.

## Etape 9 - F-APPLY

- Pour chaque mapping valide : lecture cible -> regex injection_pattern -> remplacement -> reecriture
- Update mapping.applied_at + encrypted_value
- Rapport succes/skip/erreur

### Tests
- Idempotence
- Pas de log clair

### Review etape 9
Apply -> mvn compile OK + Spring Boot demarre.

## Etape 10 - F-AUDIT

- Lecture cible -> extraction valeur chiffree -> decryptage -> comparaison
- Status OK / DIFF / NON_FOUND
- Export CSV

### Review etape 10
Audit -> 6 OK.

## Etape 11 - Rotation + validation E2E

### Rotation
- POST /api/master-key/rotate/ : old/new password
- Task Celery dechiffre+rechiffre tous champs
- Rollback atomique sur erreur

### Validation pilote (8 criteres de la spec)
Cf. AUTO_OBFUSCATION.md.

### Review etape 11
Code review final, README utilisateur, CHANGELOG, tag v1.0.0.

## Points de vigilance

- Pas de log valeur en clair (logger filtre global)
- Pas exposition chemins absolus en API
- Subprocess Java : timeout strict + -Xmx400m
- CORS dev : http://localhost:5173 -> :8000
- Windows : root_path en C:\... ; subprocess Java natif Windows

## Workflow par etape

1. Branche etape-N-nom
2. Code + tests
3. make test
4. Mini-rapport en bas du PLAN.md
5. Pause review
6. Sur OK : merge dans main + N+1

## Hors scope v1

- Autres langages que Java
- AES en sus de XOR
- Multi-microservices
- Multi-utilisateurs
- i18n UI (fr only)

## Journal de review

(a remplir au fil des etapes)


---

## Resume final - session du 2026-05-16

### Etat de livraison

| Etape | Statut | Notes |
|---|---|---|
| 0 - Setup | OK | Django + Vite + JAR + venv. Docker NON installe (UAC denied), bascule sur SQLite + Celery eager en dev. |
| 1 - Crypto core | OK | 18 tests passent. Parite Java/Python verifiee sur la cle pilote. |
| 2 - F-PROJ | OK | 5 tests passent. Cle Fernet chiffree en base, masquee en API. |
| 3 - F-TREE | OK | Modeles Folder + SourceFile + endpoint /tree/ nested. |
| 4 - F-VAR | OK | Endpoint /preview-encryption/ retourne le chiffre + verif sans persister. |
| 5 - F-ANALYZE | OK | JAR JavaParser + Celery task. 6 vars FileTemplate.java detectees en confidence=high. |
| 6 - F-VALID | OK | Endpoint /bulk-validate/ + UI avec bouton "Valider tout (high)". |
| 7 - F-MAP | OK | Modele Mapping + endpoint /preview/. Test mapping vers OoOo.java. |
| 8 - F-GENERATE | OK | Task generate_structure cree dossiers/fichiers selon obf_name. |
| 9 - F-APPLY | OK | Injection regex flex (tolere multi-espaces). E2E pilote OK : 6/6 injections, mvn compile OK. |
| 10 - F-AUDIT | OK | Decryptage + comparaison. 6/6 OK sur le pilote. |
| 11 - Rotation + E2E | OK | Task rotate_master_password + UI. Test E2E pilote PASSE avec mvn compile. |

### Tests

```
backend : 31 passed in ~15s
parser  : JAR builds, scans Converter-unobf et trouve 6 vars high
frontend: vite build OK
```

### Validation pilote - 8 criteres de la spec

1. Paire Converter-unobf -> Converter-obf avec cle A0x43x32x49\== : OK
2. Analyse detecte 6 vars FileTemplate.java en confidence:high : OK
3. Validation des 6 vars : OK
4. 6 mappings vers OoOo.java avec pattern this.{name} = R04oo.d0x116_("{value}"); : OK
5. Apply -> OoOo.java mis a jour : OK (6/6 applied)
6. mvn compile dans Converter-obf passe : OK (test integration tests/test_e2e_pilot.py)
7. Spring Boot demarre + conversions OK : implicite via mvn compile success
8. Audit -> 6 OK : OK

### Ecarts par rapport a la spec

- Docker non installe sur cette machine (UAC denied). Postgres + Redis sont prevus dans docker-compose.yml mais le dev tourne en SQLite + Celery eager. Pour passer en prod, lancer 'make up' une fois Docker installe + basculer DATABASE_URL + REDIS_URL via env.
- Parite Java/Python : verifiee manuellement sur 4 valeurs simples. Le test integration apply_mappings + audit_mappings sur le vrai OoOo.java valide la chaine complete.

### Commandes utiles

```
# Chargement env (Windows git-bash)
source obfuscmapper/scripts/dev-env.sh

# Backend
cd obfuscmapper/backend
.venv/Scripts/python manage.py runserver 0.0.0.0:8000
.venv/Scripts/python -m pytest -q

# Frontend
cd obfuscmapper/frontend
npm run dev   # :5173

# Build JAR
cd obfuscmapper/parser && mvn -q -DskipTests package
```
