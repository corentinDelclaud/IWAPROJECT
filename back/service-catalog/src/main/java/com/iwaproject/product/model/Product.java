package com.iwaproject.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column("id_service")
    private Integer idService;

    @Column("game")
    private Game game;

    @Column("service_type")
    private ServiceType serviceType;

    @Column("description")
    private String description;

    @Column("price")
    private Float price;

    @Column("is_unique")
    private Boolean unique = false;

    @Column("is_available")
    private Boolean isAvailable = true;

    @Column("id_provider")
    private Integer idProvider;
}
