package com.iwaproject.catalog.controller;

import com.iwaproject.catalog.dto.CreateServiceRequest;
import com.iwaproject.catalog.dto.ServiceDTO;
import com.iwaproject.catalog.model.Game;
import com.iwaproject.catalog.model.ServiceType;
import com.iwaproject.catalog.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CatalogController {

    private final CatalogService catalogService;

    // GET /api/catalog - Récupérer tous les services
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices(
            @RequestParam(required = false) Boolean available
    ) {
        if (available != null && available) {
            return ResponseEntity.ok(catalogService.getAvailableServices());
        }
        return ResponseEntity.ok(catalogService.getAllServices());
    }

    // GET /api/catalog/{id} - Récupérer un service par ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Integer id) {
        return catalogService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/catalog/game/{game} - Récupérer les services par jeu
    @GetMapping("/game/{game}")
    public ResponseEntity<List<ServiceDTO>> getServicesByGame(@PathVariable Game game) {
        return ResponseEntity.ok(catalogService.getServicesByGame(game));
    }

    // GET /api/catalog/type/{type} - Récupérer les services par type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ServiceDTO>> getServicesByType(@PathVariable ServiceType type) {
        return ResponseEntity.ok(catalogService.getServicesByType(type));
    }

    // GET /api/catalog/filter - Filtrer par jeu et type
    @GetMapping("/filter")
    public ResponseEntity<List<ServiceDTO>> filterServices(
            @RequestParam(required = false) Game game,
            @RequestParam(required = false) ServiceType type
    ) {
        if (game != null && type != null) {
            return ResponseEntity.ok(catalogService.getServicesByGameAndType(game, type));
        } else if (game != null) {
            return ResponseEntity.ok(catalogService.getServicesByGame(game));
        } else if (type != null) {
            return ResponseEntity.ok(catalogService.getServicesByType(type));
        }
        return ResponseEntity.ok(catalogService.getAllServices());
    }

    // GET /api/catalog/provider/{idProvider} - Récupérer les services d'un provider
    @GetMapping("/provider/{idProvider}")
    public ResponseEntity<List<ServiceDTO>> getServicesByProvider(@PathVariable Integer idProvider) {
        return ResponseEntity.ok(catalogService.getServicesByProvider(idProvider));
    }

    // POST /api/catalog - Créer un nouveau service
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(@Valid @RequestBody CreateServiceRequest request) {
        ServiceDTO created = catalogService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/catalog/{id} - Mettre à jour un service
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        return catalogService.updateService(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/catalog/{id} - Supprimer un service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Integer id) {
        if (catalogService.deleteService(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // PATCH /api/catalog/{id}/toggle-availability - Changer la disponibilité
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<ServiceDTO> toggleAvailability(@PathVariable Integer id) {
        return catalogService.toggleAvailability(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

