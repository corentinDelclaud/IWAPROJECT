package com.iwaproject.product.dto.stripe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeProductResponse {
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("productDescription")
    private String productDescription;
    
    @JsonProperty("productPrice")
    private Integer productPrice;
    
    @JsonProperty("priceId")
    private String priceId;
    
    // Note: Le productId n'est pas retourné par l'endpoint actuel
    // mais on peut l'ajouter dans stripe-service si nécessaire
    private String productId;
}
