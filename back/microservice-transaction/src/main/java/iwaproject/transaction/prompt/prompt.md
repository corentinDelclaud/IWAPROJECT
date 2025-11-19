# Quest qu'il faut faire ?

Le but est de modifier le microservice transaction pour le lié logiquement aux autres microservices. Les fichiers lié à ce microservice sont décrit dans la commande tree terminal que tu as reçu. d'autres fichiers lié au reste de l'appli sont présent mais on les modifieras pas. 

## Qu'est ce qui n'est pas bien implémenter

1) on possède et initialise de faux produits, alors que ceux si sont présent dans une autre DB, relié au microservice service-catalog, joignable par la gateway. je t'aurai fourni le controller (ProductController) ainsi que la gateway (GatewayConfig) qui va permettre au microservice transaction de faire un get by id du produit pour vérifier que le l'id produit (reçu du dto) correspond à un vrai produit, dont l'attribut isAvailable nous donne si on accepte ou non la requête, et l'attribut idProvider nous donne l'id provider de notre transaction. 

2) Actuellement les DTO de create, get et update contiennent un champ id correspondant l'id de la personne qui envoie la requête, ce qui est une grande faille de sécurité. on va intéroger Keycloak directement via le réseau docker compose pour retrouver la secret, afin de décrypter le token, récuperer l'id de l'user authentifier, qui est un id keycloak (pas un souci). le client Keycloak qu'on utilise est "auth-client". Je t'ai fourni un context keycloak à propos de la récupération de secret. c'est ce id_client qu'on va enregistrer dans notre transaction, et bien sûr sur l'updtate et le get, on vérifie toujours le token qu'on reçoit. 

## Ce que tu dois concrètement faire en connaissance de ce qu'il faut faire et de l'ensemble de ton context

Donner toutes les modifications minimales sur chaque fichier du microservice transaction afin de lié ce microservice à celui du catalogue et au keycloak comme demandé. 
pour toutes ces communications externe, il faudra un jolie log (commenté par #debuglog) qui me permettra de savoir si ces appels externe ont fonctionné.
Précise bien le nom du fichier à modifier. 

fais un rapport de ce que tu as fait et explique pourquoi cela marchera ou non ou sur quoi peut on émettre des doutes. 