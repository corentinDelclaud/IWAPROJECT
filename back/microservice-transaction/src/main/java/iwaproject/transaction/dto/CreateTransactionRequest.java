package iwaproject.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(
    @NotNull(message = "serviceId is required")
    @Positive(message = "serviceId must be positive")
    Integer serviceId,
    
    @NotNull(message = "directRequest is required")
    Boolean directRequest
) {}