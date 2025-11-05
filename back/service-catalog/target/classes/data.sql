-- Données d'initialisation pour les produits/services

-- Services pour League of Legends
INSERT INTO SERVICE (GAME, SERVICE_TYPE, DESCRIPTION, PRICE, IS_UNIQUE, IS_AVAILABLE, ID_PROVIDER) VALUES
('LEAGUE_OF_LEGENDS', 'COACHING', 'Coaching personnalisé pour améliorer votre gameplay LoL', 25.00, false, true, 1),
('LEAGUE_OF_LEGENDS', 'BOOST', 'Boost de rank Bronze à Gold', 50.00, false, true, 1),
('LEAGUE_OF_LEGENDS', 'BOOST', 'Boost de rank Gold à Platine', 80.00, false, true, 2),
('LEAGUE_OF_LEGENDS', 'OTHER', 'Accompagnement en partie classée', 15.00, false, true, 1),
('LEAGUE_OF_LEGENDS', 'COACHING', 'Coaching avancé pour Diamant+', 40.00, false, true, 3);

-- Services pour Valorant
INSERT INTO SERVICE (GAME, SERVICE_TYPE, DESCRIPTION, PRICE, IS_UNIQUE, IS_AVAILABLE, ID_PROVIDER) VALUES
('VALORANT', 'COACHING', 'Coaching stratégique Valorant', 30.00, false, true, 2),
('VALORANT', 'BOOST', 'Boost de rank Iron à Silver', 35.00, false, true, 2),
('VALORANT', 'BOOST', 'Boost de rank Silver à Gold', 55.00, false, true, 3),
('VALORANT', 'OTHER', 'Duo queue avec un expert', 20.00, false, true, 2),
('VALORANT', 'COACHING', 'Coaching aim et positionnement', 25.00, false, true, 1);


-- Services pour Rocket League
INSERT INTO SERVICE (GAME, SERVICE_TYPE, DESCRIPTION, PRICE, IS_UNIQUE, IS_AVAILABLE, ID_PROVIDER) VALUES
('ROCKET_LEAGUE', 'COACHING', 'Coaching mécanique et rotation', 24.00, false, true, 1),
('ROCKET_LEAGUE', 'BOOST', 'Boost de rank Gold à Diamond', 60.00, false, true, 2),
('ROCKET_LEAGUE', 'OTHER', 'Entraînement 2v2 avec coach', 16.00, false, true, 3);


