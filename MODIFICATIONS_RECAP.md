# 🎯 Récapitulatif des Modifications - Connexion Front-Back

## 📋 Résumé
Connexion du marketplace front-end au service-catalog backend via l'API Gateway, permettant d'afficher les vrais produits de la base de données au lieu de données simulées.

---

## 🔧 Modifications Effectuées

### 1️⃣ **API Gateway** (`back/api-gateway/src/main/resources/application.properties`)

#### Avant :
```properties
spring.application.name=api-gateway
server.port=8090

# Routes vers service-catalog
spring.cloud.gateway.server.webflux.routes[0].id=service-catalog
spring.cloud.gateway.server.webflux.routes[0].uri=http://localhost:8083
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/products/**
```

#### Après :
```properties
spring.application.name=api-gateway
server.port=8090

# CORS Configuration - Permet au front-end d'accéder à l'API
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=http://localhost:8081,http://localhost:19000,http://localhost:19006
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=true

# Routes vers service-catalog
spring.cloud.gateway.server.webflux.routes[0].id=service-catalog
spring.cloud.gateway.server.webflux.routes[0].uri=http://localhost:8083
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/products/**
```

**📝 Explications :**
- **CORS ajouté** : Permet au front-end (ports 8081, 19000, 19006) de faire des requêtes HTTP vers l'API Gateway
- **Méthodes autorisées** : GET, POST, PUT, DELETE, OPTIONS pour toutes les opérations CRUD
- **Headers autorisés** : Tous les headers (pour JWT, Content-Type, etc.)
- **Credentials** : Permet l'envoi de cookies et credentials

---

### 2️⃣ **Service Product** (`front/services/productService.ts`)

#### Avant :
```typescript
const API_BASE_URL = 'http://localhost:8080/api/products';

export interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    game: string;
    serviceType: string;
    idProvider: number;
}
```

#### Après :
```typescript
const API_BASE_URL = 'http://localhost:8090/api/products';

export interface Product {
    id: number;
    title: string;
    description: string;
    price: string;  // "XX€"
    game: string;
    category: string;
    provider: string;
    rating: number;
    reviews: number;
    delivery: string;
    image: string;
    online: boolean;
    badges: string[];
}
```

**📝 Explications :**
- **URL changée** : Port 8090 (API Gateway) au lieu de 8080
- **Interface mise à jour** : Compatible avec le composant ProductCard
- **Mapper créé** : Transforme les données backend vers le format front-end
- **3 fonctions créées** :
  - `fetchProducts()` : Récupère tous les produits
  - `fetchProductById(id)` : Récupère un produit spécifique
  - `fetchProductsByFilters(filters)` : Récupère les produits filtrés

**🔄 Transformation des données :**
```typescript
Backend                 Frontend
--------                --------
name         →          title
price (number) →        price (string "XX€")
serviceType  →          category
idProvider   →          provider ("Provider X")
imageUrl     →          image (avec fallback)
+ Ajout de valeurs par défaut pour reviews, delivery, badges, etc.
```

---

### 3️⃣ **Marketplace** (`front/app/(tabs)/marketplace.tsx`)

#### Changements principaux :

1. **Import mis à jour** :
```typescript
import { fetchProducts, fetchProductsByFilters, Product } from "@/services/productService";
```

2. **État de chargement ajouté** :
```typescript
const [loading, setLoading] = useState(true);
```

3. **Filtrage backend au lieu de client** :
```typescript
useEffect(() => {
  const filters: any = {};
  if (selectedGame !== "all") filters.game = selectedGame;
  if (selectedCategory !== "all") filters.type = selectedCategory;
  
  const fetchPromise = Object.keys(filters).length > 0 
    ? fetchProductsByFilters(filters)
    : fetchProducts();
  
  fetchPromise.then((list) => {
    setProducts(list);
    setLoading(false);
  });
}, [selectedGame, selectedCategory]);
```

