package iwaproject.transaction.repository;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    Optional<Transaction> findByIdClientAndIdServiceAndTransactionStateNotIn(
        String idClient,
        Integer idService,
        List<TransitionState> excludedStates
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.idClient = :userId OR t.idProvider = :userId ORDER BY t.creationDate DESC")
    List<Transaction> findByIdClientOrIdProviderOrderByCreationDateDesc(@Param("userId") String idClient, @Param("userId") String idProvider);
    
    List<Transaction> findByIdClient(String idClient);
    
    List<Transaction> findByIdProvider(String idProvider);
}