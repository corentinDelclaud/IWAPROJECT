# Guide de Test de la Connexion Front-Back
## Procédure de test

### Étape 1: Démarrer le Service-Catalog
```bash
cd back/service-catalog
mvn spring-boot:run
```
**Port**: 8083
**Test**: http://localhost:8083/api/products

### Étape 2: Démarrer l'API Gateway
```bash
cd back/api-gateway
mvn spring-boot:run
```
**Port**: 8090
**Test**: http://localhost:8090/api/products

### Étape 3: Démarrer le Front-End
```bash
cd front
npx expo start --tunnel
```

### Étape 4: Tests à effectuer

#### Test 1: Vérifier que les produits s'affichent
- Ouvrir le marketplace
- Vérifier que les produits de la base de données apparaissent
- Les faux produits ne devraient plus être présents

#### Test 2: Tester les filtres de jeu
- Cliquer sur "League of Legends" → Seuls les produits LOL apparaissent
- Cliquer sur "Valorant" → Seuls les produits Valorant apparaissent
- Cliquer sur "Tous" → Tous les produits réapparaissent

#### Test 3: Tester les filtres de catégorie
- Cliquer sur "Coaching" → Seuls les services de coaching apparaissent
- Cliquer sur "Boost" → Seuls les services de boost apparaissent
- Combiner avec un filtre de jeu (ex: Valorant + Coaching)

#### Test 4: Tester la recherche
- Taper un mot-clé dans la barre de recherche
- Les résultats doivent être filtrés côté client

#### Test 5: Tester la page produit
- Cliquer sur un produit dans la marketplace
- La page produit doit s'afficher avec les détails du produit sélectionné
- Les informations doivent correspondre à celles de la base de données

## Mapping des données

### Données Backend → Frontend

| Backend | Frontend | Transformation |
|---------|----------|----------------|
| `id` | `id` | Direct |
| `name` | `title` | Direct |
| `description` | `description` | Direct |
| `price` | `price` | Formaté en string avec "€" |
| `game` | `game` | Converti en minuscules |
| `serviceType` | `category` | Converti en minuscules |
| `idProvider` | `provider` | Formaté en "Provider X" |
| `imageUrl` | `image` | URL de l'image ou placeholder |
| `rating` | `rating` | Valeur par défaut: 4.5 |
| N/A | `reviews` | Valeur par défaut: 127 |
| N/A | `delivery` | Valeur par défaut: "24-48h" |
| N/A | `online` | Valeur par défaut: true |
| N/A | `badges` | Valeur par défaut: ["Vérifié", "Rapide"] |

## Console de débogage

### Backend
```bash
# Dans le terminal du service-catalog
# Vous devriez voir les logs SQL:
DEBUG o.s.jdbc.core.JdbcTemplate - Executing prepared SQL query
DEBUG o.s.jdbc.core.JdbcTemplate - Executing prepared SQL statement [SELECT * FROM SERVICE WHERE ...]
```

### Frontend
```javascript
// Dans la console du navigateur ou terminal Expo
// Vous devriez voir:
Fetching products from: http://localhost:8090/api/products
Received X products from backend
```

## Dépannage

### Erreur CORS
**Problème**: Le front-end ne peut pas accéder à l'API Gateway
**Solution**: Vérifier la configuration CORS dans `application.properties` de l'API Gateway

### Erreur 404 sur /api/products
**Problème**: L'API Gateway ne trouve pas le service-catalog
**Solution**: Vérifier que le service-catalog tourne bien sur le port 8083

### Aucun produit affiché
**Problème**: La base de données est vide ou les données ne sont pas compatibles
**Solution**: 
1. Vérifier le fichier `data.sql.old` du service-catalog
2. Vérifier que les types d'énumération (ServiceType) correspondent

### Les filtres ne fonctionnent pas
**Problème**: Les paramètres de filtrage ne sont pas bien passés
**Solution**: Vérifier les logs du backend pour voir les paramètres reçus

## Prochaines étapes

1. **Ajouter des images réelles** aux produits dans la base de données
2. **Implémenter le système de reviews** (nombre de commentaires)
3. **Ajouter un endpoint de recherche** côté backend pour améliorer les performances
4. **Mettre en place un cache** pour réduire les appels API
5. **Ajouter une pagination** pour gérer de grandes quantités de produits

