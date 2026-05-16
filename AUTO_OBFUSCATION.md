# ObfuscMapper — Spécification complète du projet

## Résumé exécutif

**ObfuscMapper** est une application web de gestion du mapping d'obfuscation entre un projet
source lisible et un projet cible obfusqué. Elle automatise un processus aujourd'hui entièrement
manuel : analyser un projet source, extraire ses variables, les chiffrer avec une clé
définie par l'utilisateur, générer la structure du projet obfusqué, et injecter les valeurs
chiffrées dans les fichiers cibles.

**Cas d'usage concret (projet pilote) :**
- Source : `Converter-unobf/` — application Spring Boot de conversion de fichiers (CSV, TXT, XML)
- Cible : `Converter-obf/` — même application avec noms de classes, champs et strings chiffrés
- Algorithme : XOR + Base64 avec clé définie par l'utilisateur

**Stack :** Django (Python) · PostgreSQL · React + TypeScript + Vite · Celery · Redis
**Contrainte RAM :** 4 Go maximum pour l'ensemble de la solution déployée
**Langue de projet supportée (v1) :** Java monolithe (architecture extensible, voir fin de doc)

---

## Algorithme de chiffrement

XOR octet par octet avec clé cyclique, résultat encodé en Base64.

**La clé est définie par l'utilisateur** pour chaque paire de projets. Elle est saisie lors
de la création de la paire source↔cible et stockée chiffrée en base de données (jamais en clair).
Chaque paire de projets a sa propre clé indépendante.

**Propriété fondamentale :** l'algorithme est son propre inverse.
`encrypt(encrypt(X, clé), clé) = X`. Chiffrer et déchiffrer sont la même opération —
seule la couche Base64 change (encode côté chiffrement, decode côté déchiffrement).
Cela permet une fonctionnalité d'audit et de récupération (voir F-AUDIT).

**Implémentation Python de référence (backend Django) :**

```python
import base64

def xor_encrypt(plaintext: str, key: str) -> str:
    data = plaintext.encode("utf-8")
    key_bytes = key.encode("utf-8")
    result = bytes(data[i] ^ key_bytes[i % len(key_bytes)] for i in range(len(data)))
    return base64.b64encode(result).decode("ascii")

def xor_decrypt(encoded: str, key: str) -> str:
    data = base64.b64decode(encoded)
    key_bytes = key.encode("utf-8")
    result = bytes(data[i] ^ key_bytes[i % len(key_bytes)] for i in range(len(data)))
    return result.decode("utf-8")

def verify(plaintext: str, encoded: str, key: str) -> bool:
    return xor_decrypt(encoded, key) == plaintext
```

---

## Sécurité de la base de données

### Chiffrement des champs sensibles

Le contenu sensible stocké en PostgreSQL (clés de chiffrement des projets, valeurs de variables
en clair) est chiffré au niveau applicatif avant insertion. Django utilise Fernet (AES-128-CBC
avec HMAC-SHA256) pour chiffrer ces champs.

La clé Fernet est dérivée d'un **mot de passe maître** saisi par l'utilisateur administrateur.
Ce mot de passe maître n'est jamais stocké — seule la clé dérivée (en mémoire pendant la
session) est utilisée.

### Rotation mensuelle du mot de passe maître

L'utilisateur entre l'**ancien** mot de passe maître et le **nouveau** chaque mois.
Django déclenche alors un job Celery qui :
1. Déchiffre tous les champs sensibles avec l'ancienne clé dérivée
2. Les rechiffre avec la nouvelle clé dérivée
3. Confirme la rotation (rapport de succès/erreurs)

```
Interface de rotation :
  [Ancien mot de passe maître] ____________________
  [Nouveau mot de passe maître] ___________________
  [Confirmer nouveau] _____________________________
  [Lancer la rotation]
```

### Champs chiffrés dans PostgreSQL

| Table | Champ chiffré |
|---|---|
| `project_pair` | `encryption_key` |
| `variable` | `evaluated_value` |
| `mapping` | `encrypted_value` |

---

## Architecture générale

