package com.iwaproject.catalog.repository;

import com.iwaproject.catalog.model.Game;
import com.iwaproject.catalog.model.ServiceEntity;
import com.iwaproject.catalog.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {

    // Trouver tous les services disponibles
    List<ServiceEntity> findByIsAvailableTrue();

    // Trouver par jeu
    List<ServiceEntity> findByGame(Game game);

    // Trouver par type de service
    List<ServiceEntity> findByServiceType(ServiceType serviceType);

    // Trouver par jeu et type
    List<ServiceEntity> findByGameAndServiceType(Game game, ServiceType serviceType);

    // Trouver par provider
    List<ServiceEntity> findByIdProvider(Integer idProvider);

    // Trouver les services disponibles par jeu
    List<ServiceEntity> findByGameAndIsAvailableTrue(Game game);

    // Trouver les services disponibles par type
    List<ServiceEntity> findByServiceTypeAndIsAvailableTrue(ServiceType serviceType);
}
