package iwaproject.user_microservice.controller;

import iwaproject.user_microservice.dto.KeycloakUserWebhookDTO;
import iwaproject.user_microservice.service.UserWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook controller to receive user events from Keycloak
 * These endpoints are called by the Keycloak webhook extension
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class UserWebhookController {

    private final UserWebhookService webhookService;

    /**
     * Handle user creation from Keycloak
     */
    @PostMapping("/users")
    public ResponseEntity<Void> handleUserCreate(@RequestBody KeycloakUserWebhookDTO userData) {
        log.info("Received user creation webhook for user: {}", userData.getId());
        try {
            webhookService.createUser(userData);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error handling user creation webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle user update from Keycloak
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> handleUserUpdate(
            @PathVariable String userId,
            @RequestBody KeycloakUserWebhookDTO userData) {
        log.info("Received user update webhook for user: {}", userId);
        try {
            webhookService.updateUser(userId, userData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling user update webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle user deletion from Keycloak (soft delete)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> handleUserDelete(@PathVariable String userId) {
        log.info("Received user deletion webhook for user: {}", userId);
        try {
            webhookService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling user deletion webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}
