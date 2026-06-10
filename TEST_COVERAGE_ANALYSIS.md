# Analyse de couverture de tests sur 5 fonctionnalités de l'API

Ce document extrait 5 fonctionnalités directement du code source de l'API bancaire et construit pour chacune :

- le graphe de flot de contrôle (Control Flow Graph, CFG)
- les chemins de test pertinents
- la couverture des instructions (statement coverage)
- la couverture des branches (branch coverage)
- une table de couverture

Fonctionnalités retenues :

1. `createAccount(CreateAccountRequest request)`
2. `listAccounts(int page, int limit)`
3. `getAccountDetails(Long id)`
4. `deposit(Long id, BigDecimal amount)`
5. `withdraw(Long id, BigDecimal amount)`

Source principale : [AccountService.java](/c:/Users/COMPUTER%20STORES/Desktop/projet%20perso/api/src/main/java/com/example/bankapi/service/AccountService.java)

Référence des tests existants : [AccountControllerTest.java](/c:/Users/COMPUTER%20STORES/Desktop/projet%20perso/api/src/test/java/com/example/bankapi/controller/AccountControllerTest.java)

## Méthode

- Une instruction correspond ici à un noeud métier significatif dans le service.
- Une branche correspond à une décision binaire observable dans le code source.
- Quand une méthode n'a pas de `if` explicite, son CFG a un seul chemin nominal.
- Les exceptions levées par `orElseThrow(...)` sont modélisées comme une bifurcation `compte trouvé ?`.

## F1. Création de compte

Méthode source : `AccountService.createAccount`

### CFG

```text
N1  Début
N2  email existe ?
    ├─ Oui -> N3
    └─ Non -> N4
N3  throw EmailAlreadyExistsException
N4  initialBalance == null ?
    ├─ Oui -> N5
    └─ Non -> N6
N5  balance = 0
N6  balance = initialBalance / ou valeur déjà calculée
N7  balance < 0 ?
    ├─ Oui -> N8
    └─ Non -> N9
N8  throw InvalidAmountException
N9  créer Account
N10 save(account)
N11 return AccountDetails
N12 Fin
```

### Chemins

- `P1`: `N1 -> N2[Oui] -> N3 -> N12`
- `P2`: `N1 -> N2[Non] -> N4[Oui] -> N5 -> N7[Non] -> N9 -> N10 -> N11 -> N12`
- `P3`: `N1 -> N2[Non] -> N4[Non] -> N6 -> N7[Oui] -> N8 -> N12`
- `P4`: `N1 -> N2[Non] -> N4[Non] -> N6 -> N7[Non] -> N9 -> N10 -> N11 -> N12`

### Jeux de test minimaux

- Statement testing minimal : `P1 + P3 + P4`
  Cela couvre tous les noeuds au moins une fois.
- Branch testing minimal : `P1 + P2 + P3 + P4`
  Cela couvre tous les résultats de décision :
  `email existe ?`, `initialBalance == null ?`, `balance < 0 ?`
- Path testing ciblé : `P1, P2, P3, P4`

### Table de couverture

| Cas | Description | Chemin | Instructions couvertes | Branches couvertes | Statement coverage cumulé | Branch coverage cumulé | Test existant |
|---|---|---|---|---|---:|---:|---|
| TC1 | Email déjà existant | P1 | N1 N2 N3 | N2=Oui | 25% | 16.7% | `TC1.3` |
| TC2 | Solde initial négatif | P3 | N1 N2 N4 N6 N7 N8 | N2=Non, N4=Non, N7=Oui | 66.7% | 66.7% | `TC1.6` |
| TC3 | Création valide avec solde | P4 | N1 N2 N4 N6 N7 N9 N10 N11 | N2=Non, N4=Non, N7=Non | 91.7% | 83.3% | `TC1.1` |
| TC4 | Création valide sans solde initial | P2 | N1 N2 N4 N5 N7 N9 N10 N11 | N2=Non, N4=Oui, N7=Non | 100% | 100% | `TC1.2` |

## F2. Liste paginée des comptes

