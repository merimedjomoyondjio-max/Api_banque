# 🏦 API Bancaire - Documentation Complète

## Vue d'ensemble

L'**API Bancaire** est un système REST complète de gestion de comptes bancaires avec support complet des transactions (dépôts, retraits), pagination, et historique détaillé.

### Caractéristiques principales
- ✅ 6 endpoints RESTful bien documentés
- ✅ Validation complète des données (email unique, format téléphone, etc.)
- ✅ Gestion transactionnelle ACID
- ✅ Pagination des résultats
- ✅ Documentation OpenAPI/Swagger automatique
- ✅ 30 cas de test (tests unitaires + tests manuels)
- ✅ Gestion d'erreurs centralisée
- ✅ Support BDD H2 (dev) et PostgreSQL (prod)

---

## 📋 Table des Matières

1. [Installation](#installation)
2. [Démarrage Rapide](#démarrage-rapide)
3. [Les 6 Fonctionnalités](#fonctionnalités)
4. [Endpoints API](#endpoints-api)
5. [Codes de Réponse HTTP](#codes-http)
6. [Exemples d'Utilisation](#exemples)
7. [Tests](#tests)
8. [Architecture](#architecture)
9. [Configuration](#configuration)
10. [Troubleshooting](#troubleshooting)

---

## Installation {#installation}

### Prérequis
- **Java 21+**
- **Maven 3.8+**
- **Port 8080** disponible

### Étapes

#### 1. Cloner le projet
```bash
cd c:\Users\COMPUTER STORES\Desktop\projet perso\api
```

#### 2. Compiler
```bash
mvn clean install
```

#### 3. Démarrer l'API
```bash
mvn spring-boot:run
```

#### 4. Vérifier le démarrage
```
✓ "Started BankApiApplication in X seconds"
✓ "Tomcat started on port(s): 8080"
```

---

## Démarrage Rapide {#démarrage-rapide}

### 1. Accéder à Swagger UI
Ouvrir: **http://localhost:8080/swagger-ui.html**

### 2. Créer un compte
```bash
POST /accounts
Content-Type: application/json

{
  "name": "JAPHET DJOMO",
  "email": "JAPHETDJOM@GMAIL.COM",
  "phone": "+237657786440",
  "initialBalance": 1000.00
}
```

### 3. Effectuer un dépôt
```bash
POST /accounts/1/deposit
Content-Type: application/json

{
  "montant": 500.00
}
```

### 4. Voir le solde
```bash
GET /accounts/1
```

---

## Les 6 Fonctionnalités {#fonctionnalités}

### F1: Créer un Compte 🆕

**Endpoint**: `POST /accounts`  
**Authentification**: Aucune  
**Rate Limit**: Aucun

**Paramètres**:
| Champ | Type | Requis | Validation | Exemple |
|-------|------|--------|-----------|---------|
| `name` | String | ✓ | Non vide | "JAPHET DJOMO" |
| `email` | String | ✓ | Format email, unique | "JAPHETDJOM@GMAIL.COM" |
| `phone` | String | ✓ | 10-15 chiffres, +[pays][num] | "+237657786440" |
| `initialBalance` | Decimal | ✗ | ≥ 0.00 (défaut: 0.00) | 1000.00 |

**Réponse (201 Created)**:
```json
{
  "id": 1,
  "name": "JAPHET DJOMO",
  "email": "JAPHETDJOM@GMAIL.COM",
  "phone": "+237657786440",
  "balance": 1000.00,
  "createdAt": "2024-04-22T10:30:45.123Z",
  "updatedAt": "2024-04-22T10:30:45.123Z"
}
```

**Erreurs Possibles**:
- `400 Bad Request`: Email invalide, téléphone invalide, solde négatif
- `409 Conflict`: Email déjà utilisé

---

### F2: Lister les Comptes 📋

**Endpoint**: `GET /accounts?page=1&limit=10`  
**Authentification**: Aucune  
**Défaut**: Page 1, 10 résultats par page

**Paramètres**:
| Paramètre | Type | Défaut | Exemple |
|-----------|------|--------|---------|
| `page` | Integer | 1 | 2 |
| `limit` | Integer | 10 | 50 |

**Réponse (200 OK)**:
```json
{
  "results": [
    {
      "id": 1,
      "name": "JAPHET DJOMO",
      "email": "JAPHETDJOM@GMAIL.COM",
      "balance": 1000.00,
      "createdAt": "2024-04-22T10:30:45.123Z"
    },
    {
      "id": 2,
      "name": "Marie Martin",
      "email": "marie@example.com",
      "balance": 500.00,
      "createdAt": "2024-04-22T10:31:00.456Z"
    }
  ],
  "currentPage": 1,
  "pageSize": 10,
  "totalElements": 2,
  "totalPages": 1,
  "isLast": true
}
```

**Calcul Pagination**:
- `totalPages = ceil(totalElements / limit)`
- `isLast = (currentPage == totalPages)`

---

### F3: Récupérer Détails d'un Compte 👤

**Endpoint**: `GET /accounts/{accountId}`  
**Authentification**: Aucune

**Paramètres**:
| Paramètre | Type | Requis |
|-----------|------|--------|
| `accountId` | Long | ✓ |

**Réponse (200 OK)**:
```json
{
  "id": 1,
  "name": "JAPHET DJOMO",
  "email": "JAPHETDJOM@GMAIL.COM",
  "phone": "+237657786440",
  "balance": 1000.00,
  "createdAt": "2024-04-22T10:30:45.123Z",
  "updatedAt": "2024-04-22T10:30:45.123Z"
}
```

**Erreurs Possibles**:
- `404 Not Found`: Compte inexistant
- `400 Bad Request`: ID format invalide

---

### F4: Effectuer un Dépôt 💰

**Endpoint**: `POST /accounts/{accountId}/deposit`  
**Authentification**: Aucune

**Paramètres**:

*Path*:
| Paramètre | Type | Requis |
|-----------|------|--------|
| `accountId` | Long | ✓ |

*Body*:
```json
{
  "montant": 250.00
}
```

**Validation Montant**:
- Minimum: `0.01`
- Maximum: Pas de limite
- Décimales: 2

**Réponse (200 OK)**:
```json
{
  "message": "Dépôt effectué",
  "accountId": 1,
  "newBalance": 1250.00
}
```

**Effets Secondaires**:
- Balance augmentée
- Transaction créée et enregistrée
- `updatedAt` de l'account mis à jour
- Description: "Dépôt espèces"

**Erreurs Possibles**:
- `400 Bad Request`: Montant ≤ 0.00
- `404 Not Found`: Compte inexistant

---

### F5: Effectuer un Retrait 💸

**Endpoint**: `POST /accounts/{accountId}/withdraw`  
**Authentification**: Aucune

**Paramètres**:

*Path*:
| Paramètre | Type | Requis |
|-----------|------|--------|
| `accountId` | Long | ✓ |

*Body*:
```json
{
  "montant": 100.00
}
```

**Validation Montant**:
- Minimum: `0.01`
- Maximum: Solde actuel
- Décimales: 2

**Vérification Fonds**:
- Balance must be ≥ montant
- Sinon: Erreur 422 Unprocessable Entity

**Réponse (200 OK)**:
```json
{
  "message": "Retrait effectué",
  "accountId": 1,
  "newBalance": 900.00
}
```

**Effets Secondaires**:
- Balance diminuée
- Transaction créée
- `updatedAt` mis à jour
- Description: "Retrait espèces"

**Erreurs Possibles**:
- `400 Bad Request`: Montant ≤ 0.00
- `404 Not Found`: Compte inexistant
- `422 Unprocessable Entity`: Fonds insuffisants

---

### F6: Consulter l'Historique des Transactions 📜

**Endpoint**: `GET /accounts/{accountId}/transactions?limit=20`  
**Authentification**: Aucune

**Paramètres**:

*Path*:
| Paramètre | Type | Requis |
|-----------|------|--------|
| `accountId` | Long | ✓ |

*Query*:
| Paramètre | Type | Défaut |
|-----------|------|--------|
| `limit` | Integer | 20 |

**Tri**: Descendant (Plus récent d'abord)

**Réponse (200 OK)**:
```json
[
  {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "accountId": 1,
    "type": "WITHDRAWAL",
    "amount": 100.00,
    "newBalance": 900.00,
    "timestamp": "2024-04-22T10:32:15.789Z",
    "description": "Retrait espèces"
  },
  {
    "transactionId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "accountId": 1,
    "type": "DEPOSIT",
    "amount": 250.00,
    "newBalance": 1000.00,
    "timestamp": "2024-04-22T10:31:30.456Z",
    "description": "Dépôt espèces"
  }
]
```

**Types de Transaction**:
- `DEPOSIT`: Dépôt
- `WITHDRAWAL`: Retrait

**Erreurs Possibles**:
- `404 Not Found`: Compte inexistant

---

## Endpoints API {#endpoints-api}

### Vue d'ensemble
```
POST   /accounts                          F1: Créer compte
GET    /accounts                          F2: Lister comptes  
GET    /accounts/{accountId}              F3: Détails compte
POST   /accounts/{accountId}/deposit      F4: Dépôt
POST   /accounts/{accountId}/withdraw     F5: Retrait
GET    /accounts/{accountId}/transactions F6: Historique
```

### Tableau Récapitulatif

| Méthode | Endpoint | F# | Body | Params | Auth |
|---------|----------|----|----|--------|------|
| POST | /accounts | F1 | CreateAccountRequest | - | ✗ |
| GET | /accounts | F2 | - | page, limit | ✗ |
| GET | /accounts/{id} | F3 | - | - | ✗ |
| POST | /accounts/{id}/deposit | F4 | AmountRequest | - | ✗ |
| POST | /accounts/{id}/withdraw | F5 | AmountRequest | - | ✗ |
| GET | /accounts/{id}/transactions | F6 | - | limit | ✗ |

---

## Codes de Réponse HTTP {#codes-http}

### Codes de Succès

| Code | Signification | Endpoints |
|------|---------------|-----------|
| `200 OK` | Succès (retrait, dépôt, lecture) | F2, F3, F4, F5, F6 |
| `201 Created` | Ressource créée | F1 |

### Codes d'Erreur Client

| Code | Signification | Cause | Endpoints |
|------|---------------|-------|-----------|
| `400 Bad Request` | Requête invalide | Email/phone/montant invalide, solde négatif | F1, F4, F5 |
| `404 Not Found` | Ressource non trouvée | AccountId inexistant | F3, F4, F5, F6 |
| `409 Conflict` | Conflit | Email déjà utilisé | F1 |
| `422 Unprocessable Entity` | Impossible à traiter | Fonds insuffisants | F5 |

### Codes d'Erreur Serveur

| Code | Signification | Cause |
|------|---------------|-------|
| `500 Internal Server Error` | Erreur serveur | Bug, BDD non accessible |
| `503 Service Unavailable` | Service indisponible | BDD down, manque de mémoire |

---

## Exemples d'Utilisation {#exemples}

### Curl

#### Créer un compte
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "JAPHET DJOMO",
    "email": "JAPHETDJOM@GMAIL.COM",
    "phone": "+237657786440",
    "initialBalance": 1000.00
  }'
```

#### Faire un dépôt
```bash
curl -X POST http://localhost:8080/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"montant": 500.00}'
```

#### Voir l'historique
```bash
curl -X GET "http://localhost:8080/accounts/1/transactions?limit=10" \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch)

```javascript
// Créer un compte
const response = await fetch('http://localhost:8080/accounts', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'JAPHET DJOMO',
    email: 'JAPHETDJOM@GMAIL.COM',
    phone: '+237657786440',
    initialBalance: 1000.00
  })
});

const account = await response.json();
console.log('Compte créé:', account.id);

// Faire un dépôt
const depositResponse = await fetch(`http://localhost:8080/accounts/${account.id}/deposit`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ montant: 500.00 })
});

const deposit = await depositResponse.json();
console.log('Nouveau solde:', deposit.newBalance);
```

### Python

```python
import requests

BASE_URL = "http://localhost:8080"

# Créer un compte
account_data = {
    "name": "JAPHET DJOMO",
    "email": "JAPHETDJOM@GMAIL.COM",
    "phone": "+237657786440",
    "initialBalance": 1000.00
}

response = requests.post(f"{BASE_URL}/accounts", json=account_data)
account = response.json()
account_id = account['id']

# Faire un dépôt
deposit_data = {"montant": 500.00}
response = requests.post(f"{BASE_URL}/accounts/{account_id}/deposit", json=deposit_data)
print(f"Nouveau solde: {response.json()['newBalance']}")

# Voir l'historique
response = requests.get(f"{BASE_URL}/accounts/{account_id}/transactions")
transactions = response.json()
print(f"Nombre de transactions: {len(transactions)}")
```

---

## Tests {#tests}

### Tests Unitaires

#### Exécuter tous les tests
```bash
mvn test
```

#### Exécuter un test spécifique
```bash
mvn test -Dtest=AccountControllerTest#testCreateAccountWithAllFields
```

#### Voir la couverture de code
```bash
mvn test jacoco:report
# Ouvrir: target/site/jacoco/index.html
```

### Tests Manuels via Swagger UI

Voir le fichier: **[GUIDE_TEST_MANUEL.md](GUIDE_TEST_MANUEL.md)**

Incluent:
- ✅ 30 cas de test détaillés
- ✅ Pas à pas avec Swagger UI
- ✅ Vérifications de cohérence
- ✅ Troubleshooting

### Matrice de Traçabilité

| F# | Fonction | TC | Total |
|----|----------|----|----|
| F1 | Créer compte | TC1.1 à TC1.7 | 7 |
| F2 | Lister comptes | TC2.1 à TC2.5 | 5 |
| F3 | Récupérer détails | TC3.1 à TC3.3 | 3 |
| F4 | Dépôt | TC4.1 à TC4.6 | 6 |
| F5 | Retrait | TC5.1 à TC5.5 | 5 |
| F6 | Historique | TC6.1 à TC6.4 | 4 |
| **Total** | **6 Fonctionnalités** | **30 Cas** | **30** |

---

## Architecture {#architecture}

### Structure du Projet
```
src/main/java/com/example/bankapi/
├── BankApiApplication.java           Application Spring Boot
├── OpenApiConfig.java                Configuration Swagger/OpenAPI
├── SecurityConfig.java               Configuration Security (Spring Security)
├── controller/
│   ├── AccountController.java        Endpoints REST (6 endpoints)
│   └── GlobalExceptionHandler.java   Gestion centralisée des erreurs
├── dto/
│   ├── CreateAccountRequest.java     DTO: Créer compte
│   ├── AccountDetails.java           DTO: Détails compte
│   ├── AccountSummary.java           DTO: Résumé compte (listage)
│   ├── AmountRequest.java            DTO: Montant (dépôt/retrait)
│   ├── DepositResponse.java          DTO: Réponse dépôt
│   ├── WithdrawResponse.java         DTO: Réponse retrait
│   ├── PagedResponse.java            DTO: Réponse paginée
│   ├── TransactionDto.java           DTO: Transaction
│   └── ErrorResponse.java            DTO: Erreur
├── model/
│   ├── Account.java                  Entité compte (JPA)
│   ├── Transaction.java              Entité transaction (JPA)
│   └── TransactionType.java          Enum des types
├── repository/
│   ├── AccountRepository.java        Repository comptes (Spring Data)
│   └── TransactionRepository.java    Repository transactions
└── service/
    ├── AccountService.java           Logique métier comptes
    ├── AccountNotFoundException.java  Exception custom
    ├── EmailAlreadyExistsException.java
    ├── InsufficientFundsException.java
    └── InvalidAmountException.java

src/test/java/com/example/bankapi/
└── controller/
    └── AccountControllerTest.java    Suite de tests (30 cas)

src/main/resources/
├── application.properties            Configuration Spring Boot
└── templates/
    ├── login.html                    Page login (Thymeleaf)
    └── accounts.html                 Page comptes (Thymeleaf)
```

### Dépendances Clés
- **Spring Boot 3.3.2**: Framework
- **Spring Data JPA**: ORM
- **Spring Security**: Authentification
- **Springdoc OpenAPI 2.1.0**: Documentation Swagger
- **H2 Database**: BDD développement
- **PostgreSQL**: BDD production

### Patterns Utilisés
- **MVC**: Séparation contrôleur/service/repository
- **DTO**: Transfer objects pour API
- **Exception Handling**: GlobalExceptionHandler
- **Transactional**: @Transactional pour ACID
- **Repository Pattern**: Spring Data JPA

---

## Configuration {#configuration}

### application.properties
```properties
# Serveur
server.port=${PORT:8080}

# BDD - Développement (H2)
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:bankdb}
spring.datasource.driver-class-name=${DB_DRIVER:org.h2.Driver}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=${DB_PLATFORM:org.hibernate.dialect.H2Dialect}

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Thymeleaf
spring.thymeleaf.cache=false
```

### Variables d'Environnement
```bash
# Port
PORT=8080

# BDD Production (PostgreSQL)
DATABASE_URL=jdbc:postgresql://localhost:5432/bankdb
DB_DRIVER=org.postgresql.Driver
DB_USERNAME=postgres
DB_PASSWORD=password
DB_PLATFORM=org.hibernate.dialect.PostgreSQL82Dialect
```

### Security Config
- Endpoints `/accounts/**` publiques (demo mode)
- Dans production: Implémenter JWT/OAuth2
- See: `SecurityConfig.java`

---

## Troubleshooting {#troubleshooting}

### ❌ "Connection refused"
```
Cause: API non démarrée
Solution:
1. mvn spring-boot:run
2. Vérifier port 8080: netstat -ano | findstr :8080
3. Attendre "Started BankApiApplication"
```

### ❌ "404 Not Found" Swagger UI
```
Cause: Dépendance Swagger manquante
Solution:
1. Vérifier pom.xml
2. mvn clean install
3. Redémarrer l'API
```

### ❌ "409 Conflict" lors de la création
```
Cause: Email déjà utilisé
Solution:
1. H2 Console: http://localhost:8080/h2-console
2. DELETE FROM accounts;
3. Réessayer
```

### ❌ Montant invalide
```
Cause: Format JSON incorrect
Solution:
Utiliser: {"montant": 1000.00}
Pas: {"montant": "1.000,00"}
```

---

## Spécifications Complètes

Voir les documents:
- **[ANALYSE_API.md](ANALYSE_API.md)**: Spécifications détaillées (30 cas de test)
- **[GUIDE_TEST_MANUEL.md](GUIDE_TEST_MANUEL.md)**: Guide complet des tests manuels

---

## Support

**Contact**: japhetdjomo@gmail.com  
**Version**: 1.0.0  
**Dernière mise à jour**: 22 avril 2026  
**Status**: ✅ Production-Ready

---

## Déploiement Render

Ce projet est prêt pour un déploiement direct sur Render via le fichier `render.yaml` à la racine.

### Ce que Render crée
- 1 service web Docker pour Spring Boot
- 1 base PostgreSQL liée automatiquement au service
- 1 profil Spring `prod`

### Réglages déjà prévus
- Build Docker: Maven compile dans l'image
- Start Docker: `java -Dserver.port=${PORT:-8080} -jar /app/app.jar`
- Profil actif: `prod`
- Port: fourni par Render via `PORT`
- Base de données: injectée automatiquement via les variables `SPRING_DATASOURCE_*`

### Fichiers utilisés par Render
- [`render.yaml`](render.yaml): blueprint du service et de la base
- [`Dockerfile`](Dockerfile): build et lancement de l’application

### Après le déploiement
1. Ouvre le service Render.
2. Vérifie les logs de démarrage.
3. Attends le premier build complet.
4. Si tu ajoutes des changements, Render redéploie automatiquement grâce à `autoDeploy: true`.

### En cas de souci
- Si la base n’est pas accessible, vérifie que le service PostgreSQL a bien été créé.
- Si l’application ne démarre pas, vérifie que le jar généré est bien `target/bank-api-0.0.1-SNAPSHOT.jar`.
- Si tu changes le packaging Maven, pense à adapter `COPY --from=build /app/target/*.jar /app/app.jar`.

---

## License

CEL Project @2024 - All Rights Reserved

---

**Prêt à utiliser! Pour démarrer:**
```bash
mvn spring-boot:run
# Ouvrir: http://localhost:8080/swagger-ui.html
```

