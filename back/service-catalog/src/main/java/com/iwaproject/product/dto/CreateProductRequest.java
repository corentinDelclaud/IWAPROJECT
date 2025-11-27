package com.iwaproject.product.dto;

import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotNull(message = "Le jeu est obligatoire")
    private Game game;

    @NotNull(message = "Le type de service est obligatoire")
    private ServiceType serviceType;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Float price;

    private Boolean unique = false;

    private Boolean isAvailable = true;

    @NotNull(message = "L'IUUD du provider est obligatoire")
    private String idProvider;
}

