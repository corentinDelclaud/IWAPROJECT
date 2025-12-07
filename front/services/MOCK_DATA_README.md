# Mode Test - Données Mockées

## 🎯 Description

Ce projet inclut un mode test qui permet de tester l'interface utilisateur sans dépendre du backend.

## 📝 Comment activer/désactiver le mode test

Dans le fichier `services/productService.ts`, modifiez la constante `USE_MOCK_DATA` :

```typescript
// ⚠️ MODE TEST: Mettre à true pour utiliser les données mockées, false pour utiliser le backend réel
const USE_MOCK_DATA = true;  // Mode test activé
// const USE_MOCK_DATA = false;  // Mode production (backend réel)
```

## 🎮 Données de test disponibles

Le fichier `services/mockProductData.ts` contient **12 produits de test** répartis comme suit :

### Par Jeu
- **VALORANT** : 3 produits (Coaching, Boost, Compte)
- **LEAGUE_OF_LEGENDS** : 3 produits (Boost, Compte, Coaching)
- **ROCKET_LEAGUE** : 3 produits (Coaching, Boost, Compte)
- **TEAMFIGHT_TACTICS** : 2 produits (Coaching, Boost)
- **OTHER** : 1 produit (Coaching multi-jeux)

### Par Type de Service
- **COACHING** : 5 produits
- **BOOST** : 4 produits
- **ACCOUNT_RESALING** : 3 produits

### Prix
- De **20€** à **55€**
- Tous les produits ont des prix différents pour tester les filtres

## ✨ Fonctionnalités testables

### 1. Liste des produits (Marketplace)
- Affichage de tous les produits
- Images par défaut selon le jeu
- Badges et statuts (en ligne/hors ligne)

### 2. Filtres
- **Filtre par jeu** : Tous, League of Legends, TFT, Rocket League, Valorant, Other
- **Filtre par catégorie** : Tous, Boost, Coaching, Account Resaling
- **Recherche textuelle** : Par titre ou nom du provider

### 3. Page détail produit
- Affichage complet des informations
- Image du jeu par défaut
- Prix, description, rating
- Badges et statut en ligne

### 4. Création de produit
- Ajouter un nouveau produit
- Le nouveau produit reçoit automatiquement un ID unique
- Le produit est ajouté à la liste (en mémoire uniquement)

### 5. Suppression de produit
- Supprimer un produit existant
- Le produit est retiré de la liste (en mémoire uniquement)

## 🔧 Avantages du mode test

1. **Développement sans backend** : Testez l'interface même si le backend est down
2. **Latence simulée** : Les requêtes simulent un délai réseau réaliste (200-400ms)
3. **Données prévisibles** : Toujours les mêmes données pour tester différents scénarios
4. **Pas de pollution de la base** : Les modifications ne persistent pas

## ⚠️ Limitations

- Les données créées/supprimées ne persistent que pendant la session
- Pas de véritable authentification
- Pas de gestion d'erreurs spécifiques au backend

## 🚀 Passage en production

Quand vous êtes prêt à utiliser le backend réel :

1. Mettez `USE_MOCK_DATA = false` dans `productService.ts`
2. Assurez-vous que le backend est accessible
3. Vérifiez que l'URL de l'API est correcte dans `getBaseUrl()`

## 📊 Exemple de produit mock

```typescript
{
    idService: 1,
    description: "Coaching personnalisé Valorant - Analyse de gameplay...",
    price: 30,
    game: "VALORANT",
    serviceType: "COACHING",
    idProvider: "94ba8d62-6521-4c66-87b5-edac76514bff",
    unique: false,
    isAvailable: true,
    providerName: "ProGamer_Valorant",
    rating: 4.8
}
```

Ce produit sera automatiquement transformé en format frontend avec images, badges, etc.

