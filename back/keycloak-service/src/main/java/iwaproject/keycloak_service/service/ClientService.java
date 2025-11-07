package iwaproject.keycloak_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iwaproject.keycloak_service.dto.ClientDto;
import iwaproject.keycloak_service.exception.KeycloakServiceException;

@Service
@Transactional
public class ClientService {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    public ClientDto createClient(ClientDto clientDto) {
        try {
            // Logic to create a client in Keycloak
            return keycloakAdminService.createClient(clientDto);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to create client", e);
        }
    }

    public ClientDto updateClient(String clientId, ClientDto clientDto) {
        try {
            // Logic to update a client in Keycloak
            return keycloakAdminService.updateClient(clientId, clientDto);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to update client", e);
        }
    }

    public void deleteClient(String clientId) {
        try {
            // Logic to delete a client in Keycloak
            keycloakAdminService.deleteClient(clientId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to delete client", e);
        }
    }

    public ClientDto getClient(String clientId) {
        try {
            // Logic to retrieve a client from Keycloak
            return keycloakAdminService.getClient(clientId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to retrieve client", e);
        }
    }
}