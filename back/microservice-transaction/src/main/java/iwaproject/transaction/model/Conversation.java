package iwaproject.transaction.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "id_client", nullable = false)
    private String idClient;  // Changed from Integer to String
    
    @Column(name = "id_provider", nullable = false)
    private String idProvider;  // Changed from Integer to String
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    protected Conversation() {}
    
    public Conversation(String idClient, String idProvider) {
        this.idClient = idClient;
        this.idProvider = idProvider;
    }
    
    public Integer getId() { return id; }
    public String getIdClient() { return idClient; }
    public String getIdProvider() { return idProvider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}