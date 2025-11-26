package iwaproject.user_microservice.service;

import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.dto.UserStatsDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserAlreadyExistsException;
import iwaproject.user_microservice.exception.UserNotFoundException;
import iwaproject.user_microservice.kafka.producer.UserEventProducer;
import iwaproject.user_microservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceExtendedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UpdateProfileDTO updateDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("test-user-id-123")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        updateDTO = new UpdateProfileDTO("updateduser", "Updated", "Name");
    }

    // ========== getUserProfile Tests ==========

    @Test
    @DisplayName("getUserProfile - Should return user profile successfully")
    void getUserProfile_Success() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));

        // When
        UserProfileDTO result = userService.getUserProfile("test-user-id-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-user-id-123");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");

        verify(userRepository).findByIdAndDeletedAtIsNull("test-user-id-123");
    }

    @Test
    @DisplayName("getUserProfile - Should throw exception when user not found")
    void getUserProfile_UserNotFound() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("non-existent-id"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: non-existent-id");

        verify(userRepository).findByIdAndDeletedAtIsNull("non-existent-id");
    }

    // ========== getPublicProfile Tests ==========

    @Test
    @DisplayName("getPublicProfile - Should return public profile successfully")
    void getPublicProfile_Success() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));

        // When
        UserPublicDTO result = userService.getPublicProfile("test-user-id-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-user-id-123");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");

        verify(userRepository).findByIdAndDeletedAtIsNull("test-user-id-123");
    }

    @Test
    @DisplayName("getPublicProfile - Should throw exception when user deleted")
    void getPublicProfile_UserDeleted() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("deleted-user"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getPublicProfile("deleted-user"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ========== updateProfile Tests ==========

    @Test
    @DisplayName("updateProfile - Should update profile successfully")
    void updateProfile_Success() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileDTO result = userService.updateProfile("test-user-id-123", updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByIdAndDeletedAtIsNull("test-user-id-123");
        verify(userRepository).existsByUsername("updateduser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - Should update without changing username")
    void updateProfile_SameUsername() {
        // Given
        UpdateProfileDTO sameUsernameDTO = new UpdateProfileDTO("testuser", "NewFirst", "NewLast");
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileDTO result = userService.updateProfile("test-user-id-123", sameUsernameDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByIdAndDeletedAtIsNull("test-user-id-123");
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - Should throw exception when username taken")
    void updateProfile_UsernameTaken() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile("test-user-id-123", updateDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository).existsByUsername("updateduser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - Should throw exception when user not found")
    void updateProfile_UserNotFound() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("non-existent-id"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile("non-existent-id", updateDTO))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== deleteProfile Tests ==========

    @Test
    @DisplayName("deleteProfile - Should soft delete user successfully")
    void deleteProfile_Success() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deleteProfile("test-user-id-123");

        // Then
        verify(userRepository).findByIdAndDeletedAtIsNull("test-user-id-123");
        verify(userRepository).save(argThat(user -> user.getDeletedAt() != null));
    }

    @Test
    @DisplayName("deleteProfile - Should throw exception when user not found")
    void deleteProfile_UserNotFound() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull("non-existent-id"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteProfile("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== createUser Tests ==========

    @Test
    @DisplayName("createUser - Should create new user successfully")
    void createUser_Success() {
        // Given
        when(userRepository.findById("new-keycloak-id")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser("new-keycloak-id", "newuser", 
                "new@example.com", "New", "User");

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById("new-keycloak-id");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Should throw exception when user already exists")
    void createUser_UserAlreadyExists() {
        // Given
        when(userRepository.findById("existing-id")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser("existing-id", "user", 
                "email@test.com", "First", "Last"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists with id");

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== getOrCreateUser Tests ==========

    @Test
    @DisplayName("getOrCreateUser - Should return existing user by ID")
    void getOrCreateUser_ExistingUserById() {
        // Given
        when(userRepository.findById("test-user-id-123")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getOrCreateUser("test-user-id-123", "testuser", 
                "test@example.com", "Test", "User");

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById("test-user-id-123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getOrCreateUser - Should return existing user by username")
    void getOrCreateUser_ExistingUserByUsername() {
        // Given
        when(userRepository.findById("new-id")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.of(testUser));

        // When
        User result = userService.getOrCreateUser("new-id", "testuser", 
                "test@example.com", "Test", "User");

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getOrCreateUser - Should return existing user by email")
    void getOrCreateUser_ExistingUserByEmail() {
        // Given
        when(userRepository.findById("new-id")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameAndDeletedAtIsNull("newuser"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        User result = userService.getOrCreateUser("new-id", "newuser", 
                "test@example.com", "Test", "User");

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getOrCreateUser - Should create new user when not exists")
    void getOrCreateUser_CreateNewUser() {
        // Given
        when(userRepository.findById("new-id")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameAndDeletedAtIsNull("newuser"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull("new@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.getOrCreateUser("new-id", "newuser", 
                "new@example.com", "New", "User");

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // ========== getAllUsers Tests ==========

    @Test
    @DisplayName("getAllUsers - Should return paginated users")
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> page = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);

        // When
        Page<UserPublicDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");

        verify(userRepository).findAllByDeletedAtIsNull(pageable);
    }

    @Test
    @DisplayName("getAllUsers - Should return empty page when no users")
    void getAllUsers_EmptyPage() {
        // Given
        Page<User> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(emptyPage);

        // When
        Page<UserPublicDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ========== searchUsers Tests ==========

    @Test
    @DisplayName("searchUsers - Should find users by search term")
    void searchUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> page = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNull(
                "test", "test", pageable)).thenReturn(page);

        // When
        Page<UserPublicDTO> result = userService.searchUsers("test", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNull(
                "test", "test", pageable);
    }

    // ========== getUserByEmail Tests ==========

    @Test
    @DisplayName("getUserByEmail - Should return user by email")
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserPublicDTO result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByEmailAndDeletedAtIsNull("test@example.com");
    }

    @Test
    @DisplayName("getUserByEmail - Should throw exception when not found")
    void getUserByEmail_NotFound() {
        // Given
        when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    // ========== getUsersByIds Tests ==========

    @Test
    @DisplayName("getUsersByIds - Should return multiple users")
    void getUsersByIds_Success() {
        // Given
        User user2 = User.builder()
                .id("user-2")
                .username("user2")
                .firstName("Second")
                .lastName("User")
                .build();

        List<String> ids = Arrays.asList("test-user-id-123", "user-2");
        List<User> users = Arrays.asList(testUser, user2);

        when(userRepository.findAllByIdInAndDeletedAtIsNull(ids)).thenReturn(users);

        // When
        List<UserPublicDTO> result = userService.getUsersByIds(ids);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");

        verify(userRepository).findAllByIdInAndDeletedAtIsNull(ids);
    }

    @Test
    @DisplayName("getUsersByIds - Should return empty list for no matches")
    void getUsersByIds_EmptyList() {
        // Given
        List<String> ids = Arrays.asList("non-existent-1", "non-existent-2");
        when(userRepository.findAllByIdInAndDeletedAtIsNull(ids)).thenReturn(Arrays.asList());

        // When
        List<UserPublicDTO> result = userService.getUsersByIds(ids);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== userExists Tests ==========

    @Test
    @DisplayName("userExists - Should return true when user exists")
    void userExists_True() {
        // Given
        when(userRepository.existsByIdAndDeletedAtIsNull("test-user-id-123"))
                .thenReturn(true);

        // When
        boolean result = userService.userExists("test-user-id-123");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("userExists - Should return false when user doesn't exist")
    void userExists_False() {
        // Given
        when(userRepository.existsByIdAndDeletedAtIsNull("non-existent"))
                .thenReturn(false);

        // When
        boolean result = userService.userExists("non-existent");

        // Then
        assertThat(result).isFalse();
    }

    // ========== userExistsByEmail Tests ==========

    @Test
    @DisplayName("userExistsByEmail - Should return true when email exists")
    void userExistsByEmail_True() {
        // Given
        when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com"))
                .thenReturn(true);

        // When
        boolean result = userService.userExistsByEmail("test@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("userExistsByEmail - Should return false when email doesn't exist")
    void userExistsByEmail_False() {
        // Given
        when(userRepository.existsByEmailAndDeletedAtIsNull("notfound@example.com"))
                .thenReturn(false);

        // When
        boolean result = userService.userExistsByEmail("notfound@example.com");

        // Then
        assertThat(result).isFalse();
    }

    // ========== getUserStats Tests ==========

    @Test
    @DisplayName("getUserStats - Should return statistics correctly")
    void getUserStats_Success() {
        // Given
        when(userRepository.countByDeletedAtIsNull()).thenReturn(100L);
        when(userRepository.countByDeletedAtIsNotNull()).thenReturn(10L);
        when(userRepository.countByCreatedAtAfterAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(25L);

        // When
        UserStatsDTO result = userService.getUserStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalActiveUsers()).isEqualTo(100L);
        assertThat(result.getTotalDeletedUsers()).isEqualTo(10L);
        assertThat(result.getUsersLastMonth()).isEqualTo(25L);

        verify(userRepository).countByDeletedAtIsNull();
        verify(userRepository).countByDeletedAtIsNotNull();
        verify(userRepository).countByCreatedAtAfterAndDeletedAtIsNull(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getUserStats - Should handle zero counts")
    void getUserStats_ZeroCounts() {
        // Given
        when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);
        when(userRepository.countByDeletedAtIsNotNull()).thenReturn(0L);
        when(userRepository.countByCreatedAtAfterAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(0L);

        // When
        UserStatsDTO result = userService.getUserStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalActiveUsers()).isZero();
        assertThat(result.getTotalDeletedUsers()).isZero();
        assertThat(result.getUsersLastMonth()).isZero();
    }
}
