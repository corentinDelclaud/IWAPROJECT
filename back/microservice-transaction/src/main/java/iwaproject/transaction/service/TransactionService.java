package iwaproject.transaction.service;

import iwaproject.transaction.dto.CreateTransactionRequest;
import iwaproject.transaction.dto.UpdateStateRequest;
import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Conversation;
import iwaproject.transaction.model.Product;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.repository.ConversationRepository;
import iwaproject.transaction.repository.ProductRepository;
import iwaproject.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final ConversationRepository conversationRepository;
    private final ProductRepository productRepository;
    
    public TransactionService(TransactionRepository transactionRepository, 
                            ConversationRepository conversationRepository,
                            ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.conversationRepository = conversationRepository;
        this.productRepository = productRepository;
    }
    
    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction for userId={}, serviceId={}", request.userId(), request.serviceId());
        
        Product product = productRepository.findById(request.serviceId())
            .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        
        // Vérifier qu'aucune transaction active n'existe déjà
        transactionRepository.findByIdClientAndIdServiceAndTransactionStateNotIn(
            request.userId(),
            request.serviceId(),
            java.util.List.of(TransitionState.FINISHED_AND_PAYED, TransitionState.CANCELED)
        ).ifPresent(existingTransaction -> {
            throw new IllegalStateException(
                "Active transaction already exists (id=" + existingTransaction.getId() + ")"
            );
        });
        
        TransitionState initialState = request.directRequest() 
            ? TransitionState.REQUESTED 
            : TransitionState.EXCHANGING;
        
        Conversation conversation = new Conversation(request.userId(), product.getIdProvider());
        conversation = conversationRepository.save(conversation);
        
        Transaction transaction = new Transaction(
            initialState,
            request.serviceId(),
            request.userId(),
            product.getIdProvider(),
            conversation.getId()
        );
        
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: id={}, state={}", saved.getId(), initialState);
        return saved;
    }
    
    public Transaction getTransaction(Integer id) {
        log.debug("Fetching transaction with id={}", id);
        return transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }
    
    @Transactional
    public Transaction updateState(Integer transactionId, UpdateStateRequest request) {
        log.info("Updating transaction {} state to {} by user {}", transactionId, request.newState(), request.userId());
        
        Transaction transaction = getTransaction(transactionId);
        TransitionState currentState = transaction.getTransactionState();
        TransitionState newState = request.newState();
        Integer userId = request.userId();
        
        validateStateTransition(transaction, currentState, newState, userId);
        
        transaction.setTransactionState(newState);
        
        switch (newState) {
            case REQUEST_ACCEPTED -> transaction.setRequestValidationDate(LocalDateTime.now());
            case CLIENT_CONFIRMED, PROVIDER_CONFIRMED -> handleConfirmation(transaction, userId);
            case DOUBLE_CONFIRMED -> handleDoubleConfirmation(transaction);
            case FINISHED_AND_PAYED, CANCELED -> transaction.setFinishDate(LocalDateTime.now());
        }
        
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction {} state changed: {} -> {}", transactionId, currentState, saved.getTransactionState());
        return saved;
    }
    
    private void validateStateTransition(Transaction transaction, TransitionState current, 
                                        TransitionState target, Integer userId) {
        log.debug("Validating state transition: {} -> {} for user {}", current, target, userId);
        
        if (current == TransitionState.CANCELED || current == TransitionState.FINISHED_AND_PAYED) {
            throw new IllegalStateException("Transaction is already finalized");
        }
        
        switch (target) {
            case REQUESTED -> {
                if (current != TransitionState.EXCHANGING) 
                    throw new IllegalStateException("Can only move to REQUESTED from EXCHANGING");
                if (!userId.equals(transaction.getIdClient())) 
                    throw new IllegalStateException("Only client can request");
            }
            case REQUEST_ACCEPTED -> {
                if (current != TransitionState.REQUESTED) 
                    throw new IllegalStateException("Can only accept from REQUESTED");
                if (!userId.equals(transaction.getIdProvider())) 
                    throw new IllegalStateException("Only provider can accept");
            }
            case PREPAID -> {
                if (current != TransitionState.REQUEST_ACCEPTED) 
                    throw new IllegalStateException("Can only prepay from REQUEST_ACCEPTED");
                if (!userId.equals(999)) 
                    throw new IllegalStateException("Only test user 999 can trigger prepayment");
            }
            case CLIENT_CONFIRMED -> {
                if (current != TransitionState.PREPAID && current != TransitionState.PROVIDER_CONFIRMED)
                    throw new IllegalStateException("Can only confirm from PREPAID or after provider confirmation");
                if (!userId.equals(transaction.getIdClient()))
                    throw new IllegalStateException("Only client can confirm as client");
            }
            case PROVIDER_CONFIRMED -> {
                if (current != TransitionState.PREPAID && current != TransitionState.CLIENT_CONFIRMED)
                    throw new IllegalStateException("Can only confirm from PREPAID or after client confirmation");
                if (!userId.equals(transaction.getIdProvider()))
                    throw new IllegalStateException("Only provider can confirm as provider");
            }
            case DOUBLE_CONFIRMED -> {
                throw new IllegalStateException("DOUBLE_CONFIRMED is set automatically, cannot be set manually");
            }
            case FINISHED_AND_PAYED -> {
                throw new IllegalStateException("FINISHED_AND_PAYED is set automatically after DOUBLE_CONFIRMED");
            }
            case CANCELED -> {
                if (current == TransitionState.PREPAID ||
                    current == TransitionState.CLIENT_CONFIRMED || 
                    current == TransitionState.PROVIDER_CONFIRMED ||
                    current == TransitionState.DOUBLE_CONFIRMED)
                    throw new IllegalStateException("Cannot cancel after prepayment");
            }
        }
    }
    
    private void handleConfirmation(Transaction transaction, Integer userId) {
        boolean isClient = userId.equals(transaction.getIdClient());
        boolean isProvider = userId.equals(transaction.getIdProvider());
        
        if (!isClient && !isProvider) {
            throw new IllegalStateException("User not part of transaction");
        }
        
        TransitionState current = transaction.getTransactionState();
        
        // Vérifier qu'on vient bien de PREPAID ou d'une confirmation partielle
        if (current != TransitionState.PREPAID && 
            current != TransitionState.CLIENT_CONFIRMED && 
            current != TransitionState.PROVIDER_CONFIRMED) {
            throw new IllegalStateException("Can only confirm from PREPAID state");
        }
        
        if (current == TransitionState.CLIENT_CONFIRMED && isProvider) {
            transaction.setTransactionState(TransitionState.DOUBLE_CONFIRMED);
        } else if (current == TransitionState.PROVIDER_CONFIRMED && isClient) {
            transaction.setTransactionState(TransitionState.DOUBLE_CONFIRMED);
        }
        // Si on est en PREPAID, le state reste CLIENT_CONFIRMED ou PROVIDER_CONFIRMED (déjà set avant l'appel)
    }
    
    private void handleDoubleConfirmation(Transaction transaction) {
        log.info("Both parties confirmed transaction {}, processing payment...", transaction.getId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        transaction.setTransactionState(TransitionState.FINISHED_AND_PAYED);
        transaction.setFinishDate(LocalDateTime.now());
        log.info("Transaction {} finalized and paid", transaction.getId());
    }
}