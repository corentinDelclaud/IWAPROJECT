## But
Gère le processus d’achat de services, de la création d’une discussion centrée sur un produit jusqu’au paiement final. Inspiré de l’architecture de BlaBlaCar, le service relie l’interface de messagerie et la transaction, qui peut passer par plusieurs états : demande, confirmation, prépaiement, annulation, confirmation de service rendu et finalisation (paiement au vendeur). La transaction débute lorsqu’un client demande un service ou ouvre une discussion via « commander » ou « envoyer un message ».
## Techno
Spring Boot (REST), PostgreSQL, Kafka (producteur), JWT pour auth, Docker, CI/CD. Publie un événement Kafka à chaque changement d’état. Pour ce microservice, on veut utiliser Spring Data JPA pour manipuler les données côté serveur et faire la connexion DB. 
Attention, ça c'est le résultat final de la stack, ça veut pas dire que tu dois me donner toutes ces implémentations à chaque prompt, mais bien uniquement ce que je demande.  

## Endpoint

POST
/transaction
un demandeur de service (user comme client) peut créer une transaction (état messaging ou requested). Pour crée une une transaction, il faut fournir comme json : son id (dison user_id, et l'id du service/produit (table produit) et également un boolean : direct_request ), cela vas nous permettre de crée une transaction. 
une création de transaction crée automatiquement une conversation avec 
GET
/transaction/{id}/
récupérer la transaction (état, service_id concerné, conversation_id concerné) 
PUT
/transaction/{id}/state
déclarer un changement d’état. (exemple :  le vendeur accepte la demande de services )
donné à mettre : id transaction, id user, et l'enum de changement. 
voici toutes les règles metier sur l'état : 
la création d'une transaction va mettre l'état à exchanging ou requested selon l'action du client. le passage de exchanging à requested n'est possible que par le client (pas le provider). le passage de requested à requested_accepted n'est possible que par le provider (vendeur), le passage de requested accepted à prepayed sera par un système externe, mais pour les test, il sera possible si l'id user est 999, l'idée des états :     CLIENT_CONFIRMED,
PROVIDER_CONFIRMED,
DOUBLE_CONFIRMED, c'est l'idée que le premier des deux qui va confirmé qui va set l'état, et le deuxième mettra à double_confirmed, à ce moment là, on va faire un log de debug, un petit sleep puis passera à finished_and_payed. avant l'état prepayed, les deux users on le droit d'annuler et de passer l'état à canceled pour lequel plus rien n'est possible ensuite. 

