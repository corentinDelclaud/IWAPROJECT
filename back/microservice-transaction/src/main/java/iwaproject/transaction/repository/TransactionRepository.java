package iwaproject.transaction.repository;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    Optional<Transaction> findByIdClientAndIdServiceAndTransactionStateNotIn(
        String idClient,  // String au lieu de Integer
        Integer idService, 
        List<TransitionState> states
    );
}