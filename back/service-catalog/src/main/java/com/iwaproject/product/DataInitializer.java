package com.iwaproject.product;

import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.Product;
import com.iwaproject.product.model.ServiceType;
import com.iwaproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Données de test pour le développement
        Product product1 = new Product();
        product1.setGame(Game.VALORANT);
        product1.setServiceType(ServiceType.COACHING);
        product1.setDescription("Coaching personnalisé Valorant - Analyse de gameplay, stratégies avancées pour atteindre Radiant");
        product1.setPrice(30.0f);
        product1.setUnique(false);
        product1.setIsAvailable(true);
        product1.setIdProvider(1);
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setGame(Game.LEAGUE_OF_LEGENDS);
        product2.setServiceType(ServiceType.BOOSTING);
        product2.setDescription("Boost de rang LoL (Fer à Diamant) - Service rapide et sécurisé par joueurs Master/Grandmaster");
        product2.setPrice(20.0f);
        product2.setUnique(false);
        product2.setIsAvailable(true);
        product2.setIdProvider(2);
        productRepository.save(product2);

        Product product3 = new Product();
        product3.setGame(Game.VALORANT);
        product3.setServiceType(ServiceType.BOOSTING);
        product3.setDescription("Boost rapide Valorant - De Bronze à Immortal en moins d'une semaine");
        product3.setPrice(45.0f);
        product3.setUnique(false);
        product3.setIsAvailable(true);
        product3.setIdProvider(1);
        productRepository.save(product3);

        Product product4 = new Product();
        product4.setGame(Game.LEAGUE_OF_LEGENDS);
        product4.setServiceType(ServiceType.ACCOUNT_RESALING);
        product4.setDescription("Compte LoL niveau 30 avec 50 champions débloqués - Non classé, prêt pour le ranked");
        product4.setPrice(25.0f);
        product4.setUnique(true);
        product4.setIsAvailable(true);
        product4.setIdProvider(3);
        productRepository.save(product4);

        Product product5 = new Product();
        product5.setGame(Game.ROCKET_LEAGUE);
        product5.setServiceType(ServiceType.COACHING);
        product5.setDescription("Coaching Rocket League - Mécanique avancée, positionnement, rotation en 2v2 et 3v3");
        product5.setPrice(35.0f);
        product5.setUnique(false);
        product5.setIsAvailable(true);
        product5.setIdProvider(4);
        productRepository.save(product5);

        System.out.println("✅ Données initiales chargées : " + productRepository.count() + " produits créés");
    }
}

