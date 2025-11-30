package iwaproject.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iwaproject.transaction.dto.CreateTransactionRequest;
import iwaproject.transaction.dto.TransactionResponse;
import iwaproject.transaction.dto.UpdateStateRequest;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@Tag(name = "Transaction", description = "Gestion des transactions")
@Validated
public class TransactionController {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        log.info("#debuglog Received create transaction request with X-User-Id: {}", userId);
        Transaction transaction = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(TransactionResponse.fromEntity(transaction));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une transaction par ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable @Positive(message = "id must be positive") Integer id) {
        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
    
    @PutMapping("/{id}/state")
    @Operation(summary = "Mettre à jour l'état d'une transaction")
    public ResponseEntity<TransactionResponse> updateState(
            @PathVariable @Positive(message = "id must be positive") Integer id,
            @Valid @RequestBody UpdateStateRequest request,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        log.info("#debuglog Received update state request with X-User-Id: {}", userId);
        Transaction transaction = transactionService.updateState(id, request, userId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
}