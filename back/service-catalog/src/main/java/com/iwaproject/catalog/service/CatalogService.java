package com.iwaproject.catalog.service;

import com.iwaproject.catalog.dto.CreateServiceRequest;
import com.iwaproject.catalog.dto.ServiceDTO;
import com.iwaproject.catalog.model.Game;
import com.iwaproject.catalog.model.ServiceEntity;
import com.iwaproject.catalog.model.ServiceType;
import com.iwaproject.catalog.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceRepository serviceRepository;

    // Récupérer tous les services
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer tous les services disponibles
    public List<ServiceDTO> getAvailableServices() {
        return serviceRepository.findByIsAvailableTrue().stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer un service par ID
    public Optional<ServiceDTO> getServiceById(Integer id) {
        return serviceRepository.findById(id)
                .map(ServiceDTO::fromEntity);
    }

    // Récupérer les services par jeu
    public List<ServiceDTO> getServicesByGame(Game game) {
        return serviceRepository.findByGame(game).stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services par type
    public List<ServiceDTO> getServicesByType(ServiceType serviceType) {
        return serviceRepository.findByServiceType(serviceType).stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services par jeu et type
    public List<ServiceDTO> getServicesByGameAndType(Game game, ServiceType serviceType) {
        return serviceRepository.findByGameAndServiceType(game, serviceType).stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services d'un provider
    public List<ServiceDTO> getServicesByProvider(Integer idProvider) {
        return serviceRepository.findByIdProvider(idProvider).stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Créer un nouveau service
    @Transactional
    public ServiceDTO createService(CreateServiceRequest request) {
        ServiceEntity service = new ServiceEntity();
        service.setGame(request.getGame());
        service.setServiceType(request.getServiceType());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setUnique(request.getUnique());
        service.setIsAvailable(request.getIsAvailable());
        service.setIdProvider(request.getIdProvider());

        ServiceEntity savedService = serviceRepository.save(service);
        return ServiceDTO.fromEntity(savedService);
    }

    // Mettre à jour un service
    @Transactional
    public Optional<ServiceDTO> updateService(Integer id, CreateServiceRequest request) {
        return serviceRepository.findById(id).map(service -> {
            service.setGame(request.getGame());
            service.setServiceType(request.getServiceType());
            service.setDescription(request.getDescription());
            service.setPrice(request.getPrice());
            service.setUnique(request.getUnique());
            service.setIsAvailable(request.getIsAvailable());
            service.setIdProvider(request.getIdProvider());

            ServiceEntity updatedService = serviceRepository.save(service);
            return ServiceDTO.fromEntity(updatedService);
        });
    }

    // Supprimer un service
    @Transactional
    public boolean deleteService(Integer id) {
        if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Changer la disponibilité d'un service
    @Transactional
    public Optional<ServiceDTO> toggleAvailability(Integer id) {
        return serviceRepository.findById(id).map(service -> {
            service.setIsAvailable(!service.getIsAvailable());
            ServiceEntity updatedService = serviceRepository.save(service);
            return ServiceDTO.fromEntity(updatedService);
        });
    }
}
