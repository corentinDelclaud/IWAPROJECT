package iwaproject.transaction.dto;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse(
    Integer id,
    TransitionState state,
    Integer serviceId,
    String idClient,
    String idProvider,
    LocalDateTime creationDate,
    LocalDateTime requestValidationDate,
    LocalDateTime finishDate
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getTransactionState(),
            transaction.getIdService(),
            transaction.getIdClient(),
            transaction.getIdProvider(),
            transaction.getCreationDate(),
            transaction.getRequestValidationDate(),
            transaction.getFinishDate()
        );
    }
}