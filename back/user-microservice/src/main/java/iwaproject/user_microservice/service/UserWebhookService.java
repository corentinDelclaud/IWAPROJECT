package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.KeycloakUserWebhookDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service to handle webhook events from Keycloak and sync users to the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserWebhookService {

    private final UserRepository userRepository;

    /**
     * Create a new user from Keycloak webhook data
     */
    @Transactional
    public void createUser(KeycloakUserWebhookDTO webhookData) {
        log.info("Creating user from Keycloak webhook: {}", webhookData.getId());

        // Check if user already exists (to handle duplicate events)
        if (userRepository.existsById(webhookData.getId())) {
            log.warn("User {} already exists, updating instead", webhookData.getId());
            updateUser(webhookData.getId(), webhookData);
            return;
        }

        User user = User.builder()
                .id(webhookData.getId()) // Use Keycloak ID as primary key
                .username(webhookData.getUsername())
                .email(webhookData.getEmail())
                .firstName(webhookData.getFirstName())
                .lastName(webhookData.getLastName())
                .build();

        userRepository.save(user);
        log.info("User {} created successfully in local database", user.getId());
    }

    /**
     * Update an existing user from Keycloak webhook data
     */
    @Transactional
    public void updateUser(String userId, KeycloakUserWebhookDTO webhookData) {
        log.info("Updating user from Keycloak webhook: {}", userId);

        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    log.warn("User {} not found, creating new user", userId);
                    return User.builder().id(userId).build();
                });

        // Update user fields
        user.setUsername(webhookData.getUsername());
        user.setEmail(webhookData.getEmail());
        user.setFirstName(webhookData.getFirstName());
        user.setLastName(webhookData.getLastName());

        userRepository.save(user);
        log.info("User {} updated successfully in local database", userId);
    }

    /**
     * Soft delete a user (set deletedAt timestamp)
     */
    @Transactional
    public void deleteUser(String userId) {
        log.info("Soft deleting user from Keycloak webhook: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User {} soft deleted successfully in local database", userId);
    }
}
