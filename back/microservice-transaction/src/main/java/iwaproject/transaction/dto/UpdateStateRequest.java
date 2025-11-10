package iwaproject.transaction.dto;

import iwaproject.transaction.enums.TransitionState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateStateRequest(
    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    Integer userId,

    @NotNull(message = "newState is required")
    TransitionState newState
) {}