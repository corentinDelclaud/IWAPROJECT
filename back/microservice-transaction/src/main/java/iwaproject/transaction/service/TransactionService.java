package iwaproject.transaction.service;

import iwaproject.transaction.client.CatalogClient;
import iwaproject.transaction.dto.CreateTransactionRequest;
import iwaproject.transaction.dto.UpdateStateRequest;
import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Conversation;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.repository.ConversationRepository;
import iwaproject.transaction.repository.TransactionRepository;
import iwaproject.transaction.util.JwtUtil;
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
    private final CatalogClient catalogClient;
    private final JwtUtil jwtUtil;
    
    public TransactionService(TransactionRepository transactionRepository, 
                            ConversationRepository conversationRepository,
                            CatalogClient catalogClient,
                            JwtUtil jwtUtil) {
        this.transactionRepository = transactionRepository;
        this.conversationRepository = conversationRepository;
        this.catalogClient = catalogClient;
        this.jwtUtil = jwtUtil;
    }
    
    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        String userId = jwtUtil.getUserIdFromToken();
        log.info("Creating transaction for userId={}, serviceId={}", userId, request.serviceId());
        
        // #debuglog Récupération du produit depuis le catalog
        CatalogClient.ProductDTO product = catalogClient.getProduct(request.serviceId());
        
        if (product == null) {
            log.error("#debuglog Product not found: serviceId={}", request.serviceId());
            throw new IllegalArgumentException("Service not found");
        }
        
        if (!product.isAvailable()) {
            log.error("#debuglog Product not available: serviceId={}", request.serviceId());
            throw new IllegalStateException("Service is not available");
        }
        
        log.info("#debuglog Product validated: id={}, provider={}", product.idService(), product.idProvider());
        
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
        
        Conversation conversation = new Conversation(userId, product.idProvider());
        conversation = conversationRepository.save(conversation);
        
        Transaction transaction = new Transaction(
            initialState,
            request.serviceId(),
            userId,
            product.idProvider(),
            conversation.getId()
        );
        
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: id={}, state={}", saved.getId(), initialState);
        return saved;
    }
    
    public Transaction getTransaction(Integer id) {
        String userId = jwtUtil.getUserIdFromToken();
        log.debug("#debuglog Fetching transaction with id={} for user={}", id, userId);
        
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        // Vérifier que l'utilisateur a le droit d'accéder à cette transaction
        if (!transaction.getIdClient().equals(userId) && !transaction.getIdProvider().equals(userId)) {
            log.error("#debuglog User {} not authorized to access transaction {}", userId, id);
            throw new IllegalStateException("Not authorized to access this transaction");
        }
        
        return transaction;
    }
    
    @Transactional
    public Transaction updateState(Integer transactionId, UpdateStateRequest request) {
        String userId = jwtUtil.getUserIdFromToken();
        log.info("#debuglog Updating transaction {} state to {} by user {}", transactionId, request.newState(), userId);
        
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
        return saved;
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