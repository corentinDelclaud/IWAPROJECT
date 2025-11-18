package com.iwaproject.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusResponse {
    private String id;
    private Boolean payoutsEnabled;
    private Boolean chargesEnabled;
    private Boolean detailsSubmitted;
}
