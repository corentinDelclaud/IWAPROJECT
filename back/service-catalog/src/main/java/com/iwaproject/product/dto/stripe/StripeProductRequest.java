package com.iwaproject.product.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeProductRequest {
    private String productName;
    private String productDescription;
    private Integer productPrice; // en centimes
    private String accountId;
}
