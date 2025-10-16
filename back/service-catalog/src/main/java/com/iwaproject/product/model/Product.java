package com.iwaproject.product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("SERVICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column("ID_SERVICE")
    private Integer idService;

    @Column("GAME")
    private Game game;

    @Column("SERVICE_TYPE")
    private ServiceType serviceType;

    @Column("DESCRIPTION")
    private String description;

    @Column("PRICE")
    private Float price;

    @Column("IS_UNIQUE")
    private Boolean unique = false;

    @Column("IS_AVAILABLE")
    private Boolean isAvailable = true;

    @Column("ID_PROVIDER")
    private Integer idProvider;
}
