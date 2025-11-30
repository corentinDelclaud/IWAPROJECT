package iwaproject.transaction.controller;

import iwaproject.transaction.dto.CreateTransactionRequest;
import iwaproject.transaction.dto.TransactionResponse;
import iwaproject.transaction.dto.UpdateStateRequest;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create a new transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("POST /transaction - userId: {}, serviceId: {}", userId, request.serviceId());

        Transaction transaction = transactionService.createTransaction(request, userId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    /**
     * Get a specific transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("GET /transaction/{} - userId: {}", id, userId);

        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    /**
     * Get all transactions for the current user
     */
    @GetMapping("/my")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("GET /transaction/my - userId: {}", userId);

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Update transaction state (PUT)
     */
    @PutMapping("/{id}/state")
    public ResponseEntity<TransactionResponse> updateTransactionStatePut(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("PUT /transaction/{}/state - userId: {}, newState: {}", id, userId, request.newState());

        Transaction transaction = transactionService.updateState(id, request, userId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    /**
     * Update transaction state (PATCH)
     */
    @PatchMapping("/{id}/state")
    public ResponseEntity<TransactionResponse> updateTransactionStatePatch(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("PATCH /transaction/{}/state - userId: {}, newState: {}", id, userId, request.newState());

        Transaction transaction = transactionService.updateState(id, request, userId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
}