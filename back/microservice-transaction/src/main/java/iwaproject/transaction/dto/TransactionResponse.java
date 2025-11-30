package iwaproject.transaction.dto;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;

public record TransactionResponse(
    Integer id,
    TransitionState state,
    Integer serviceId,
    String idClient,     // String au lieu de Integer
    String idProvider    // String au lieu de Integer
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getTransactionState(),
            transaction.getIdService(),
            transaction.getIdClient(),
            transaction.getIdProvider()
        );
    }
}