package iwaproject.transaction.dto;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;

public record TransactionResponse(
    Integer id,
    TransitionState state,
    Integer serviceId,
    Integer conversationId,
    Integer idClient,
    Integer idProvider
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getTransactionState(),
            transaction.getIdService(),
            transaction.getIdConversation(),
            transaction.getIdClient(),
            transaction.getIdProvider()
        );
    }
}