package iwaproject.transaction.model;

import iwaproject.transaction.enums.TransitionState;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_state", nullable = false)
    private TransitionState transactionState;
    
    @Column(name = "request_validation_date")
    private LocalDateTime requestValidationDate;
    
    @Column(name = "finish_date")
    private LocalDateTime finishDate;
    
    @Column(name = "id_conversation", nullable = false)
    private Integer idConversation;
    
    @Column(name = "id_service", nullable = false)
    private Integer idService;
    
    @Column(name = "id_client", nullable = false)
    private String idClient;
    
    @Column(name = "id_provider", nullable = false)
    private String idProvider;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    protected Transaction() {}
    
    public Transaction(TransitionState transactionState, Integer idService, 
                      String idClient, String idProvider, Integer idConversation) {
        this.transactionState = transactionState;
        this.idService = idService;
        this.idClient = idClient;
        this.idProvider = idProvider;
        this.idConversation = idConversation;
        this.createdAt = LocalDateTime.now();
    }
    
    public Integer getId() { return id; }
    public TransitionState getTransactionState() { return transactionState; }
    public LocalDateTime getRequestValidationDate() { return requestValidationDate; }
    public LocalDateTime getFinishDate() { return finishDate; }
    public Integer getIdConversation() { return idConversation; }
    public Integer getIdService() { return idService; }
    public String getIdClient() { return idClient; }
    public String getIdProvider() { return idProvider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setTransactionState(TransitionState state) { this.transactionState = state; }
    public void setRequestValidationDate(LocalDateTime date) { this.requestValidationDate = date; }
    public void setFinishDate(LocalDateTime date) { this.finishDate = date; }
}