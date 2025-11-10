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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@Tag(name = "Transaction", description = "Gestion des transactions")
@Validated
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle transaction")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
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
            @Valid @RequestBody UpdateStateRequest request) {
        Transaction transaction = transactionService.updateState(id, request);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
}