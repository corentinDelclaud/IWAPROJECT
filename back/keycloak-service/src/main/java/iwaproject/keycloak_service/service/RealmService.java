package iwaproject.keycloak_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iwaproject.keycloak_service.dto.RealmDto;
import iwaproject.keycloak_service.exception.KeycloakServiceException;

@Service
@Transactional
public class RealmService {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    public RealmDto createRealm(RealmDto realmDto) {
        try {
            // Logic to create a realm using KeycloakAdminService
            return keycloakAdminService.createRealm(realmDto);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to create realm", e);
        }
    }

    public RealmDto updateRealm(String realmId, RealmDto realmDto) {
        try {
            // Logic to update a realm using KeycloakAdminService
            return keycloakAdminService.updateRealm(realmId, realmDto);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to update realm", e);
        }
    }

    public void deleteRealm(String realmId) {
        try {
            // Logic to delete a realm using KeycloakAdminService
            keycloakAdminService.deleteRealm(realmId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to delete realm", e);
        }
    }

    public RealmDto getRealm(String realmId) {
        try {
            // Logic to retrieve a realm using KeycloakAdminService
            return keycloakAdminService.getRealm(realmId);
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to retrieve realm", e);
        }
    }
}