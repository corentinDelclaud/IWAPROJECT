package iwaproject.transaction.repository;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByIdClientAndIdServiceAndTransactionStateNotIn(
        Integer idClient, 
        Integer idService, 
        java.util.List<TransitionState> states
    );
}