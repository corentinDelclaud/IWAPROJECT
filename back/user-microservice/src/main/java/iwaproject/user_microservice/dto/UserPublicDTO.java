package iwaproject.user_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public user profile (visible to other users)
 * Does not include sensitive information like email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
}
