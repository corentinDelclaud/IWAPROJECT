package iwaproject.keycloak_service.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class KeycloakAdminService {

    private Keycloak keycloak;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @PostConstruct
    public void init() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();
    }

    public List<RealmRepresentation> getRealms() {
        return keycloak.realms().findAll();
    }

    public UserRepresentation getUser(String userId) {
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

    public void createUser(UserRepresentation user) {
        keycloak.realm(realm).users().create(user);
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().delete(userId);
    }

    // Additional methods for managing realms and users can be added here
}