## Creation
Pour tout nouveau fichier, donne la commande touch adapté. 

## But 
j'ai initialialisé le micro service messagerie, tu vas devoir crée son véritable contenu et l'intégré au reste de la stack (docker compose), donc ce microservice a sa propre db avec ces deux tables :

Conversation : 
- son id, (auto increment, int)
- idClient,  (string car uuid). 
- idProvider, (string car uuid). 

Message :
- son id, (auto increment, int)
- idFrom (string, uuid)
-  content (string)
- date (Date)

voici les différentes requetes possibles (écrites en language naturelle)
avoir la liste de conversations : le dto de réponse c'est une liste de : idOther, last Message. 
comme ça, le front va fetch le pseudo de la personne avec qui on parle, (rien à faire de notre côté), et pourra directement utilisé le lastMessage pour avoir l'aperçu de la conversation dans la messagerie. (classique)

recuperer pour une conversation, tous (oui tous) les messages d'une conversation précise, 
le résultat est une liste de (content : string, isMine : boolean), classique

post : crée un message sur une conversation (content + conversation id). 

## 




