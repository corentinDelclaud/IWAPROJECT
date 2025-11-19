package iwaproject.transaction.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CatalogClient {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    
    private final WebClient webClient;
    
    public CatalogClient(@Value("${services.gateway.url}") String gatewayUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(gatewayUrl)
            .build();
    }
    
    public ProductDTO getProduct(Integer productId) {
        log.info("#debuglog Calling catalog service for product ID: {}", productId);
        
        try {
            ProductDTO product = webClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();
            
            if (product != null) {
                log.info("#debuglog Product retrieved successfully: id={}, available={}, provider={}", 
                    product.idService(), product.isAvailable(), product.idProvider());
            } else {
                log.warn("#debuglog Product not found: id={}", productId);
            }
            
            return product;
        } catch (Exception e) {
            log.error("#debuglog Failed to retrieve product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve product from catalog", e);
        }
    }
    
    public record ProductDTO(
        Integer idService,
        String game,
        String serviceType,
        String description,
        Float price,
        Boolean unique,
        Boolean isAvailable,
        Integer idProvider
    ) {}
}