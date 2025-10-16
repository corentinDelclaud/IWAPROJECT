package com.iwaproject.product.service;

import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.dto.ProductDTO;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.Product;
import com.iwaproject.product.model.ServiceType;
import com.iwaproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Récupérer tous les services
    public List<ProductDTO> getAllProducts() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
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
    public List<ProductDTO> getProductsByProvider(Integer idProvider) {
        return productRepository.findByIdProvider(idProvider).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Créer un nouveau service
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setGame(request.getGame());
        product.setServiceType(request.getServiceType());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnique(request.getUnique());
        product.setIsAvailable(request.getIsAvailable());
        product.setIdProvider(request.getIdProvider());

        Product savedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(savedProduct);
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
}