```
┌─────────────────────────────────────────────────────────┐
│                FRONTEND (React + TSX + Vite)             │
│  Dashboard · Projects · FileTree · Variables             │
│  Mapping · Apply · Generate · Audit                      │
└───────────────────────┬─────────────────────────────────┘
                        │ REST API (JSON)
┌───────────────────────▼─────────────────────────────────┐
│                  BACKEND (Django + DRF)                  │
│  Projects · Files · Variables · Mappings · Jobs          │
│  XorBase64Service · FernetEncryptionService              │
│  Celery task queue                                       │
└───────────┬───────────────────────────┬─────────────────┘
            │                           │
    ┌───────▼──────┐           ┌────────▼────────┐
    │  PostgreSQL  │           │  Redis (Celery  │
    │  (données    │           │  broker+cache)  │
    │   chiffrées) │           └─────────────────┘
    └──────────────┘
                        │
            ┌───────────▼──────────────┐
            │  Java Parser JAR         │
            │  Subprocess appelé par   │
            │  Celery pour analyser    │
            │  les projets Java 17+    │
            └──────────────────────────┘
```

### Pourquoi un JAR Java pour le parsing ?

Les bibliothèques Python de parsing Java (`javalang`) ne supportent pas Java 17+
(text blocks `"""`, records, sealed classes). Le projet pilote utilise Java 17 avec des
text blocks dans ses définitions de variables.

La solution : un JAR standalone basé sur **JavaParser** (lib Java mature, Java 17+ supporté)
qui analyse un projet Java et retourne un JSON structuré. Django l'appelle via `subprocess.run`
depuis une tâche Celery.

**Interface CLI du JAR :**
```bash
java -Xmx400m -jar obfusc-parser.jar \
  --project /chemin/vers/projet/source \
  --output /chemin/vers/result.json \
  --include-private true \
  --resolve-strings true
```

---

## Modèle de données (PostgreSQL)

### Table `project`

```
id              UUID PRIMARY KEY
name            VARCHAR(255) NOT NULL UNIQUE
description     TEXT
root_path       VARCHAR(1024)    -- chemin absolu du projet sur le serveur
language        VARCHAR(50) DEFAULT 'java'
project_type    VARCHAR(50) DEFAULT 'monolith'
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

> `root_path` est indispensable pour deux opérations :
> - Projet source → lecture par le JAR Java pour l'analyse automatique
> - Projet cible → lecture/écriture par Django pour la génération et l'injection

### Table `project_pair`

```
id              UUID PRIMARY KEY
name            VARCHAR(255)
source_project  FK -> project
target_project  FK -> project (NULL si pas encore généré)
encryption_key  TEXT (chiffré Fernet)  -- clé XOR saisie par l'utilisateur
algorithm       VARCHAR(50) DEFAULT 'xor_base64'
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### Table `folder`

Arborescence de dossiers. Autoréférentielle pour les sous-dossiers.

```
id              UUID PRIMARY KEY
project         FK -> project
parent          FK -> folder (NULL = racine)
name            VARCHAR(255)       -- nom réel sur disque
obf_name        VARCHAR(255)       -- nom obfusqué proposé (peut être identique si is_default)
path            VARCHAR(1024)      -- chemin relatif depuis la racine du projet
is_default      BOOLEAN DEFAULT FALSE  -- si TRUE : nom inchangé dans le projet cible
validated       BOOLEAN DEFAULT FALSE
created_at      TIMESTAMP
```

### Table `source_file`