Méthode source : `AccountService.listAccounts`

### CFG

```text
N1 Début
N2 créer Pageable
N3 charger page de comptes
N4 transformer en AccountSummary
N5 construire PagedResponse
N6 Fin
```

### Chemins

- `P1`: `N1 -> N2 -> N3 -> N4 -> N5 -> N6`

### Jeux de test minimaux

- Statement testing minimal : `P1`
- Branch testing minimal : `P1`
- Path testing : `P1`

### Table de couverture

| Cas | Description | Chemin | Instructions couvertes | Branches couvertes | Statement coverage cumulé | Branch coverage cumulé | Test existant |
|---|---|---|---|---|---:|---:|---|
| TC5 | Liste par défaut | P1 | N1 N2 N3 N4 N5 | Aucune branche | 100% | 100% | `TC2.1` |

Note : cette méthode ne contient pas de décision métier explicite. Le `branch coverage` est donc trivial sur le code source de la méthode.

## F3. Détails d'un compte

Méthode source : `AccountService.getAccountDetails`

### CFG

```text
N1 Début
N2 compte trouvé ?
    ├─ Non -> N3
    └─ Oui -> N4
N3 throw AccountNotFoundException
N4 return AccountDetails
N5 Fin
```

### Chemins

- `P1`: `N1 -> N2[Oui] -> N4 -> N5`
- `P2`: `N1 -> N2[Non] -> N3 -> N5`

### Jeux de test minimaux

- Statement testing minimal : `P1 + P2`
- Branch testing minimal : `P1 + P2`
- Path testing : `P1 + P2`

### Table de couverture

| Cas | Description | Chemin | Instructions couvertes | Branches couvertes | Statement coverage cumulé | Branch coverage cumulé | Test existant |
|---|---|---|---|---|---:|---:|---|
| TC6 | Compte existant | P1 | N1 N2 N4 | N2=Oui | 60% | 50% | `TC3.1` |
| TC7 | Compte inexistant | P2 | N1 N2 N3 | N2=Non | 100% | 100% | `TC3.2` |

## F4. Dépôt

Méthode source : `AccountService.deposit`

### CFG

```text
N1  Début
N2  montant valide ?
    ├─ Non -> N3
    └─ Oui -> N4
N3  throw InvalidAmountException
N4  compte trouvé ?
    ├─ Non -> N5
    └─ Oui -> N6
N5  throw AccountNotFoundException
N6  newBalance = balance + amount
N7  setBalance(newBalance)
N8  créer Transaction
N9  save(transaction)
N10 return DepositResponse
N11 Fin
```

### Chemins

- `P1`: `N1 -> N2[Non] -> N3 -> N11`
- `P2`: `N1 -> N2[Oui] -> N4[Non] -> N5 -> N11`
- `P3`: `N1 -> N2[Oui] -> N4[Oui] -> N6 -> N7 -> N8 -> N9 -> N10 -> N11`

### Jeux de test minimaux

- Statement testing minimal : `P1 + P2 + P3`
- Branch testing minimal : `P1 + P2 + P3`
- Path testing : `P1 + P2 + P3`

### Table de couverture

| Cas | Description | Chemin | Instructions couvertes | Branches couvertes | Statement coverage cumulé | Branch coverage cumulé | Test existant |
|---|---|---|---|---|---:|---:|---|
| TC8 | Montant invalide | P1 | N1 N2 N3 | N2=Non | 27.3% | 25% | `TC4.3` ou `TC4.4` |
| TC9 | Compte inexistant | P2 | N1 N2 N4 N5 | N2=Oui, N4=Non | 45.5% | 75% | `TC4.2` |
| TC10 | Dépôt valide | P3 | N1 N2 N4 N6 N7 N8 N9 N10 | N2=Oui, N4=Oui | 100% | 100% | `TC4.1` |

## F5. Retrait

Méthode source : `AccountService.withdraw`

### CFG

