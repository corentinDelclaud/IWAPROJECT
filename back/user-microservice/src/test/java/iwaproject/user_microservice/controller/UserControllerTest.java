package iwaproject.user_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.dto.UserStatsDTO;
import iwaproject.user_microservice.entity.User;
import iwaproject.user_microservice.exception.UserNotFoundException;
import iwaproject.user_microservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@SuppressWarnings("null")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserService userService;

    private User testUser;
    private UserProfileDTO testProfileDTO;
    private UserPublicDTO testPublicDTO;
    private UpdateProfileDTO updateDTO;
    private Jwt testJwt;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test DTOs
        testProfileDTO = UserProfileDTO.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPublicDTO = UserPublicDTO.builder()
                .id("test-user-id")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        updateDTO = new UpdateProfileDTO("newusername", "NewFirst", "NewLast");

        // Create mock JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("preferred_username", "testuser");
        claims.put("email", "test@example.com");
        claims.put("given_name", "Test");
        claims.put("family_name", "User");
        
        testJwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                claims
        );
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return current user profile")
    void getCurrentUserProfile_Success() throws Exception {
        // Given
        when(userService.getOrCreateUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testUser);
        when(userService.getUserProfile(anyString())).thenReturn(testProfileDTO);
        
        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-user-id"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).getOrCreateUser("test-user-id", "testuser", "test@example.com", "Test", "User");
        verify(userService).getUserProfile("test-user-id");
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should update user profile successfully")
    void updateCurrentUserProfile_Success() throws Exception {
        // Given
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id("test-user-id")
                .username("newusername")
                .email("test@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.updateProfile(eq("test-user-id"), org.mockito.ArgumentMatchers.any(UpdateProfileDTO.class)))
                .thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.firstName").value("NewFirst"))
                .andExpect(jsonPath("$.lastName").value("NewLast"));

        verify(userService).updateProfile(eq("test-user-id"), org.mockito.ArgumentMatchers.any(UpdateProfileDTO.class));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should return 400 for invalid data")
    void updateCurrentUserProfile_InvalidData() throws Exception {
        // Given - invalid username (too short)
        UpdateProfileDTO invalidDTO = new UpdateProfileDTO("ab", "First", "Last");

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(anyString(), org.mockito.ArgumentMatchers.any(UpdateProfileDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/users/profile - Should soft delete user profile")
    void deleteCurrentUserProfile_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteProfile("test-user-id");

        // When & Then
        mockMvc.perform(delete("/api/users/profile")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isNoContent());

        verify(userService).deleteProfile("test-user-id");
    }

    @Test
    @DisplayName("GET /api/users/{userId} - Should return public user profile")
    void getUserPublicProfile_Success() throws Exception {
        // Given
        when(userService.getPublicProfile("test-user-id")).thenReturn(testPublicDTO);

        // When & Then
        mockMvc.perform(get("/api/users/test-user-id")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-user-id"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).getPublicProfile("test-user-id");
    }

    @Test
    @DisplayName("GET /api/users/{userId} - Should return 404 when user not found")
    void getUserPublicProfile_NotFound() throws Exception {
        // Given
        when(userService.getPublicProfile("non-existent-id"))
                .thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/non-existent-id")
                        .with(jwt()))
                .andExpect(status().isNotFound());

        verify(userService).getPublicProfile("non-existent-id");
    }

    @Test
    @DisplayName("GET /api/users - Should return paginated list of users")
    void getAllUsers_Success() throws Exception {
        // Given
        List<UserPublicDTO> users = Arrays.asList(testPublicDTO);
        Page<UserPublicDTO> page = new PageImpl<>(users, PageRequest.of(0, 20), 1);

        when(userService.getAllUsers(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getAllUsers(any());
    }

    @Test
    @DisplayName("GET /api/users/search - Should search users by query")
    void searchUsers_Success() throws Exception {
        // Given
        List<UserPublicDTO> users = Arrays.asList(testPublicDTO);
        Page<UserPublicDTO> page = new PageImpl<>(users, PageRequest.of(0, 20), 1);

        when(userService.searchUsers(eq("test"), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .with(jwt())
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userService).searchUsers(eq("test"), any());
    }

    @Test
    @DisplayName("GET /api/users/email/{email} - Should return user by email")
    void getUserByEmail_Success() throws Exception {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(testPublicDTO);

        // When & Then
        mockMvc.perform(get("/api/users/email/test@example.com")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").doesNotExist()); // Public DTO doesn't include email

        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("POST /api/users/batch - Should return users by IDs")
    void getUsersByIds_Success() throws Exception {
        // Given
        List<String> userIds = Arrays.asList("test-user-id", "another-id");
        List<UserPublicDTO> users = Arrays.asList(testPublicDTO);

        when(userService.getUsersByIds(userIds)).thenReturn(users);

        // When & Then
        mockMvc.perform(post("/api/users/batch")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(userService).getUsersByIds(userIds);
    }

    @Test
    @DisplayName("GET /api/users/{userId}/exists - Should check if user exists")
    void userExists_Success() throws Exception {
        // Given
        when(userService.userExists("test-user-id")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/users/test-user-id/exists")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(userService).userExists("test-user-id");
    }

    @Test
    @DisplayName("GET /api/users/{userId}/exists - Should return false when user doesn't exist")
    void userExists_NotFound() throws Exception {
        // Given
        when(userService.userExists("non-existent-id")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/users/non-existent-id/exists")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(userService).userExists("non-existent-id");
    }

    @Test
    @DisplayName("GET /api/users/email/{email}/exists - Should check if user exists by email")
    void userExistsByEmail_Success() throws Exception {
        // Given
        when(userService.userExistsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/users/email/test@example.com/exists")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(userService).userExistsByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /api/users/stats - Should return user statistics")
    void getUserStats_Success() throws Exception {
        // Given
        UserStatsDTO stats = UserStatsDTO.builder()
                .totalActiveUsers(100L)
                .totalDeletedUsers(10L)
                .usersLastMonth(25L)
                .build();

        when(userService.getUserStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/users/stats")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActiveUsers").value(100))
                .andExpect(jsonPath("$.totalDeletedUsers").value(10))
                .andExpect(jsonPath("$.usersLastMonth").value(25));

        verify(userService).getUserStats();
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return 401 when not authenticated")
    void getCurrentUserProfile_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyString());
    }
}
