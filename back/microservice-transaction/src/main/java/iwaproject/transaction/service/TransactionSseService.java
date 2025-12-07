package iwaproject.transaction.service;

import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionSseService {

    private static final Logger log = LoggerFactory.getLogger(TransactionSseService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    
    // Map: transactionId -> list of SSE emitters
    private final Map<Integer, List<SseEmitter>> transactionEmitters = new ConcurrentHashMap<>();
    
    // Map: userId -> list of SSE emitters
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    
    private final TransactionRepository transactionRepository;
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public TransactionSseService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        // Heartbeat toutes les 30 secondes pour garder les connexions ouvertes
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * S'abonner aux mises à jour d'une transaction spécifique
     */
    public SseEmitter subscribeToTransaction(Integer transactionId, String userId) {
        // Vérifier que l'utilisateur a accès à cette transaction
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            log.warn("Transaction {} not found for SSE subscription", transactionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalArgumentException("Transaction not found"));
            return emitter;
        }
        
        if (userId != null && !userId.equals(transaction.getIdClient()) && !userId.equals(transaction.getIdProvider())) {
            log.warn("User {} not authorized to subscribe to transaction {}", userId, transactionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalArgumentException("Not authorized"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        List<SseEmitter> emitters = transactionEmitters.computeIfAbsent(transactionId, 
            k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        log.info("[SSE] User {} subscribed to transaction {}", userId, transactionId);

        // Envoyer l'état actuel immédiatement
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(transactionToJson(transaction)));
        } catch (IOException e) {
            log.error("[SSE] Error sending initial state", e);
        }

        // Cleanup on completion/timeout/error
        emitter.onCompletion(() -> removeTransactionEmitter(transactionId, emitter));
        emitter.onTimeout(() -> removeTransactionEmitter(transactionId, emitter));
        emitter.onError(e -> removeTransactionEmitter(transactionId, emitter));

        return emitter;
    }

    /**
     * S'abonner à toutes les transactions d'un utilisateur
     */
    public SseEmitter subscribeToUserTransactions(String userId) {
        if (userId == null || userId.isBlank()) {
            log.warn("Cannot subscribe without userId");
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalArgumentException("User ID required"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        List<SseEmitter> emitters = userEmitters.computeIfAbsent(userId, 
            k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        log.info("[SSE] User {} subscribed to all their transactions", userId);

        // Cleanup
        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError(e -> removeUserEmitter(userId, emitter));

        return emitter;
    }

    /**
     * Notifier tous les abonnés d'une mise à jour de transaction
     */
    public void notifyTransactionUpdate(Transaction transaction) {
        log.info("[SSE] Broadcasting update for transaction {}, state: {}", 
            transaction.getId(), transaction.getTransactionState());

        String jsonData = transactionToJson(transaction);

        // Notifier les abonnés de cette transaction spécifique
        List<SseEmitter> txEmitters = transactionEmitters.get(transaction.getId());
        if (txEmitters != null) {
            sendToEmitters(txEmitters, jsonData, transaction.getId());
        }

        // Notifier le client
        if (transaction.getIdClient() != null) {
            List<SseEmitter> clientEmitters = userEmitters.get(transaction.getIdClient());
            if (clientEmitters != null) {
                sendToEmitters(clientEmitters, jsonData, null);
            }
        }

        // Notifier le provider
        if (transaction.getIdProvider() != null) {
            List<SseEmitter> providerEmitters = userEmitters.get(transaction.getIdProvider());
            if (providerEmitters != null) {
                sendToEmitters(providerEmitters, jsonData, null);
            }
        }
    }

    private void sendToEmitters(List<SseEmitter> emitters, String data, Integer transactionId) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(data));
            } catch (IOException e) {
                log.warn("[SSE] Failed to send to emitter, marking for removal");
                deadEmitters.add(emitter);
            }
        }
        
        emitters.removeAll(deadEmitters);
    }

    private void sendHeartbeats() {
        String heartbeat = "{\"type\":\"heartbeat\",\"id\":-1}";
        
        transactionEmitters.values().forEach(emitters -> 
            sendToEmitters(emitters, heartbeat, null));
        
        userEmitters.values().forEach(emitters -> 
            sendToEmitters(emitters, heartbeat, null));
    }

    private void removeTransactionEmitter(Integer transactionId, SseEmitter emitter) {
        List<SseEmitter> emitters = transactionEmitters.get(transactionId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                transactionEmitters.remove(transactionId);
            }
        }
        log.debug("[SSE] Emitter removed for transaction {}", transactionId);
    }

    private void removeUserEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
        log.debug("[SSE] Emitter removed for user {}", userId);
    }

    private String transactionToJson(Transaction t) {
        return String.format(
            "{\"id\":%d,\"state\":\"%s\",\"serviceId\":%d,\"idClient\":\"%s\",\"idProvider\":\"%s\",\"creationDate\":\"%s\"%s%s}",
            t.getId(),
            t.getTransactionState().name(),
            t.getIdService(),
            t.getIdClient() != null ? t.getIdClient() : "",
            t.getIdProvider() != null ? t.getIdProvider() : "",
            t.getCreationDate() != null ? t.getCreationDate().toString() : "",
            t.getRequestValidationDate() != null ? ",\"requestValidationDate\":\"" + t.getRequestValidationDate() + "\"" : "",
            t.getFinishDate() != null ? ",\"finishDate\":\"" + t.getFinishDate() + "\"" : ""
        );
    }
}