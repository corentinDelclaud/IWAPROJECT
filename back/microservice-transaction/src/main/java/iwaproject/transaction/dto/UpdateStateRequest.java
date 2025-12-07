package iwaproject.transaction.dto;

import iwaproject.transaction.enums.TransitionState;
import jakarta.validation.constraints.NotNull;

public record UpdateStateRequest(
    @NotNull(message = "newState is required")
    TransitionState newState
) {}