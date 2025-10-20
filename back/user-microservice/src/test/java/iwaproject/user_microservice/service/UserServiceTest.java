package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserAlreadyExistsException;
import iwaproject.user_microservice.exception.UserNotFoundException;
import iwaproject.user_microservice.kafka.producer.UserEventProducer;
import iwaproject.user_microservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String TEST_USER_ID = "test-user-id-123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserProfile_WhenUserExists_ShouldReturnUserProfileDTO() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(TEST_USER_ID))
                .thenReturn(Optional.of(testUser));

        // When
        UserProfileDTO result = userService.getUserProfile(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        
        verify(userRepository, times(1)).findByIdAndDeletedAtIsNull(TEST_USER_ID);
    }

    @Test
    void getUserProfile_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
        
        verify(userRepository, times(1)).findByIdAndDeletedAtIsNull("non-existent-id");
    }

    @Test
    void getPublicProfile_WhenUserExists_ShouldReturnUserPublicDTO() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(TEST_USER_ID))
                .thenReturn(Optional.of(testUser));

        // When
        UserPublicDTO result = userService.getPublicProfile(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        // Email should not be in public DTO
        
        verify(userRepository, times(1)).findByIdAndDeletedAtIsNull(TEST_USER_ID);
    }

    @Test
    void updateProfile_WhenValidData_ShouldUpdateAndReturnDTO() {
        // Given
        UpdateProfileDTO updateDTO = new UpdateProfileDTO(
                "newusername",
                "UpdatedFirst",
                "UpdatedLast"
        );
        
        when(userRepository.findByIdAndDeletedAtIsNull(TEST_USER_ID))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileDTO result = userService.updateProfile(TEST_USER_ID, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventProducer, times(1)).publishUserEvent(any());
    }

    @Test
    void updateProfile_WhenUsernameAlreadyTaken_ShouldThrowException() {
        // Given
        UpdateProfileDTO updateDTO = new UpdateProfileDTO(
                "existingusername",
                "UpdatedFirst",
                "UpdatedLast"
        );
        
        when(userRepository.findByIdAndDeletedAtIsNull(TEST_USER_ID))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existingusername")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(TEST_USER_ID, updateDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");
        
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publishUserEvent(any());
    }

    @Test
    void deleteProfile_WhenUserExists_ShouldMarkAsDeleted() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(TEST_USER_ID))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deleteProfile(TEST_USER_ID);

        // Then
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getDeletedAt() != null
        ));
        verify(userEventProducer, times(1)).publishUserEvent(any());
    }

    @Test
    void createUser_WhenValidData_ShouldCreateUser() {
        // Given
        String keycloakId = "new-keycloak-id";
        when(userRepository.findById(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(
                keycloakId,
                "newuser",
                "new@example.com",
                "New",
                "User"
        );

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventProducer, times(1)).publishUserEvent(any());
    }

    @Test
    void createUser_WhenUserAlreadyExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(
                TEST_USER_ID,
                "username",
                "email@example.com",
                "First",
                "Last"
        ))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists");
        
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publishUserEvent(any());
    }
}
