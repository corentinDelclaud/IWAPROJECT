package com.iwaproject.catalog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Integer idService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Float price;

    @Column(name = "is_unique", nullable = false)
    private Boolean unique = false;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "id_provider", nullable = false)
    private Integer idProvider;
}
