package com.iwaproject.product.dto;

import com.iwaproject.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer idService;
    private String game;
    private String serviceType;
    private String description;
    private Float price;
    private Boolean unique;
    private Boolean isAvailable;
    private String idProvider;
    
    // Stripe integration fields
    private String stripeProductId;
    private String stripePriceId;
    private String stripeAccountId;

    // Constructeur à partir de l'entité
    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
            product.getIdService(),
            product.getGame().getDisplayName(),
            product.getServiceType().getDisplayName(),
            product.getDescription(),
            product.getPrice(),
            product.getUnique(),
            product.getIsAvailable(),
            product.getIdProvider(),
            product.getStripeProductId(),
            product.getStripePriceId(),
            product.getStripeAccountId()
        );
    }
}

