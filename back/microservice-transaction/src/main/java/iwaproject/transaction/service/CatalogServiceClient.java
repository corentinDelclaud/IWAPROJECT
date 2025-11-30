package iwaproject.transaction.service;

import iwaproject.transaction.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class CatalogServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogServiceClient.class);
    
    private final WebClient webClient;
    private final String catalogBasePath;
    
    public CatalogServiceClient(
            @Value("${api-gateway.url}") String gatewayUrl,
            @Value("${catalog-service.path}") String catalogPath,
            WebClient.Builder webClientBuilder) {
        this.catalogBasePath = catalogPath;
        this.webClient = webClientBuilder
                .baseUrl(gatewayUrl)
                .build();
        log.info("CatalogServiceClient initialized with gateway URL: {}", gatewayUrl);
    }
    
    /**
     * Récupère un produit depuis le service-catalog via la gateway
     */
    public ProductDTO getProductById(Integer productId) {
        String url = catalogBasePath + "/" + productId;
        log.info("#debuglog Calling catalog service: GET {}", url);
        
        try {
            ProductDTO product = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
            
            log.info("#debuglog Successfully retrieved product {} from catalog service", productId);
            log.debug("#debuglog Product details: game={}, type={}, price={}, available={}, provider={}", 
                    product.game(), product.serviceType(), product.price(), 
                    product.isAvailable(), product.idProvider());  // idProvider maintenant String
            return product;
            
        } catch (WebClientResponseException.NotFound e) {
            log.error("#debuglog Product {} not found in catalog service", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
            
        } catch (Exception e) {
            log.error("#debuglog Failed to call catalog service for product {}: {}", 
                    productId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve product from catalog service", e);
        }
    }
}