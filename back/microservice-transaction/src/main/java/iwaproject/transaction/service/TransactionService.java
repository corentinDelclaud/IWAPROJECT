package iwaproject.transaction.service;

import iwaproject.transaction.dto.CreateTransactionRequest;
import iwaproject.transaction.dto.ProductDTO;
import iwaproject.transaction.dto.UpdateStateRequest;
import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final TransactionSseService sseService;
    
    public TransactionService(TransactionRepository transactionRepository,
                             CatalogServiceClient catalogServiceClient,
                             TransactionSseService sseService) {
        this.transactionRepository = transactionRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.sseService = sseService;
    }
    
    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request, String userIdFromHeader) {
        String userId = validateUserId(userIdFromHeader);
        
        log.info("Creating transaction for userId={} (from JWT), serviceId={}", 
                userId, request.serviceId());
        
        // Récupérer le produit depuis le service-catalog via gateway
        ProductDTO product = catalogServiceClient.getProductById(request.serviceId());
        
        // Vérifier la disponibilité
        if (product.isAvailable() == null || !product.isAvailable()) {
            log.warn("#debuglog Product {} is not available", request.serviceId());
            throw new IllegalStateException("Product is not available for purchase");
        }
        
        log.info("#debuglog Product {} is available, idProvider={}", 
                request.serviceId(), product.idProvider());
        
        // Vérifier qu'aucune transaction active n'existe déjà
        transactionRepository.findByIdClientAndIdServiceAndTransactionStateNotIn(
            userId,
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
        
        // Hook pour création de conversation (non implémenté)
        log.info("#debuglog Conversation creation hook triggered for client={} and provider={} - NOT IMPLEMENTED YET",
                userId, product.idProvider());
        
        // Créer transaction avec idProvider du produit (String)
        Transaction transaction = new Transaction(
            initialState,
            request.serviceId(),
            userId,
            product.idProvider()
        );
        
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: id={}, state={}, provider={}", 
                saved.getId(), initialState, product.idProvider());
        
        // Notifier via SSE
        sseService.notifyTransactionUpdate(saved);
        
        return saved;
    }
    
    public Transaction getTransaction(Integer id) {
        log.debug("Fetching transaction with id={}", id);
        return transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }
    
    /**
     * Récupère toutes les transactions d'un utilisateur (en tant que client ou provider)
     */
    public List<Transaction> getTransactionsByUser(String userId) {
        log.debug("Fetching transactions for user={}", userId);
        return transactionRepository.findByIdClientOrIdProviderOrderByCreationDateDesc(userId, userId);
    }
    
    @Transactional
    public Transaction updateState(Integer transactionId, UpdateStateRequest request, String userIdFromHeader) {
        String userId = validateUserId(userIdFromHeader);
        
        log.info("Updating transaction {} state to {} by user {} (from JWT)", 
                transactionId, request.newState(), userId);
        
        Transaction transaction = getTransaction(transactionId);
        TransitionState currentState = transaction.getTransactionState();
        TransitionState newState = request.newState();
        
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
        
        // Notifier via SSE
        sseService.notifyTransactionUpdate(saved);
        
        return saved;
    }
    
    private String validateUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            log.error("#debuglog Missing X-User-Id header from JWT");
            throw new IllegalArgumentException("User ID header is missing");
        }
        
        log.debug("#debuglog Validated user ID from JWT header: {}", userIdHeader);
        return userIdHeader;
    }
    
    private void validateStateTransition(Transaction transaction, TransitionState current, 
                                        TransitionState target, String userId) {
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
                log.warn("#debuglog PREPAID state set - payment validation should be handled by external service");
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
    
    private void handleConfirmation(Transaction transaction, String userId) {
        boolean isClient = userId.equals(transaction.getIdClient());
        boolean isProvider = userId.equals(transaction.getIdProvider());
        
        if (!isClient && !isProvider) {
            throw new IllegalStateException("User not part of transaction");
        }
        
        TransitionState current = transaction.getTransactionState();
        
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