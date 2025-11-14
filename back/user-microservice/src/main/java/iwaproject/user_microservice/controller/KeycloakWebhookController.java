package iwaproject.user_microservice.controller;

import iwaproject.user_microservice.dto.KeycloakUserRegistrationDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserAlreadyExistsException;
import iwaproject.user_microservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for receiving webhooks from Keycloak
 * This endpoint allows Keycloak to notify the microservice when users register
 */
@RestController
@RequestMapping("/api/webhooks/keycloak")
@RequiredArgsConstructor
@Slf4j
public class KeycloakWebhookController {

    private final UserService userService;

    /**
     * Endpoint called by Keycloak when a new user registers
     * POST /api/webhooks/keycloak/user-registered
     */
    @PostMapping("/user-registered")
    public ResponseEntity<?> handleUserRegistration(@Valid @RequestBody KeycloakUserRegistrationDTO registration) {
        log.info("Received user registration webhook for user: {}", registration.getUsername());

        try {
            User user = userService.createUser(
                    registration.getUserId(),
                    registration.getUsername(),
                    registration.getEmail(),
                    registration.getFirstName(),
                    registration.getLastName()
            );

            log.info("User created successfully from webhook: {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (UserAlreadyExistsException e) {
            log.warn("User already exists: {}", registration.getUserId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");

        } catch (Exception e) {
            log.error("Error creating user from webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for the webhook
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Keycloak webhook endpoint is healthy");
    }
}
