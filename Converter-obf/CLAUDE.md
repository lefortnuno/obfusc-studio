# Converter-obf — Analyse du code obfusqué

## Vue d'ensemble

Application Spring Boot 3.2.3 (Java 17) de conversion de fichiers.
Formats supportés : CSV, TXT, XML, Excel (xlsx/xls).
SSL activé sur le port **8086**.
Base de données H2 embarquée : `Converter.mv.db`.

---

## Architecture des packages (noms obfusqués → rôle réel)

| Package / Classe obfusquée | Rôle réel |
|---|---|
| `Cx0A` | Main Spring Boot |
| `c0o/Oo` | AuthenticationFailureHandler |
| `c0o/OoOo` | **Service de génération XSLT** (cœur de la conversion) |
| `c0o/OoOoOo` | SecurityFilterChain (Spring Security) |
| `c0o/OoOoOoOo` | WebMvcConfigurer + Intercepteur |
| `e0o/*` | Entités JPA : AppUser, AppRole, Structure, ComplexStructure, SubStructure, StructureDetail, SubStructureDetail, Licence |
| `m0o/OoOoOoOoOo` | Intercepteur de validation de licence (sur chaque requête) |
| `m0o/R04o` | Exécution de processus externe (`d0x116_.exe`) |
| `r0o/*` | Repositories Spring Data JPA |
| `s0o/R04oo` | Utilitaire de décodage de chaînes (Base64 + XOR) |
| `s0o/R03o` | **Service de conversion** — appel Saxon via ProcessBuilder |
| `s0o/RO6o0o` | **Service Excel→XML/CSV** (Apache POI) |
| `s0o/O3r0oo` | Service de déchiffrement de licence (AES-256/CBC/PKCS5) |
| `s0o/OR5oo` | Service de suppression de SubStructure |
| `w0o/S03o` | Controller principal (accueil + lancement de conversion) |
| `w0o/S04oo` | Controller UI Structure (CRUD simple) |
| `w0o/OS5oo` | REST API Structure |
| `w0o/O2s0o` | REST API ComplexStructure |
| `w0o/Ooooo` | Controller UI ComplexStructure |
| `w0o/SO6o0o` | Controller gestion utilisateurs |
| `w0o/O3s0oo` | Controller configuration licence |

---

## Modèle de données

```
Structure  (1:N)  StructureDetail
  └─ StrName (unique), libelle, StrType, expression (CLOB avec @champs)
  └─ StructureDetail : name, type, position, longeur, decimal, link

ComplexStructure  (M:N via table)  SubStructure
  └─ name (unique), libelle, expression (CLOB avec @sous-structures)
  └─ SubStructure  (1:N)  SubStructureDetail

AppUser  (M:N)  AppRole
Licence  — clé chiffrée AES, contient HOSTNAME, EXPDATE, MOD_BASE
```

---

## Flux de conversion (fichiers texte / XML / CSV)

1. L'utilisateur uploade un fichier + choisit structure + format cible
2. `OoOo.m0x461$()` génère un fichier XSLT à partir de `Structure.expression` en remplaçant les tokens `@NomChamp`
3. `R03o.t0x110$()` appelle Saxon (`net.sf.saxon.Transform`) via `ProcessBuilder`
4. Le fichier converti est écrit dans `/download/`
5. L'utilisateur télécharge le résultat

## Flux de conversion Excel

1. `RO6o0o` lit le fichier `.xlsx` avec Apache POI
2. Détecte les tableaux dans la feuille (toute ligne non vide = début de tableau)
3. Pour chaque ligne de données, copie `SubStructure.expression` et remplace `@NomChamp` par la valeur de la cellule
4. Assemble le XML final en remplaçant les tokens `@NomSousStructure` dans `ComplexStructure.expression`

---

## Correctifs appliqués (session 2026-04-10) — livraison client

### 1. Collision de noms de champs (substring matching) — CORRIGÉ

**Fichiers :** `c0o/OoOo.java` (lignes 118, 124) · `s0o/RO6o0o.java` (lignes 91, 101, 104, 106, 130, 132, 134, 149)

`String.replace("@User", val)` remplaçait aussi `@UserOverriden` et `@UserSuffix`.

**Correction appliquée :**
```java
Fields = Fields.replaceAll("@" + Pattern.quote(field.getName()) + "(?![\\w])", Matcher.quoteReplacement(tmp));
```
Imports ajoutés : `java.util.regex.Pattern`, `java.util.regex.Matcher`.
Même correction portée dans `Converter-unobf` : `config/FileTemplate.java` et `service/TableDetectionService.java`.

---

### 2. Perte de champs à la mise à jour d'une structure — CORRIGÉ

**Fichier :** `w0o/OS5oo.java` — méthode PUT update

Sans `@Transactional`, si une exception survenait après le `deleteAll()` des anciens champs, la structure se retrouvait sans champs.

**Correction :** annotation `@Transactional` ajoutée sur la méthode update.
Même correction portée dans `Converter-unobf` : `web/StructureRestController.java`.

---

### 3. Temps de réponse lent + erreur d'auth qui disparaît — CORRIGÉ

**Fichier :** `c0o/Oo.java` (AuthenticationFailureHandler)

`forward` gardait la requête HTTP ouverte jusqu'à la fin de tous les traitements Spring Security.

**Correction :** remplacement par `sendRedirect` + stockage du message en session.
Même correction portée dans `Converter-unobf` : `config/CustomAuthenticationFailureHandler.java` et `web/HomeController.java`.

---

### 4. Messages d'erreur SQL bruts exposés à l'utilisateur — CORRIGÉ (unobf uniquement)

La version obfusquée gérait déjà les messages. La version non obfusquée renvoyait le stack trace SQL brut.

**Correction :** création de `service/ErrorMessageHelper.java` dans `Converter-unobf`, utilisé dans `UserController`, `StructureRestController` et `ComplexStructureRestController`.

---

### 5. Alerte de validation qui disparaît immédiatement — CORRIGÉ

**Fichier :** `static/assets/js/custom.js` (les deux projets)

Le callback `success` de `checkName` / `checkSubName` / `checkMainName` appelait `setAlert("", "")`, effaçant l'alerte d'erreur affichée juste avant par la validation synchrone.

**Correction :** suppression des appels `setAlert("", "")` dans les trois callbacks `success`.

---

## Chiffrement / Obfuscation

### Décodage de chaînes (`s0o/R04oo`)
- Base64 decode → XOR avec la clé fixe `A0x43x32x49$cwBJAQ==`
- Utilisé pour cacher les rôles (`ADMIN`, `user`), les URLs, les noms de paramètres

### Licence (`s0o/O3r0oo`)
- AES-256/CBC/PKCS5Padding
- Payload JSON : `{"HOSTNAME":"...", "EXPDATE":"yyyy-MM-dd", "MOD_BASE":"True"}`
- Vérifié sur chaque requête par l'intercepteur `OoOoOoOoOo`

---

## Configuration (`application.properties`)

| Paramètre | Valeur |
|---|---|
| Port | 8086 |
| DB | H2 fichier `./Converter` |
| SSL | PKCS12, mot de passe `ac2i123` |
| Upload max | 800 MB |
| Saxon JARs | `sdIl8WQlYKQFsXWg==.jar`, `xkYRW1cEWQ=.jar` |

---

## CLI (modes de lancement)

```bash
java -jar converter.jar -l <clé_licence>          # Activer la licence
java -jar converter.jar -c <xslt> <in> <out> [d]  # Convertir un fichier
java -jar converter.jar -h                         # Aide
java -jar converter.jar -d                         # Démarrer le serveur web
```
