package com.iwaproject.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {
    private String productName;
    private String productDescription;
    private Long productPrice;
    private String priceId;
}
