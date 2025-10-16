package com.iwaproject.catalog;

import com.iwaproject.catalog.model.Game;
import com.iwaproject.catalog.model.ServiceEntity;
import com.iwaproject.catalog.model.ServiceType;
import com.iwaproject.catalog.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ServiceRepository serviceRepository;

    @Override
    public void run(String... args) {
        // Données de test pour le développement
        ServiceEntity service1 = new ServiceEntity();
        service1.setGame(Game.VALORANT);
        service1.setServiceType(ServiceType.COACHING);
        service1.setDescription("Coaching personnalisé Valorant - Analyse de gameplay, stratégies avancées pour atteindre Radiant");
        service1.setPrice(30.0f);
        service1.setUnique(false);
        service1.setIsAvailable(true);
        service1.setIdProvider(1);
        serviceRepository.save(service1);

        ServiceEntity service2 = new ServiceEntity();
        service2.setGame(Game.LEAGUE_OF_LEGENDS);
        service2.setServiceType(ServiceType.BOOSTING);
        service2.setDescription("Boost de rang LoL (Fer à Diamant) - Service rapide et sécurisé par joueurs Master/Grandmaster");
        service2.setPrice(20.0f);
        service2.setUnique(false);
        service2.setIsAvailable(true);
        service2.setIdProvider(2);
        serviceRepository.save(service2);

        ServiceEntity service3 = new ServiceEntity();
        service3.setGame(Game.VALORANT);
        service3.setServiceType(ServiceType.BOOSTING);
        service3.setDescription("Boost rapide Valorant - De Bronze à Immortal en moins d'une semaine");
        service3.setPrice(45.0f);
        service3.setUnique(false);
        service3.setIsAvailable(true);
        service3.setIdProvider(1);
        serviceRepository.save(service3);

        ServiceEntity service4 = new ServiceEntity();
        service4.setGame(Game.LEAGUE_OF_LEGENDS);
        service4.setServiceType(ServiceType.ACCOUNT_RESALING);
        service4.setDescription("Compte LoL niveau 30 avec 50 champions débloqués - Non classé, prêt pour le ranked");
        service4.setPrice(25.0f);
        service4.setUnique(true);
        service4.setIsAvailable(true);
        service4.setIdProvider(3);
        serviceRepository.save(service4);

        ServiceEntity service5 = new ServiceEntity();
        service5.setGame(Game.ROCKET_LEAGUE);
        service5.setServiceType(ServiceType.COACHING);
        service5.setDescription("Coaching Rocket League - Mécanique avancée, positionnement, rotation en 2v2 et 3v3");
        service5.setPrice(35.0f);
        service5.setUnique(false);
        service5.setIsAvailable(true);
        service5.setIdProvider(4);
        serviceRepository.save(service5);

        System.out.println("✅ Données initiales chargées : " + serviceRepository.count() + " services créés");
    }
}
