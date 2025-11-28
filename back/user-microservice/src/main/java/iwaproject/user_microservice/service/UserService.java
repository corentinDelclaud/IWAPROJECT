package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.dto.UserStatsDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserAlreadyExistsException;
import iwaproject.user_microservice.exception.UserNotFoundException;
import iwaproject.user_microservice.kafka.event.UserEvent;
import iwaproject.user_microservice.kafka.producer.UserEventProducer;
import iwaproject.user_microservice.kafka.producer.LogProducer;
import iwaproject.user_microservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private UserEventProducer userEventProducer;
    
    @Autowired(required = false)
    private LogProducer logProducer;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the complete profile of the current user
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String userId) {
        log.info("Fetching profile for user: {}", userId);
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return mapToProfileDTO(user);
    }

    /**
     * Get public profile of any user (visible to others)
     */
    @Transactional(readOnly = true)
    public UserPublicDTO getPublicProfile(String userId) {
        log.info("Fetching public profile for user: {}", userId);
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return mapToPublicDTO(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserProfileDTO updateProfile(String userId, UpdateProfileDTO updateDTO) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(updateDTO.getUsername())) {
            if (userRepository.existsByUsername(updateDTO.getUsername())) {
                throw new UserAlreadyExistsException("Username already taken: " + updateDTO.getUsername());
            }
            user.setUsername(updateDTO.getUsername());
        }

        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());

        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);

        // Send log to Kafka
        if (logProducer != null) {
            logProducer.sendLog("INFO", 
                String.format("User profile updated - ID: %s, Username: %s", 
                    userId, savedUser.getUsername()),
                null, userId, null, null, null);
        }

        // Publish user updated event
        publishUserEvent(savedUser, "USER_UPDATED");

        return mapToProfileDTO(savedUser);
    }

    /**
     * Soft delete user profile
     */
    @Transactional
    public void deleteProfile(String userId) {
        log.info("Deleting profile for user: {}", userId);
        
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Send log to Kafka
        if (logProducer != null) {
            logProducer.sendLog("WARN", 
                String.format("User profile deleted - ID: %s, Username: %s", 
                    userId, user.getUsername()),
                null, userId, null, null, null);
        }
        
        // Publish user deleted event
        publishUserEvent(user, "USER_DELETED");
        
        log.info("Profile deleted successfully for user: {}", userId);
    }

    /**
     * Create a new user (called from Kafka consumer when user registers in Keycloak)
     */
    @Transactional
    public User createUser(String keycloakId, String username, String email, String firstName, String lastName) {
        log.info("Creating new user with Keycloak ID: {}", keycloakId);

        if (keycloakId != null && userRepository.findById(keycloakId).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with id: " + keycloakId);
        }

        User user = User.builder()
                .id(keycloakId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();


        if (user == null) {
            throw new RuntimeException("Failed to save user with id: " + keycloakId);
        }
        User savedUser = userRepository.save(user);
        
        log.info("User created successfully: {}", keycloakId);

        // Send log to Kafka
        log.info("LogProducer is: {}", logProducer != null ? "AVAILABLE" : "NULL");
        if (logProducer != null) {
            log.info("Attempting to send log to Kafka for user creation");
            logProducer.sendLog("INFO", 
                String.format("New user created - ID: %s, Username: %s, Email: %s", 
                    keycloakId, username, email),
                null, keycloakId, null, null, null);
            log.info("Log sent to Kafka successfully");
        } else {
            log.warn("LogProducer is NULL, cannot send log to Kafka");
        }

        // Publish user created event
        publishUserEvent(savedUser, "USER_CREATED");

        return savedUser;
    }

    /**
     * Get or create user - automatically creates user if they don't exist yet
     * Useful for auto-syncing users on first API access
     */
    @Transactional
    public User getOrCreateUser(String keycloakId, String username, String email, String firstName, String lastName) {
        log.debug("Getting or creating user with Keycloak ID: {}", keycloakId);
        if (keycloakId == null) {
            throw new IllegalArgumentException("Keycloak ID cannot be null");
        }
        Optional<User> existingUser = userRepository.findById(keycloakId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // If a user with the same username already exists (but with a different id),
        // return that existing user to avoid unique constraint violations.
        // This handles the case where the DB has a pre-existing user created earlier
        // and Keycloak now provides a different subject id for the same username.
        if (username != null) {
            Optional<User> byUsername = userRepository.findByUsernameAndDeletedAtIsNull(username);
            if (byUsername.isPresent()) {
                log.warn("Found existing user by username with different id. Returning existing user id={}", byUsername.get().getId());
                return byUsername.get();
            }
        }

        // Also check by email if provided
        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
            if (byEmail.isPresent()) {
                log.warn("Found existing user by email with different id. Returning existing user id={}", byEmail.get().getId());
                return byEmail.get();
            }
        }

        log.info("User not found in database, creating new user: {}", keycloakId);
        return createUser(keycloakId, username, email, firstName, lastName);
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserPublicDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        Page<User> users = userRepository.findAllByDeletedAtIsNull(pageable);
        return users.map(this::mapToPublicDTO);
    }

    /**
     * Search users by username or email
     */
    @Transactional(readOnly = true)
    public Page<UserPublicDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);
        Page<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNull(
                searchTerm, searchTerm, pageable);
        return users.map(this::mapToPublicDTO);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserPublicDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToPublicDTO(user);
    }

    /**
     * Get multiple users by their IDs
     */
    @Transactional(readOnly = true)
    public List<UserPublicDTO> getUsersByIds(List<String> userIds) {
        log.info("Fetching users by IDs: {}", userIds);
        List<User> users = userRepository.findAllByIdInAndDeletedAtIsNull(userIds);
        return users.stream()
                .map(this::mapToPublicDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if user exists by ID
     */
    @Transactional(readOnly = true)
    public boolean userExists(String userId) {
        return userRepository.existsByIdAndDeletedAtIsNull(userId);
    }

    /**
     * Check if user exists by email
     */
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats() {
        
        long totalUsers = userRepository.countByDeletedAtIsNull();
        long deletedUsers = userRepository.countByDeletedAtIsNotNull();
        long recentUsers = userRepository.countByCreatedAtAfterAndDeletedAtIsNull(
                LocalDateTime.now().minusDays(30));
        
        // Send log to Kafka
        if (logProducer != null) {
            logProducer.sendLog("INFO", 
                String.format("User statistics requested - Total: %d, Deleted: %d, Recent: %d", 
                    totalUsers, deletedUsers, recentUsers));
        } else {
            log.warn("LogProducer is not available, skipping log sending");
        }
        
        return UserStatsDTO.builder()
                .totalActiveUsers(totalUsers)
                .totalDeletedUsers(deletedUsers)
                .usersLastMonth(recentUsers)
                .build();
    }

    // Helper methods for mapping
    private UserProfileDTO mapToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserPublicDTO mapToPublicDTO(User user) {
        return UserPublicDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private void publishUserEvent(User user, String eventType) {
        if (userEventProducer == null) {
            log.debug("Kafka is disabled, skipping event publishing for: {}", eventType);
            return;
        }
        
        UserEvent event = UserEvent.builder()
                .eventType(eventType)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .timestamp(LocalDateTime.now())
                .build();
        
        userEventProducer.publishUserEvent(event);
    }
}
