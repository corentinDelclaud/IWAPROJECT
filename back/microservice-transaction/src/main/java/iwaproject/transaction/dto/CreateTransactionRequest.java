package iwaproject.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(
    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    Integer userId,
    
    @NotNull(message = "serviceId is required")
    @Positive(message = "serviceId must be positive")
    Integer serviceId,
    
    @NotNull(message = "directRequest is required")
    Boolean directRequest
) {}