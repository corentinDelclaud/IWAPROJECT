## **Guide des Endpoints Stripe**

### **1. Gestion des Comptes Connectés**

#### **Créer un compte Stripe Connect**
- **Méthode** : `POST`
- **URL** : `/api/stripe/connect-account`
- **Corps de la requête** :
  ```json
  {
    "email": "jb@example.com"
  }
  ```
- **Réponse** :
  ```json
  {
    "accountId": "acct_1SVoov4JuzlA8zQj"
  }
  ```

---

#### **Créer un lien de configuration de compte**
- **Méthode** : `POST`
- **URL** : `/api/stripe/account-link`
- **Corps de la requête** :
  ```json
  {
    "accountId": "{{accountId}}"
  }
  ```
- **Réponse** :
  ```json
  {
    "url": "https://connect.stripe.com/setup/e/acct_1SVoov4..."
  }
  ```

---

#### **Vérifier le statut d’un compte**
- **Méthode** : `GET`
- **URL** : `/api/stripe/account-status/{{accountId}}`
- **Réponse** :
  ```json
  {
    "id": "acct_1SVoW13WISXNS7vn",
    "payoutsEnabled": true,
    "chargesEnabled": true,
    "detailsSubmitted": true
  }
  ```

---

### **2. Gestion des Produits**

#### **Créer un produit**
- **Méthode** : `POST`
- **URL** : `/api/stripe/product`
- **Corps de la requête** :
  ```json
  {
    "productName": "Coaching RL",
    "productDescription": "Session de coaching 1h personnalisée",
    "productPrice": 599,
    "accountId": "{{accountId}}"
  }
  ```
- **Réponse** :
  ```json
  {
    "productName": "Coaching RL",
    "productDescription": "Session de coaching 1h personnalisée",
    "productPrice": 599,
    "priceId": "price_1SVolo3WISXNS7vnHp4wtCBt"
  }
  ```

---

#### **Lister les produits d’un compte**
- **Méthode** : `GET`
- **URL** : `/api/stripe/products/{{accountId}}`
- **Réponse** :
  ```json
  [
    {
      "id": "prod_TSkGc8Q8poWMZD",
      "name": "Coaching RL",
      "description": "Session de coaching 1h personnalisée",
      "price": 599,
      "priceId": "price_1SVolo3WISXNS7vnHp4wtCBt",
      "image": "https://i.imgur.com/6Mvijcm.png"
    }
  ]
  ```

---

### **3. Gestion des Sessions de Paiement**

#### **Créer une session de paiement (Checkout)**
- **Méthode** : `POST`
- **URL** : `/api/stripe/checkout-session`
- **Corps de la requête** :
  ```json
  {
    "accountId": "{{accountId}}",
    "priceId": "{{priceId}}"
  }
  ```
- **Réponse** :
  ```json
  {
    "url": "https://checkout.stripe.com/c/pay/cs_test_a..."
  }
  ```

---

### **Remarques**
- Remplacer `{{accountId}}` et `{{priceId}}` par les identifiants réels retournés par les endpoints précédents.
- Les URLs sont basées sur un environnement local (`localhost:8090`). A adapter pour un environnement de production.
