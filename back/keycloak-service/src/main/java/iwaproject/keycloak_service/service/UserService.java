package iwaproject.keycloak_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iwaproject.keycloak_service.dto.UserDto;
import iwaproject.keycloak_service.exception.KeycloakServiceException;

@Service
@Transactional
public class UserService {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    public UserDto registerUser(UserDto userDto) {
        try {
            // Logic to register a user in Keycloak
            // This may involve calling the Keycloak admin service to create a user
            return keycloakAdminService.createUser(userDto);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to register user", e);
        }
    }

    public UserDto getUserById(String userId) {
        try {
            // Logic to retrieve a user by ID from Keycloak
            return keycloakAdminService.getUserById(userId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to retrieve user", e);
        }
    }

    public void deleteUser(String userId) {
        try {
            // Logic to delete a user from Keycloak
            keycloakAdminService.deleteUser(userId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to delete user", e);
        }
    }

    // Additional user management methods can be added here
}