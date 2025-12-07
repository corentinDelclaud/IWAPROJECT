package com.iwaproject.product.service;

import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.dto.ProductDTO;
import com.iwaproject.product.dto.stripe.StripeProductRequest;
import com.iwaproject.product.dto.stripe.StripeProductResponse;
import com.iwaproject.product.kafka.producer.LogProducer;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.Product;
import com.iwaproject.product.model.ServiceType;
import com.iwaproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    
    @Autowired(required = false)
    private LogProducer logProducer;
    
    @Value("${stripe.service.url}")
    private String stripeServiceUrl;

    // Récupérer tous les services
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer tous les services disponibles
    public List<ProductDTO> getAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer un service par ID
    public Optional<ProductDTO> getProductById(Integer id) {
        return productRepository.findById(id)
                .map(ProductDTO::fromEntity);
    }

    // Récupérer les services par jeu
    public List<ProductDTO> getProductsByGame(Game game) {
        return productRepository.findByGame(game.name()).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services par type
    public List<ProductDTO> getProductsByType(ServiceType serviceType) {
        return productRepository.findByServiceType(serviceType.name()).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services par jeu et type
    public List<ProductDTO> getProductsByGameAndType(Game game, ServiceType serviceType) {
        return productRepository.findByGameAndServiceType(game.name(), serviceType.name()).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Récupérer les services d'un provider
    public List<ProductDTO> getProductsByProvider(String idProvider) {
        return productRepository.findByIdProvider(idProvider).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Créer un nouveau service
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating product for provider: {}", request.getIdProvider());
        
        // 1. Récupérer le stripeAccountId du provider
        // Note: Pour l'instant, on suppose qu'il est fourni dans la requête
        // Dans une version future, on pourrait appeler user-microservice pour le récupérer
        String stripeAccountId = request.getStripeAccountId();
        
        if (stripeAccountId == null || stripeAccountId.isEmpty()) {
            log.error("Provider {} has no Stripe account", request.getIdProvider());
            throw new RuntimeException("Provider must have a Stripe account to create products");
        }
        
        // 2. Créer le produit dans Stripe
        StripeProductResponse stripeProduct;
        try {
            stripeProduct = createStripeProduct(
                request.getDescription(),
                request.getPrice(),
                stripeAccountId
            );
            log.info("Stripe product created with priceId: {}", stripeProduct.getPriceId());
        } catch (RestClientException e) {
            log.error("Failed to create product in Stripe", e);
            throw new RuntimeException("Cannot create product: Stripe error - " + e.getMessage(), e);
        }
        
        // 3. Créer le produit local avec les IDs Stripe
        Product product = new Product();
        product.setGame(request.getGame());
        product.setServiceType(request.getServiceType());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnique(request.getUnique());
        product.setIsAvailable(request.getIsAvailable());
        product.setIdProvider(request.getIdProvider());
        
        // Ajouter les IDs Stripe
        product.setStripePriceId(stripeProduct.getPriceId());
        product.setStripeAccountId(stripeAccountId);
        // Note: productId n'est pas retourné par l'endpoint actuel de stripe-service
        // On pourrait le stocker si stripe-service est modifié pour le retourner

        Product savedProduct = productRepository.save(product);
        log.info("Product created locally with id: {}", savedProduct.getIdService());
        
        // Send log to Kafka
        if (logProducer != null) {
            logProducer.sendLog("INFO", 
                String.format("Product created: id=%s, game=%s, serviceType=%s, price=%.2f, provider=%s", 
                    savedProduct.getIdService(), 
                    savedProduct.getGame(), 
                    savedProduct.getServiceType(), 
                    savedProduct.getPrice(),
                    savedProduct.getIdProvider()));
        }
        
        return ProductDTO.fromEntity(savedProduct);
    }
    
    /**
     * Appel au stripe-service pour créer un produit Stripe
     */
    private StripeProductResponse createStripeProduct(String description, Float price, String accountId) {
        String url = stripeServiceUrl + "/api/stripe/product";
        
        StripeProductRequest request = new StripeProductRequest();
        request.setProductName(description); // On utilise la description comme nom
        request.setProductDescription(description);
        request.setProductPrice(Math.round(price * 100)); // Convertir en centimes
        request.setAccountId(accountId);
        
        log.debug("Calling Stripe API: {} with accountId: {}", url, accountId);
        
        return restTemplate.postForObject(url, request, StripeProductResponse.class);
    }

    // Mettre à jour un service
    @Transactional
    public Optional<ProductDTO> updateProduct(Integer id, CreateProductRequest request) {
        return productRepository.findById(id).map(product -> {
            product.setGame(request.getGame());
            product.setServiceType(request.getServiceType());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setUnique(request.getUnique());
            product.setIsAvailable(request.getIsAvailable());
            product.setIdProvider(request.getIdProvider());

            Product updatedProduct = productRepository.save(product);
            return ProductDTO.fromEntity(updatedProduct);
        });
    }

    // Supprimer un service
    @Transactional
    public boolean deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Changer la disponibilité d'un service
    @Transactional
    public Optional<ProductDTO> toggleAvailability(Integer id) {
        return productRepository.findById(id).map(product -> {
            product.setIsAvailable(!product.getIsAvailable());
            Product updatedProduct = productRepository.save(product);
            return ProductDTO.fromEntity(updatedProduct);
        });
    }

    // Récupérer les services par filtres : jeu, type, fourchette de prix, provider
    public List<ProductDTO> getProductsByFilters(Game game,
                                                 ServiceType serviceType,
                                                 Float minPrice,
                                                 Float maxPrice,
                                                 String idProvider) {

        String gameStr = (game == null) ? null : game.name();
        String typeStr = (serviceType == null) ? null : serviceType.name();

        return productRepository.findByFilters(gameStr, typeStr, minPrice, maxPrice, idProvider)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

