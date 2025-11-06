package iwaproject.user_microservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.dto.UserStatsDTO;
import iwaproject.user_microservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Returns the complete profile of the authenticated user")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // Extract user ID from JWT 'sub' claim
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        log.info("GET /api/users/profile - User (token sub): {}", userId);

        // Auto-create or resolve existing user if they don't exist yet (first login sync)
        // Use the returned User (might be an existing record with a different internal id)
        iwaproject.user_microservice.entity.User resolvedUser = userService.getOrCreateUser(userId, username, email, firstName, lastName);

        // Fetch profile using the resolved user's id to avoid conflicts when an existing user
        // (by username/email) already exists with a different id
        UserProfileDTO profile = userService.getUserProfile(resolvedUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Update the authenticated user's profile information")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileDTO updateDTO) {
        String userId = jwt.getSubject();
        log.info("PUT /api/users/profile - User: {}", userId);
        
        UserProfileDTO updatedProfile = userService.updateProfile(userId, updateDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Delete current user's profile (soft delete)
     */
    @DeleteMapping("/profile")
    @Operation(summary = "Delete current user profile", description = "Soft delete the authenticated user's profile")
    public ResponseEntity<Void> deleteCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("DELETE /api/users/profile - User: {}", userId);
        
        userService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get public profile of any user
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile", description = "Returns the public profile of a specific user")
    public ResponseEntity<UserPublicDTO> getUserPublicProfile(@PathVariable String userId) {
        log.info("GET /api/users/{} - Public profile request", userId);
        
        UserPublicDTO publicProfile = userService.getPublicProfile(userId);
        return ResponseEntity.ok(publicProfile);
    }

    /**
     * Get list of all users with pagination
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a paginated list of all active users")
    public ResponseEntity<Page<UserPublicDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /api/users - page: {}, size: {}, sortBy: {}, direction: {}", 
                page, size, sortBy, sortDirection);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserPublicDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by username or email
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by username or email with pagination")
    public ResponseEntity<Page<UserPublicDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/users/search - query: {}, page: {}, size: {}", query, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username"));
        Page<UserPublicDTO> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Returns the public profile of a user by their email")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{}", email);
        
        UserPublicDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Get multiple users by their IDs (batch operation)
     */
    @PostMapping("/batch")
    @Operation(summary = "Get users by IDs", description = "Returns multiple users by their IDs (batch operation)")
    public ResponseEntity<List<UserPublicDTO>> getUsersByIds(@RequestBody List<String> userIds) {
        log.info("POST /api/users/batch - requesting {} users", userIds.size());
        
        List<UserPublicDTO> users = userService.getUsersByIds(userIds);
        return ResponseEntity.ok(users);
    }

    /**
     * Check if user exists
     */
    @GetMapping("/{userId}/exists")
    @Operation(summary = "Check if user exists", description = "Check if a user exists by their ID")
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
        log.info("GET /api/users/{}/exists", userId);
        
        boolean exists = userService.userExists(userId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Check if user exists by email
     */
    @GetMapping("/email/{email}/exists")
    @Operation(summary = "Check if user exists by email", description = "Check if a user exists by their email")
    public ResponseEntity<Boolean> userExistsByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{}/exists", email);
        
        boolean exists = userService.userExistsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Returns statistics about users (total, deleted, recent)")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        log.info("GET /api/users/stats");
        
        UserStatsDTO stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }
}