```text
N1  Début
N2  montant valide ?
    ├─ Non -> N3
    └─ Oui -> N4
N3  throw InvalidAmountException
N4  compte trouvé ?
    ├─ Non -> N5
    └─ Oui -> N6
N5  throw AccountNotFoundException
N6  balance < amount ?
    ├─ Oui -> N7
    └─ Non -> N8
N7  throw InsufficientFundsException
N8  newBalance = balance - amount
N9  setBalance(newBalance)
N10 créer Transaction
N11 save(transaction)
N12 return WithdrawResponse
N13 Fin
```

### Chemins

- `P1`: `N1 -> N2[Non] -> N3 -> N13`
- `P2`: `N1 -> N2[Oui] -> N4[Non] -> N5 -> N13`
- `P3`: `N1 -> N2[Oui] -> N4[Oui] -> N6[Oui] -> N7 -> N13`
- `P4`: `N1 -> N2[Oui] -> N4[Oui] -> N6[Non] -> N8 -> N9 -> N10 -> N11 -> N12 -> N13`

### Jeux de test minimaux

- Statement testing minimal : `P1 + P2 + P3 + P4`
- Branch testing minimal : `P1 + P2 + P3 + P4`
- Path testing : `P1 + P2 + P3 + P4`

### Table de couverture

| Cas | Description | Chemin | Instructions couvertes | Branches couvertes | Statement coverage cumulé | Branch coverage cumulé | Test existant |
|---|---|---|---|---|---:|---:|---|
| TC11 | Montant invalide | P1 | N1 N2 N3 | N2=Non | 23.1% | 16.7% | cas à ajouter |
| TC12 | Compte inexistant | P2 | N1 N2 N4 N5 | N2=Oui, N4=Non | 38.5% | 50% | cas à ajouter |
| TC13 | Fonds insuffisants | P3 | N1 N2 N4 N6 N7 | N2=Oui, N4=Oui, N6=Oui | 61.5% | 83.3% | `TC5.2` |
| TC14 | Retrait valide | P4 | N1 N2 N4 N6 N8 N9 N10 N11 N12 | N2=Oui, N4=Oui, N6=Non | 100% | 100% | `TC5.1` |

## Synthèse globale

| Fonctionnalité | Décisions | Chemins indépendants | Jeu minimal pour 100% statements | Jeu minimal pour 100% branches |
|---|---:|---:|---:|---:|
| Création de compte | 3 | 4 | 3 tests | 4 tests |
| Liste paginée | 0 | 1 | 1 test | 1 test |
| Détails compte | 1 | 2 | 2 tests | 2 tests |
| Dépôt | 2 | 3 | 3 tests | 3 tests |
| Retrait | 3 | 4 | 4 tests | 4 tests |
| Total | 9 | 14 | 13 tests | 14 tests |

## Jeu minimal recommandé

Pour couvrir les 5 fonctionnalités retenues avec 100% de statement coverage et 100% de branch coverage sur les méthodes de service analysées, le jeu minimal recommandé est :

1. `createAccount` avec email dupliqué
2. `createAccount` avec solde nul implicite
3. `createAccount` avec solde négatif
4. `createAccount` valide avec solde positif
5. `listAccounts` valide
6. `getAccountDetails` compte existant
7. `getAccountDetails` compte inexistant
8. `deposit` montant invalide
9. `deposit` compte inexistant
10. `deposit` valide
11. `withdraw` montant invalide
12. `withdraw` compte inexistant
13. `withdraw` fonds insuffisants
14. `withdraw` valide

## Remarques

- Les tests déjà présents couvrent une grande partie de cette base, surtout pour `createAccount`, `deposit` et `withdraw`.
- Deux cas manquent clairement dans les tests visibles : `withdraw` avec montant invalide et `withdraw` sur compte inexistant.
- Certains tests existants semblent ne pas être alignés avec le JSON réel de réponse, par exemple l'usage de `$.id` au lieu de `$.accountId` dans les DTO de détails.
- L'analyse ci-dessus porte sur le code métier des méthodes de service. Les validations automatiques Spring (`@Valid`) et les erreurs de binding HTTP ne sont pas comptées dans les CFG métier.
