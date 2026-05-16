# Contexte de la session — Naissance du projet ObfuscMapper

## Projet pilote : Converter-Easy

Application Spring Boot (Java 17) de conversion de fichiers (CSV, TXT, XML).
Deux versions coexistent dans `C:\Users\AC2I\Desktop\Converter\` :

- `Converter-unobf/` — version lisible, code source de référence (ne pas livrer)
- `Converter-obf/` — version obfusquée livrée au client (classes, champs, strings chiffrés)
- `Obfuscateur-Converter/` — outils de chiffrement standalone

### Ce qu'on a fait manuellement jusqu'ici

1. Mettre les strings à chiffrer dans `SecureTransformService.java` (tableau `originals`)
2. `javac SecureTransformService.java && java SecureTransformService`
3. Copier chaque string chiffrée en sortie dans le bon champ de `OoOo.java`
4. Répéter pour les 6 templates XSLT

Bug corrigé en session : `"""<?xml...` (text block Java mal formé) au lieu de `"<?xml...`
aux lignes 541 et 639 de `SecureTransformService.java`.

### Algorithme de chiffrement

XOR octet par octet avec clé cyclique + Base64. Fichiers de référence :
- `SecureTransformService.java` — implémentation Java (build-time)
- `R04oo.c` — implémentation C (runtime dans le livrable client, gardée pour la résistance
  au reverse engineering car le bytecode Java se décompile facilement)

Propriété clé : l'algorithme est son propre inverse — `encrypt(encrypt(X)) = X`.
Cela permet un mode audit/récupération.

La clé n'est PAS fixe dans la nouvelle solution : chaque paire de projets a sa propre clé,
saisie par l'utilisateur et stockée chiffrée en base.

### Mapping concret FileTemplate.java → OoOo.java

| Champ source (`FileTemplate.java`) | Champ cible (`OoOo.java`) | Conversion |
|---|---|---|
| `xml_to_txt` | `x0x5F111x5Fx116$` | xmltotxt |
| `xml_to_csv` | `x0x5F111x5Fx118`  | xmltocsv |
| `txt_to_xml` | `t0x5F111x5Fx108`  | txttoxml |
| `txt_to_csv` | `t0x5F111x5Fx118`  | txttocsv |
| `csv_to_xml` | `c0x5F111x5Fx108`  | csvtoxml |
| `csv_to_txt` | `c0x5F111x5Fx116`  | csvtotxt |

Pattern d'injection dans `OoOo.java` :
```java
this.{champ} = R04oo.d0x116_("{valeur_base64}");
```

---

## Décisions de conception de la nouvelle solution

### Stack technique validé

| Composant | Technologie |
|---|---|
| Backend | Django (Python) + Django REST Framework |
| Base de données | PostgreSQL |
| Frontend | React + TypeScript + Vite |
| Queue de tâches | Celery + Redis |
| Parser Java | JAR standalone (JavaParser lib, Java 17+) appelé via subprocess |
| Chiffrement DB | Fernet (AES-128-CBC + HMAC) au niveau applicatif Django |
| Contrainte RAM | 4 Go maximum pour l'ensemble de la solution |

### Pourquoi un JAR Java pour le parsing ?

Les libs Python (`javalang`) ne supportent pas Java 17+ (text blocks, records...).
JavaParser (Java) supporte Java 17+ et peut évaluer les concaténations de strings et text blocks.
Django appelle le JAR via `subprocess.run` depuis une tâche Celery.

### Sécurité de la base de données

- Champs sensibles (clés XOR, valeurs en clair) chiffrés avec Fernet avant insertion
- Fernet dérivé d'un **mot de passe maître** saisi par l'admin (jamais stocké)
- **Rotation mensuelle** : l'utilisateur saisit l'ancien mdp + le nouveau →
  Celery rechiffre tous les champs sensibles
- La clé XOR d'un projet n'est jamais renvoyée en clair dans l'API

### Deux phases développées en parallèle

**Décision clé :** les deux phases sont développées simultanément car elles partagent
le même modèle de données et que la génération de structure obfusquée est un besoin immédiat
(le développeur a galéré à créer manuellement la copie obfusquée du projet initial).

**Fonctionnalités principales :**
- F-PROJ : gestion des projets avec `root_path` (chemin disque indispensable)
- F-TREE : arborescence manuelle (dossiers, fichiers, marquage `is_default`)
- F-VAR : déclaration manuelle des variables
- F-ANALYZE : analyse automatique via JAR Java (avec niveaux de confiance high/medium/low)
- F-VALID : validation humaine des propositions de l'analyse
- F-MAP : mapping source → cible (1 variable → N fichiers cibles possible)
- F-GENERATE : génération physique de la structure du projet cible sur disque
- F-APPLY : injection des valeurs chiffrées dans les fichiers cibles
- F-AUDIT : déchiffrement des valeurs dans le projet cible pour vérification/récupération

### Le binaire C (`d0x116_.exe`) reste dans le livrable client

ObfuscMapper ne modifie pas le binaire C. Il produit uniquement des valeurs chiffrées.
Le comment le projet cible les utilise à runtime est sa propre affaire.

### Fenêtre d'extensibilité prévue

- Autres langages : interface commune de parser, implémentation dédiée par langage
- Java microservice : plusieurs `project` pour un même système, regroupés en `project_group` (v2)
- Autres algorithmes : registre `ALGORITHM_REGISTRY` résolu dynamiquement depuis `project_pair.algorithm`

### Critères de validation Phase 1 (test concret)

1. Créer la paire avec clé `A0x43x32x49$cwBJAQ==` et les chemins des deux projets
2. Analyser `Converter-unobf` → 6 variables de `FileTemplate.java` détectées confidence:high
3. Valider + mapper vers `OoOo.java` avec le bon pattern d'injection
4. Appliquer → `OoOo.java` mis à jour
5. `mvn compile` dans `Converter-obf` passe sans erreur
6. Application Spring Boot démarre et les conversions fonctionnent
7. Audit → toutes les variables affichent OK

---

## Fichiers de référence à lire impérativement

```
C:\Users\AC2I\Desktop\Converter\
├── Obfuscateur-Converter\
│   ├── SecureTransformService.java   ← algorithme XOR+Base64 de référence
│   ├── R04oo.c                       ← idem en C (runtime client)
│   ├── AUTO_OBFUSCATION.md           ← spécification complète du projet à développer
│   └── CONV_CONTEXT.md               ← ce fichier
├── Converter-unobf\src\main\java\ma\ac2i\converter\converter\config\
│   └── FileTemplate.java             ← exemple source : 6 strings (concat + text blocks)
└── Converter-obf\src\main\java\ma\ac2i\y1r0\z1r0\c0o\
    └── OoOo.java                     ← exemple cible : 6 valeurs chiffrées injectées
```
