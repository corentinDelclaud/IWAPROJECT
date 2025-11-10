package iwaproject.transaction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Integer idService;
    
    @Column(nullable = false, length = 50)
    private String game;
    
    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "is_unique")
    private Boolean isUnique = false;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "id_provider", nullable = false)
    private Integer idProvider;
    
    protected Product() {}
    
    public Product(String game, String serviceType, String description, BigDecimal price, Boolean isUnique, Integer idProvider) {
        this.game = game;
        this.serviceType = serviceType;
        this.description = description;
        this.price = price;
        this.isUnique = isUnique;
        this.idProvider = idProvider;
    }
    
    public Integer getIdService() { return idService; }
    public String getGame() { return game; }
    public String getServiceType() { return serviceType; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Boolean getIsUnique() { return isUnique; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Integer getIdProvider() { return idProvider; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}