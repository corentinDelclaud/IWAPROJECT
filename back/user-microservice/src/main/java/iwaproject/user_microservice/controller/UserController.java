package iwaproject.user_microservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iwaproject.user_microservice.dto.UpdateProfileDTO;
import iwaproject.user_microservice.dto.UserProfileDTO;
import iwaproject.user_microservice.dto.UserPublicDTO;
import iwaproject.user_microservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
        log.info("GET /api/users/profile - User: {}", userId);
        
        UserProfileDTO profile = userService.getUserProfile(userId);
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
}
