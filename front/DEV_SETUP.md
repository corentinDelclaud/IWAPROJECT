# Démarrage du front avec Expo et Keycloak

Pour que l'authentification Keycloak fonctionne sur mobile et web, il faut indiquer à Expo l'IP locale de la machine qui héberge Keycloak.

## Étapes

1. **Trouver l'IP locale de votre machine**
   - Sur Linux/Mac :
     ```bash
     ip addr show | grep inet
     ```
   - Sur Windows :
     ```cmd
     ipconfig
     ```
   - Repérez une IP du type `192.168.x.x`, `10.x.x.x` ou parfois `162.x.x.x` (WiFi/Ethernet, pas Docker).

2. **Lancer Expo avec l'IP**
   - Remplacez `<VOTRE_IP>` par l'IP trouvée :
     ```bash
     EXPO_PUBLIC_API_HOST=<VOTRE_IP> npm start
     ```
   - Exemple :
     ```bash
     EXPO_PUBLIC_API_HOST=162.38.33.90 npm start
     ```

3. **Pourquoi ?**
   - L'IP est transmise à l'app pour que le front puisse contacter Keycloak et l'API depuis un mobile ou un autre poste.
   - Pas besoin de modifier le code ou `app.json`.

4. **Keycloak : Valid Redirect URIs**
   - Les URIs de redirection dans Keycloak doivent inclure :
     - `exp://*.exp.direct/--/*`
     - `exp://localhost:19000/--/*`
     - `http://localhost:19000/*`
     - `http://localhost:19006/*`
     - et l'URI exacte générée par Expo Go si besoin.

---

**Astuce** : Si l'IP change, relancez Expo avec la nouvelle IP.