```
id              UUID PRIMARY KEY
project         FK -> project
folder          FK -> folder
name            VARCHAR(255)       -- nom réel
obf_name        VARCHAR(255)       -- nom obfusqué proposé
relative_path   VARCHAR(1024)
language        VARCHAR(50) DEFAULT 'java'
is_default      BOOLEAN DEFAULT FALSE  -- si TRUE : nom inchangé dans le projet cible
validated       BOOLEAN DEFAULT FALSE
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### Table `variable`

```
id              UUID PRIMARY KEY
source_file     FK -> source_file
name            VARCHAR(255)       -- nom original dans le code source
obf_name        VARCHAR(255)       -- nom obfusqué proposé
var_type        VARCHAR(100)       -- 'String', 'int', 'List<String>', etc.
raw_value       TEXT               -- valeur brute telle que dans le code (avant résolution)
evaluated_value TEXT (chiffré Fernet) -- valeur résolue (après concat, text blocks, etc.)
is_sensitive    BOOLEAN DEFAULT TRUE   -- si FALSE : pas de chiffrement
confidence      VARCHAR(20)        -- 'high' | 'medium' | 'low' (issu de l'analyse auto)
validated       BOOLEAN DEFAULT FALSE
notes           TEXT
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### Table `mapping`

Lien entre une variable source et son emplacement dans un fichier cible.
Une variable source peut être mappée vers plusieurs fichiers cibles.

```
id              UUID PRIMARY KEY
project_pair    FK -> project_pair
source_variable FK -> variable
target_file     FK -> source_file   -- fichier dans le projet cible
target_var_name VARCHAR(255)        -- nom du champ dans le fichier cible
encrypted_value TEXT (chiffré Fernet) -- valeur chiffrée XOR+Base64 calculée
injection_pattern TEXT              -- template de la ligne à remplacer dans le fichier cible
                                    -- ex: 'this.{var} = R04oo.d0x116_("{value}");'
validated       BOOLEAN DEFAULT FALSE
applied_at      TIMESTAMP (NULL si pas encore appliqué)
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### Table `analysis_job`

```
id              UUID PRIMARY KEY
project         FK -> project
status          VARCHAR(50)   -- 'pending' | 'running' | 'done' | 'failed'
celery_task_id  VARCHAR(255)
result_json     JSONB         -- résultat brut du JAR Java
error_message   TEXT
started_at      TIMESTAMP
finished_at     TIMESTAMP
```

### Table `apply_job`

```
id              UUID PRIMARY KEY
project_pair    FK -> project_pair
mode            VARCHAR(20)   -- 'apply' | 'generate' | 'audit'
status          VARCHAR(50)
celery_task_id  VARCHAR(255)
files_processed INTEGER DEFAULT 0
files_total     INTEGER DEFAULT 0
errors          JSONB
started_at      TIMESTAMP
finished_at     TIMESTAMP
```

---

## Fonctionnalités (les deux phases en parallèle)

Les deux phases sont développées simultanément car elles partagent le même modèle de données.
La génération de structure obfusquée (historiquement "Phase 2") est prioritaire avec l'analyse
automatique car c'est un besoin immédiat.

---

### F-PROJ — Gestion des projets

- CRUD sur les projets (source et cible séparément)
- Champ `root_path` : chemin absolu du projet sur le serveur, saisi manuellement
- Vérification que le chemin existe et est accessible au démarrage d'un job
- Association source ↔ cible via `project_pair`
- Saisie de la clé de chiffrement XOR lors de la création de la paire (stockée chiffrée Fernet)
- La clé n'est jamais renvoyée en clair dans l'API après enregistrement (affichage masqué `****`)

---

### F-TREE — Arborescence manuelle

- Interface type explorateur de fichiers pour naviguer/éditer la structure d'un projet
- Création manuelle de dossiers, sous-dossiers et fichiers
- Marquage `is_default` : fichiers/dossiers dont le nom ne change jamais dans le projet cible
- Exemples de fichiers/dossiers `is_default` pour Java :
  `pom.xml`, `build.gradle`, `application.properties`, `application.yml`,
  `resources/`, `static/`, `templates/`, `META-INF/`
- Liste `is_default` configurable par l'utilisateur dans les settings de la paire

---

### F-VAR — Gestion des variables

- Dans chaque fichier, déclarer des variables : nom, type, valeur brute, sensible ou non
- Le champ `evaluated_value` = copie de `raw_value` en saisie manuelle
- Option de marquer une variable `is_sensitive = false` (valeur non chiffrée, nom non obfusqué)
- Prévisualisation de la valeur chiffrée calculée (appel backend, résultat affiché une fois,
  non persisté en clair)
- Vérification automatique affichée : `decrypt(encrypt(value, key)) == value` → OK / KO

---

### F-ANALYZE — Analyse automatique du projet source (Java)

1. L'utilisateur clique "Analyser" sur un projet source
2. Un job Celery `analysis_job` est créé et lancé
3. Django appelle le JAR Java via subprocess :
   ```bash
   java -Xmx400m -jar obfusc-parser.jar --project {root_path} --output {tmp_file}
   ```
4. Le JAR retourne un JSON avec dossiers, fichiers, variables et leurs valeurs résolues
5. Django crée des propositions non validées (`validated=False`) pour chaque élément détecté
6. Le frontend affiche le résultat dans l'interface de validation

**Format du JSON retourné par le JAR :**
```json
{
  "project_root": "/chemin/vers/projet",
  "folders": [
    { "path": "src/main/java/ma/ac2i", "name": "ac2i", "parent": "src/main/java/ma" }
  ],
  "files": [
    {
      "path": "src/main/java/ma/ac2i/config/FileTemplate.java",
      "name": "FileTemplate.java",
      "class_name": "FileTemplate",
      "package": "ma.ac2i.config"
    }
  ],
  "variables": [
    {
      "file_path": "src/main/java/ma/ac2i/config/FileTemplate.java",
      "name": "xml_to_txt",
      "type": "String",
      "access": "private",
      "raw_value": "\"<?xml ...\" + \"<xsl:...\"",
      "evaluated_value": "<?xml version='1.0'...chaîne complète résolue...",
      "location": "constructor",
      "line_start": 28,
      "line_end": 68,
      "confidence": "high"
    }
  ]
}
```

**Niveaux de confiance `confidence` :**
- `high` : string littérale simple ou text block `"""..."""` — valeur évaluée avec certitude
- `medium` : concaténation de strings littérales `"a" + "b"` — valeur évaluée avec confiance
- `low` : dépend d'une variable externe, d'un appel de méthode ou d'un fichier — non évaluable

---

### F-VALID — Validation des propositions de l'analyse

Interface de validation après analyse automatique :

- Arbre de navigation affichant tous les éléments détectés
- Pour chaque variable proposée, l'utilisateur peut :
  - **Valider** : accepter telle quelle
  - **Modifier** : corriger le nom, la valeur évaluée, le type
  - **Supprimer** : exclure de l'obfuscation
  - **Marquer non-sensible** : la variable ne sera pas chiffrée
- Variables à `confidence: low` : avertissement visuel avec la raison, saisie manuelle requise
- Bouton "Valider tout (high)" : valide en masse les éléments à haute confiance
- Après validation, enregistrement en base (`validated=True`)

---

### F-MAP — Mapping source → cible

Pour chaque variable d'un fichier source, définir :
- Le fichier cible (dans le projet cible)
- Le nom de la variable dans le fichier cible (`target_var_name`)
- Le pattern d'injection : template de la ligne à remplacer dans le fichier cible

**Exemples de patterns d'injection Java :**
```
this.{target_var_name} = R04oo.d0x116_("{value}");
private static final String {target_var_name} = "{value}";
```
Le pattern est personnalisable par mapping (pour couvrir différents styles de code).

**Règle :** une variable source peut être mappée vers plusieurs fichiers cibles
(relation 1→N entre `variable` et `mapping`).

Interface : vue côte-à-côte (source à gauche, cible à droite), avec sélection du fichier
cible et saisie du nom de variable cible.

---

### F-GENERATE — Génération de la structure du projet cible

Fonctionnalité clé : créer automatiquement la structure obfusquée du projet cible sur disque
à partir de la structure source enregistrée.

**Deux modes :**

**Mode A — Générer la structure uniquement (dossiers + fichiers vides)**
- Crée physiquement les dossiers et fichiers sur disque dans `target_project.root_path`
- Les noms sont obfusqués selon `folder.obf_name` et `source_file.obf_name`
- Les fichiers marqués `is_default` gardent leur nom d'origine
- Les fichiers `is_default` sont copiés tels quels depuis le projet source

**Mode B — Générer + proposer les noms obfusqués automatiquement**
- L'app propose des noms obfusqués pour chaque dossier et fichier non `is_default`
- Convention de nommage : transformation du nom original en notation obfusquée
  (ex: `FileTemplate` → `OoOo`, `config` → `c0o`, `service` → `s0o`)
- L'utilisateur valide ou modifie chaque nom proposé avant génération physique
- Les `obf_name` validés sont sauvegardés en base

**Job Celery `generate` :**
1. Lit la structure validée en base (`folder`, `source_file` avec `obf_name`)
2. Crée les dossiers dans `target_project.root_path`
3. Crée les fichiers (vides ou copiés selon `is_default`)
4. Rapport : liste des fichiers/dossiers créés, erreurs éventuelles

---

### F-APPLY — Application physique du mapping

Injecte les valeurs chiffrées dans les fichiers du projet cible.

**Job Celery `apply` :**
1. Pour chaque mapping validé de la paire :
   a. Lit le fichier cible sur disque (`target_project.root_path + target_file.relative_path`)
   b. Récupère la clé XOR de la paire (déchiffrée Fernet en mémoire, jamais loggée)
   c. Calcule `encrypted_value = xor_encrypt(evaluated_value, key)`
   d. Cherche dans le fichier la ligne correspondant au `injection_pattern` (regex)
   e. Remplace la valeur entre les délimiteurs du pattern par `encrypted_value`
   f. Réécrit le fichier
2. Rapport final : succès / skip (pattern non trouvé) / erreur par mapping
3. Met à jour `mapping.applied_at` et `mapping.encrypted_value` en base

**Suivi en temps réel dans le frontend :** polling toutes les 2 secondes sur `apply_job.status`
et `apply_job.files_processed / files_total`.

---

### F-AUDIT — Déchiffrement / récupération

Grâce à la symétrie XOR (`decrypt = encrypt`), l'app peut relire les valeurs chiffrées
dans le projet cible et les déchiffrer pour vérification ou récupération.

**Utilités :**
- Vérifier que les valeurs injectées correspondent bien aux valeurs attendues
- Récupérer le contenu lisible d'un projet obfusqué si le source est perdu

**Job Celery `audit` :**
1. Lit chaque fichier cible concerné par un mapping
2. Extrait la valeur chiffrée actuelle (regex sur le pattern d'injection)
3. Déchiffre avec la clé de la paire
4. Compare avec `variable.evaluated_value` en base
5. Rapport : variable / valeur déchiffrée / valeur attendue / état (OK / DIFF / NON TROUVÉ)

---

## Interface utilisateur (React + TypeScript + Vite)

### Pages

| Page | Description |
|---|---|
| Dashboard | Liste des paires de projets, statuts, dernière application |
| Projets | CRUD projets, saisie du `root_path`, bouton Analyser |
| Arborescence | File tree interactif, CRUD dossiers/fichiers/variables |
| Mapping | Vue côte-à-côte source↔cible, création des mappings |
| Générer | Configuration et lancement de la génération du projet cible |
| Appliquer | Lancement et suivi en temps réel de l'injection |
| Audit | Déchiffrement et rapport de cohérence |
| Rotation clé | Interface de rotation du mot de passe maître |

### Indicateurs visuels

- `validated = false` → badge orange "En attente"
- `confidence: low` → icône ⚠ avec tooltip sur la raison
- `is_sensitive = false` → icône ○ (non chiffré)
- `is_default = true` → icône 🔒 (nom inchangé)
- Job en cours → spinner + barre de progression
- Job en erreur → badge rouge + lien vers le rapport d'erreurs

---

## Backend Django — Structure des apps

```
obfuscmapper/
├── apps/
│   ├── projects/     # Project, ProjectPair, Folder, SourceFile — CRUD + validation
│   ├── variables/    # Variable — CRUD + prévisualisation chiffrement
│   ├── mappings/     # Mapping — CRUD + logique d'injection
│   ├── jobs/         # AnalysisJob, ApplyJob — Celery tasks
│   └── crypto/       # XorBase64Service, FernetService, rotation mot de passe
├── settings/
│   ├── base.py
│   ├── development.py
│   └── production.py
├── celery.py
└── urls.py
```

### Celery tasks

```python
# jobs/tasks.py

@shared_task(bind=True, max_retries=3, time_limit=300)
def run_analysis(self, analysis_job_id: str):
    """Lance le JAR Java sur le projet source, parse le JSON, crée les propositions en base."""

@shared_task(bind=True, time_limit=600)
def apply_mappings(self, apply_job_id: str):
    """Applique physiquement les mappings validés dans les fichiers du projet cible."""

@shared_task(bind=True, time_limit=600)
def generate_structure(self, apply_job_id: str):
    """Crée la structure de dossiers et fichiers du projet cible sur disque."""

@shared_task(bind=True, time_limit=300)
def audit_mappings(self, apply_job_id: str):
    """Déchiffre les valeurs du projet cible et compare avec la base."""

@shared_task(bind=True, time_limit=600)
def rotate_master_password(self, old_password: str, new_password: str):
    """Rechiffre tous les champs sensibles avec le nouveau mot de passe maître."""
```

---

## Contrainte RAM (4 Go total)

| Composant | RAM cible |
|---|---|
| Django + Gunicorn (4 workers) | ~512 Mo |
| Celery (2 workers, concurrency=2) | ~512 Mo |
| Redis | ~128 Mo |
| PostgreSQL | ~256 Mo |
| Java Parser JAR (pic, tâche courte) | ~400 Mo |
| Frontend Nginx (statique) | ~64 Mo |
| OS + marge | ~700 Mo |
| **Total estimé** | ~2,6 Go |

```python
# Configuration Celery pour respecter la contrainte RAM
CELERY_WORKER_CONCURRENCY = 2
CELERY_WORKER_MAX_MEMORY_PER_CHILD = 256_000  # redémarre le worker si > 256 Mo
```

```bash
# Appel du JAR avec limite mémoire
java -Xmx400m -jar obfusc-parser.jar ...
```

---

## Fenêtre d'extensibilité

### Autres langages

La table `variable` hérite un `language` de `source_file`. Pour ajouter Python, C#, etc. :
1. Écrire un parser pour ce langage (ex: module `ast` Python, Roslyn pour C#) qui retourne
   le même format JSON que le JAR Java
2. Créer un `ParserService` dans `jobs/parsers/` implémentant l'interface commune
3. Ajouter la valeur dans les choix de `project.language`
4. Le reste (mapping, chiffrement, injection) est identique

### Java Microservice

Si le projet source passe de monolithe à microservices :
1. Créer un `project` par service
2. Les associer à des `project_pair` individuels
3. Le JAR analyse chaque service indépendamment
4. Un `project_group` (à ajouter en v2) peut regrouper plusieurs paires pour une vue globale

### Autres algorithmes

La table `project_pair` a un champ `algorithm` (`xor_base64` par défaut). Pour ajouter AES :
1. Implémenter `AesService` dans `crypto/`
2. Enregistrer dans un registre `ALGORITHM_REGISTRY = {'xor_base64': XorBase64Service, ...}`
3. Le service est résolu dynamiquement depuis `project_pair.algorithm`

---

## Chemins disque — points importants pour l'implémentation

Les chemins `root_path` des projets sont la donnée pivot de toute la solution.

- **Projet source** `root_path` → utilisé par le JAR Java pour l'analyse (`F-ANALYZE`)
- **Projet cible** `root_path` → utilisé par Django pour la génération (`F-GENERATE`) et
  l'injection (`F-APPLY`) et la lecture d'audit (`F-AUDIT`)
- Les chemins sont absolus sur le serveur qui héberge ObfuscMapper
- Validation au démarrage d'un job : vérifier que le chemin existe et que le process a
  les droits de lecture (source) et lecture/écriture (cible)
- Ne jamais exposer les chemins complets dans les logs publics ou les réponses API front

---

## Fichiers à lire avant de commencer l'implémentation

Situés dans `C:\Users\AC2I\Desktop\Converter\` :

1. `Obfuscateur-Converter/SecureTransformService.java`
   → Implémentation de référence de l'algorithme XOR+Base64 (à reproduire en Python)

2. `Converter-unobf/src/main/java/ma/ac2i/converter/converter/config/FileTemplate.java`
   → Exemple concret : 6 champs String avec mix de string concat et text blocks Java 17
   → Représente ce que l'analyse automatique doit détecter et évaluer

3. `Converter-obf/src/main/java/ma/ac2i/y1r0/z1r0/c0o/OoOo.java`
   → Exemple concret : le fichier cible avec les valeurs chiffrées déjà injectées
   → Montre le format du pattern d'injection utilisé dans ce projet

---

## Critères de validation (test concret sur le projet pilote)

1. Créer la paire "Converter-unobf → Converter-obf" avec la clé `A0x43x32x49$cwBJAQ==`
   et les chemins disques des deux projets
2. Lancer l'analyse automatique sur `Converter-unobf` → vérifier que les 6 variables de
   `FileTemplate.java` sont détectées avec `confidence: high` et leurs valeurs correctement
   résolues (text blocks + string concat)
3. Valider les 6 variables
4. Créer les 6 mappings vers `OoOo.java` avec le pattern
   `this.{target_var_name} = R04oo.d0x116_("{value}");`
5. Lancer "Appliquer" → vérifier que `OoOo.java` est mis à jour avec les bonnes valeurs
6. `mvn compile` dans `Converter-obf` doit passer sans erreur
7. Le projet Spring Boot démarre et les conversions fonctionnent
8. Lancer "Audit" → toutes les variables doivent afficher OK
