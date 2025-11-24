package iwaproject.user_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user data from Keycloak webhook events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserWebhookDTO {
    private String id; // Keycloak user ID
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;
    private Long createdTimestamp;
}
