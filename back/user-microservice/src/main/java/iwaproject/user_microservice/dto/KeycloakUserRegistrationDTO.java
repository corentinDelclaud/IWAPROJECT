package iwaproject.user_microservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user registration data from Keycloak webhook
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserRegistrationDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String firstName;

    private String lastName;
}
