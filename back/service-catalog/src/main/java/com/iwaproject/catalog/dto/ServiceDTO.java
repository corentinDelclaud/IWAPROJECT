package com.iwaproject.catalog.dto;

import com.iwaproject.catalog.model.ServiceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Integer idService;
    private String game;
    private String serviceType;
    private String description;
    private Float price;
    private Boolean unique;
    private Boolean isAvailable;
    private Integer idProvider;

    // Constructeur à partir de l'entité
    public static ServiceDTO fromEntity(ServiceEntity service) {
        return new ServiceDTO(
            service.getIdService(),
            service.getGame().getDisplayName(),
            service.getServiceType().getDisplayName(),
            service.getDescription(),
            service.getPrice(),
            service.getUnique(),
            service.getIsAvailable(),
            service.getIdProvider()
        );
    }
}
