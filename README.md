# IWAPROJECT

Front Folder

Back Folder

## Les solutions techniques à mettre en oeuvre

-Utilisation du framework React Native Expo pour le développement du frontend mobile

-Utilisation de la solution IAM open source Keycloack

-Utilisation d'une architecture microservice pour le backend

-Utilisation du framework java open source Spring Boot

-Backend avec API REST ou GraphQL

-Bases de données relationnelle ou NoSQL

-Hébergement sur une plateforme cloud (ex. : Heroku, Vercel, Firebase…)


lancement prod
1er terminal
cd back
docker-compose up --build -d

2nd terminal
cd front
$env:EXPO_PUBLIC_API_HOST="TON IP"; npm start  (ipconfig sur windows par exemple)
puis sur le web : http://localhost:8085 (port de keycloack)
user : admin
pwd : admin

passer dans du realm master à IWAPROJECT
dans clients, cliquer sur auth-service puis dans Valid redirect URIs ajouter url de votre expo (ex: exp://cfxwv_8-anonymous-19000.exp.direct/--/*)

vous pouvez maintenant utiliser l'application mobile