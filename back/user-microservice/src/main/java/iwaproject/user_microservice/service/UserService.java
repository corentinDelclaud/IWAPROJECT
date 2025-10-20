package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserAlreadyExistsException;
import iwaproject.user_microservice.exception.UserDeletedException;
import iwaproject.user_microservice.exception.UserNotFoundException;
import iwaproject.user_microservice.kafka.event.UserEvent;
import iwaproject.user_microservice.kafka.producer.UserEventProducer;
import iwaproject.user_microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private UserEventProducer userEventProducer;

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

        if (userRepository.findById(keycloakId).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with id: " + keycloakId);
        }

        User user = User.builder()
                .id(keycloakId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", keycloakId);

        // Publish user created event
        publishUserEvent(savedUser, "USER_CREATED");

        return savedUser;
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
