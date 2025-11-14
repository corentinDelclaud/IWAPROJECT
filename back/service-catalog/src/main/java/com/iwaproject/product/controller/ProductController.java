package com.iwaproject.product.controller;

import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.dto.ProductDTO;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.ServiceType;
import com.iwaproject.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    // GET /api/products - Récupérer tous les services
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) Boolean available
    ) {
        if (available != null && available) {
            return ResponseEntity.ok(productService.getAvailableProducts());
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET /api/products/{id} - Récupérer un service par ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/products/game/{game} - Récupérer les services par jeu
    @GetMapping("/game/{game}")
    public ResponseEntity<List<ProductDTO>> getProductsByGame(@PathVariable Game game) {
        return ResponseEntity.ok(productService.getProductsByGame(game));
    }

    // GET /api/products/type/{type} - Récupérer les services par type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ProductDTO>> getProductsByType(@PathVariable ServiceType type) {
        return ResponseEntity.ok(productService.getProductsByType(type));
    }

    // GET /api/products/provider/{idProvider} - Récupérer les services d'un provider
    @GetMapping("/provider/{idProvider}")
    public ResponseEntity<List<ProductDTO>> getProductsByProvider(@PathVariable Integer idProvider) {
        return ResponseEntity.ok(productService.getProductsByProvider(idProvider));
    }

    // POST /api/products - Créer un nouveau service
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/products/{id} - Mettre à jour un service
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.updateProduct(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id} - Supprimer un service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // PATCH /api/products/{id}/toggle-availability - Changer la disponibilité
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<ProductDTO> toggleAvailability(@PathVariable Integer id) {
        return productService.toggleAvailability(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/products/search?type=TYPE&game=GAME&minPrice=MIN&maxPrice=MAX&idProvider=ID
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) Game game,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(required = false) Integer idProvider
    ) {
        List<ProductDTO> products = productService.getProductsByFilters(
                game, type, minPrice, maxPrice, idProvider
        );
        return ResponseEntity.ok(products);
    }
}

