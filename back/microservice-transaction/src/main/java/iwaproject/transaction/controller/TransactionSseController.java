package iwaproject.transaction.controller;

import iwaproject.transaction.service.TransactionSseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/transaction/sse")
@CrossOrigin(origins = "*")
public class TransactionSseController {

    private static final Logger log = LoggerFactory.getLogger(TransactionSseController.class);
    private final TransactionSseService sseService;

    public TransactionSseController(TransactionSseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Endpoint SSE pour s'abonner aux mises à jour d'une transaction spécifique
     */
    @GetMapping(value = "/{transactionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToTransaction(
            @PathVariable Integer transactionId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("[SSE] Subscription request for transaction {} by user {}", transactionId, userId);
        return sseService.subscribeToTransaction(transactionId, userId);
    }

    /**
     * Endpoint SSE pour s'abonner à toutes les transactions d'un utilisateur
     */
    @GetMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToUserTransactions(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("[SSE] Subscription request for all transactions of user {}", userId);
        return sseService.subscribeToUserTransactions(userId);
    }
}