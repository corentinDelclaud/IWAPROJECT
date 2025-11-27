# Quest qu'il faut faire ?

Le but est de modifier le microservice transaction pour le lié logiquement aux autres microservices. Les fichiers lié à ce microservice sont décrit dans la commande tree terminal que tu as reçu. d'autres fichiers lié au reste de l'appli sont présent mais on les modifieras pas. 

## Qu'est ce qui n'est pas bien implémenter

1) on possède et initialise de faux produits, alors que ceux si sont présent dans une autre DB, relié au microservice service-catalog, joignable par la gateway. je t'aurai fourni le controller (ProductController) ainsi que la gateway (GatewayConfig) qui va permettre au microservice transaction de faire un get by id du produit pour vérifier que le l'id produit (reçu du dto) correspond à un vrai produit, dont l'attribut isAvailable nous donne si on accepte ou non la requête, et l'attribut idProvider nous donne l'id provider de notre transaction. 

2) Actuellement les DTO de create, get et update contiennent un champ id correspondant l'id de la personne qui envoie la requête, ce qui est une grande faille de sécurité. hors depuis la mise à jour d'avant, la gateway normalement vérifie et décode le token et ajoute ces headers : 
X-User-Id : ID Keycloak de l'utilisateur
X-User-Username : Nom d'utilisateur
X-User-Email : Email
X-User-Roles : Rôles (séparés par virgules)

c'est le headers de User id qui nous intéresse et donc qu'on utilise et enregistre dans notre db. 

pour ce qui est de la table des conversation c'est un autre micro service aussi on l'utilise pas encore mais en gros vu que notre création de transaction entrene une création de conversation, et bien pour l'instant on mets juste un hook / handle qui fait un logs : not implemented yet. on a donc plus besoin de cette entité dans nos micro service.

## Ce que tu dois concrètement faire en connaissance de ce qu'il faut faire et de l'ensemble de ton context

Donner toutes les modifications minimales sur chaque fichier du microservice transaction afin de lié ce microservice à celui du catalogue et impl
pour toutes ces communications externe, il faudra un jolie log (commenté par #debuglog) qui me permettra de savoir si ces appels externe ont fonctionné.
Précise bien le nom du fichier à modifier. on garde les mêmes routes mais donc on a plus notre id mais juste l'id du produit à fournir pour un create et just l'id de la transaction à fournir pour le update. 

fais un rapport de ce que tu as fait et explique pourquoi cela marchera ou non ou sur quoi peut on émettre des doutes. si pas de doutes pas de problème tant mieux. 