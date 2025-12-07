package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.KeycloakUserWebhookDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service to handle webhook events from Keycloak and sync users to the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserWebhookService {

    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private iwaproject.user_microservice.kafka.producer.LogProducer logProducer;
    private final StripeClient stripeClient;

    /**
     * Create a new user from Keycloak webhook data
     */
    @Transactional
    public void createUser(KeycloakUserWebhookDTO webhookData) {
        log.info("Creating user from Keycloak webhook: {}", webhookData.getId());

        // Check if user already exists (to handle duplicate events)
        String userId = webhookData.getId();
        if (userId != null && userRepository.existsById(userId)) {
            log.warn("User {} already exists, updating instead", userId);
            updateUser(userId, webhookData);
            return;
        }
        User user = User.builder()
                .id(webhookData.getId()) // Use Keycloak ID as primary key
                .username(webhookData.getUsername())
                .email(webhookData.getEmail())
                .firstName(webhookData.getFirstName())
                .lastName(webhookData.getLastName())
                .build();

        userRepository.save(Objects.requireNonNull(user));

        // Try to create a Stripe Connect account for the user and save the account id
        try {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String accountId = stripeClient.createConnectAccount(user.getEmail());
                if (accountId != null) {
                    user.setStripeAccountId(accountId);
                    userRepository.save(user);
                    log.info("Stripe account {} associated with user {}", accountId, user.getId());
                } else {
                    log.warn("Stripe client returned null accountId for user {}", user.getId());
                }
            } else {
                log.warn("User {} has no email, skipping Stripe account creation", user.getId());
            }
        } catch (Exception e) {
            // Do not fail the whole webhook processing if Stripe call fails. Log and move on.
            log.error("Failed to create Stripe account for user {}: {}", user.getId(), e.getMessage(), e);
        }

        log.info("User {} created successfully in local database", user.getId());
    }

    /**
     * Update an existing user from Keycloak webhook data
     */
    @Transactional
    public void updateUser(String userId, KeycloakUserWebhookDTO webhookData) {
        log.info("Updating user from Keycloak webhook: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

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

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // Send a log to Kafka about the user deletion (if Kafka/logging is enabled)
        if (logProducer != null) {
            logProducer.sendLog("WARN",
                String.format("User deleted via Keycloak webhook - ID: %s, Username: %s",
                    userId, user.getUsername()),
                null, userId, null, null, null);
        }

        log.info("User {} soft deleted successfully in local database", userId);
    }
}
