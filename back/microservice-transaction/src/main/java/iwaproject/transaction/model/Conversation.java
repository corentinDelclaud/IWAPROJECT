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
    private Integer idClient;
    
    @Column(name = "id_provider", nullable = false)
    private Integer idProvider;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    protected Conversation() {}
    
    public Conversation(Integer idClient, Integer idProvider) {
        this.idClient = idClient;
        this.idProvider = idProvider;
        this.createdAt = LocalDateTime.now();
    }
    
    public Integer getId() { return id; }
    public Integer getIdClient() { return idClient; }
    public Integer getIdProvider() { return idProvider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}