4. **Affichage conditionnel** :
```typescript
{loading ? (
  <ActivityIndicator />
) : filteredServices.length === 0 ? (
  <Text>Aucun produit trouvé</Text>
) : (
  <FlatList data={filteredServices} ... />
)}
```

**📝 Explications :**
- **Filtrage optimisé** : Les filtres de jeu et catégorie sont envoyés au backend (plus performant)
- **Recherche locale** : La barre de recherche filtre côté client (car pas d'endpoint de recherche backend)
- **UX améliorée** : Indicateur de chargement + message si aucun produit
- **Réactivité** : Se recharge automatiquement quand les filtres changent

---

## 📊 Flux de Données

```
┌─────────────────────────────────────────────────┐
│         FRONT-END (React Native/Expo)           │
│         http://localhost:8081                   │
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │  marketplace.tsx                         │  │
│  │  - Gère l'UI et les filtres              │  │
│  │  - Appelle productService                │  │
│  └─────────────────┬────────────────────────┘  │
│                    │                            │
│  ┌─────────────────▼────────────────────────┐  │
│  │  productService.ts                       │  │
│  │  - fetchProducts()                       │  │
│  │  - fetchProductsByFilters(filters)       │  │
│  │  - fetchProductById(id)                  │  │
│  └─────────────────┬────────────────────────┘  │
└────────────────────┼─────────────────────────────┘
                     │ HTTP GET
                     │ http://localhost:8090/api/products
                     ▼
┌─────────────────────────────────────────────────┐
│         API GATEWAY (Spring Cloud Gateway)      │
│         http://localhost:8090                   │
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │  Route: /api/products/**                 │  │
│  │  → http://localhost:8083                 │  │
│  │                                          │  │
│  │  CORS: Autorise le front-end            │  │
│  └─────────────────┬────────────────────────┘  │
└────────────────────┼─────────────────────────────┘
                     │ HTTP GET
                     │ http://localhost:8083/api/products
                     ▼
┌─────────────────────────────────────────────────┐
│      SERVICE-CATALOG (Spring Boot)              │
│      http://localhost:8083                      │
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │  ProductController                       │  │
│  │  - GET /api/products                     │  │
│  │  - GET /api/products/{id}                │  │
│  │  - GET /api/products/filter              │  │
│  └─────────────────┬────────────────────────┘  │
│                    │                            │
│  ┌─────────────────▼────────────────────────┐  │
│  │  ProductService                          │  │
│  │  - getAllProducts()                      │  │
│  │  - getProductById(id)                    │  │
│  │  - getProductsByFilters(filters)         │  │
│  └─────────────────┬────────────────────────┘  │
│                    │                            │
│  ┌─────────────────▼────────────────────────┐  │
│  │  ProductRepository (Spring Data JDBC)    │  │
│  └─────────────────┬────────────────────────┘  │
└────────────────────┼─────────────────────────────┘
                     │ SQL Query
                     ▼
┌─────────────────────────────────────────────────┐
│         Base de données H2 (en mémoire)         │
│         jdbc:h2:mem:productdb                   │
│                                                 │
│  Table SERVICE:                                 │
│  - ID_SERVICE (PK)                              │
│  - NAME                                         │
│  - DESCRIPTION                                  │
│  - PRICE                                        │
│  - GAME (LOL, VALORANT, CS2, etc.)              │
│  - SERVICE_TYPE (COACHING, BOOST, etc.)         │
│  - ID_PROVIDER                                  │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Comment Tester

### Étape 1 : Démarrer le backend

```bash
# Terminal 1 : Service-Catalog
cd back/service-catalog
mvn spring-boot:run
# Attend que le message "Started ProductServiceApplication" apparaisse
```

```bash
# Terminal 2 : API Gateway
cd back/api-gateway
mvn spring-boot:run
# Attend que le message "Started ApiGatewayApplication" apparaisse
```

### Étape 2 : Vérifier le backend

Ouvrir dans un navigateur ou Postman :
- **Service direct** : http://localhost:8083/api/products
- **Via Gateway** : http://localhost:8090/api/products

Les deux URLs doivent retourner la même liste de produits en JSON.

### Étape 3 : Démarrer le front-end

```bash
cd front
npx expo start
```

### Étape 4 : Tests fonctionnels

✅ **Test 1 : Affichage des produits**
- Ouvrir le marketplace
- ➡️ Les produits de la BDD doivent s'afficher (plus de faux produits)

✅ **Test 2 : Filtre par jeu**
- Cliquer sur "Valorant"
- ➡️ Seuls les produits Valorant apparaissent

✅ **Test 3 : Filtre par catégorie**
- Cliquer sur "Coaching"
- ➡️ Seuls les services de coaching apparaissent

✅ **Test 4 : Filtre combiné**
- Sélectionner "LOL" + "Boost"
- ➡️ Seuls les services de boost pour LOL apparaissent

✅ **Test 5 : Recherche**
- Taper un mot dans la barre de recherche
- ➡️ Les résultats sont filtrés en temps réel

✅ **Test 6 : Page produit**
- Cliquer sur un produit
- ➡️ La page de détail s'ouvre avec les bonnes infos

---

## 🐛 Dépannage

### Problème : "Aucun produit trouvé"

**Causes possibles :**
1. Le service-catalog n'est pas démarré
2. L'API Gateway n'est pas démarré
3. La base de données est vide

**Solution :**
```bash
# Vérifier les ports utilisés
netstat -ano | findstr "8083"  # Service-catalog
netstat -ano | findstr "8090"  # API Gateway

# Vérifier les logs du service-catalog
# Chercher : "Initialized JPA EntityManagerFactory"
```

### Problème : Erreur CORS

**Message** : `Access to fetch at 'http://localhost:8090/api/products' from origin 'http://localhost:8081' has been blocked by CORS policy`

**Solution :** Vérifier que la configuration CORS est bien dans `application.properties` de l'API Gateway

### Problème : Erreur 404

**Message** : `HTTP error! status: 404`

**Causes possibles :**
1. Le service-catalog n'est pas démarré sur le port 8083
2. La route de l'API Gateway est mal configurée

**Solution :**
```bash
# Tester directement le service-catalog
curl http://localhost:8083/api/products

# Tester via la gateway
curl http://localhost:8090/api/products
```

---

## 📈 Prochaines Améliorations

1. **Images des produits** : Ajouter des URLs d'images réelles dans la BDD
2. **Système de reviews** : Implémenter un endpoint pour récupérer le nombre de reviews
3. **Recherche backend** : Créer un endpoint `/api/products/search?q=...`
4. **Pagination** : Ajouter la pagination pour gérer de grandes quantités de produits
5. **Cache** : Mettre en place un système de cache côté front pour réduire les appels API
6. **Base de données persistante** : Passer de H2 (en mémoire) à PostgreSQL

---

## 📝 Notes Importantes

- **Port API Gateway** : 8090 (pas 8080 !)
- **Format du prix** : Le backend envoie un nombre, le front le formate en "XX€"
- **Filtres** : Envoyés en MAJUSCULES au backend (VALORANT, COACHING, etc.)
- **Gestion d'erreurs** : En cas d'erreur, le service retourne un tableau vide pour ne pas casser l'UI
- **Base H2** : Données en mémoire, rechargées à chaque démarrage du service

---

## ✅ Checklist de Validation

- [ ] Service-catalog démarre sur le port 8083
- [ ] API Gateway démarre sur le port 8090
- [ ] http://localhost:8083/api/products retourne des données
- [ ] http://localhost:8090/api/products retourne les mêmes données
- [ ] Le marketplace affiche les produits de la BDD
- [ ] Les filtres de jeu fonctionnent
- [ ] Les filtres de catégorie fonctionnent
- [ ] La recherche par mot-clé fonctionne
- [ ] La page produit s'ouvre correctement
- [ ] Aucune erreur CORS dans la console

---

**Date de modification** : 27 octobre 2025
**Fichiers modifiés** : 3
**Fichiers créés** : 2 (CONNEXION_TEST.md, test-api-gateway-catalog.http)

