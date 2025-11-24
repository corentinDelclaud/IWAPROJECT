package iwaproject.keycloak.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Keycloak Event Listener that sends webhook notifications to the user-microservice
 * when users are created, updated, or deleted in Keycloak.
 */
public class WebhookEventListenerProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(WebhookEventListenerProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final KeycloakSession session;
    private final String webhookUrl;

    public WebhookEventListenerProvider(KeycloakSession session, String webhookUrl) {
        this.session = session;
        this.webhookUrl = webhookUrl;
        logger.infof("WebhookEventListenerProvider initialized with URL: %s", webhookUrl);
    }

    @Override
    public void onEvent(Event event) {
        // Handle user login/register events if needed
        if (event.getType() == EventType.REGISTER) {
            logger.infof("User registered: %s", event.getUserId());
            // User creation is handled by AdminEvent
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Only process USER resource events
        if (adminEvent.getResourceType() != ResourceType.USER) {
            return;
        }

        String userId = extractUserId(adminEvent.getResourcePath());
        if (userId == null) {
            logger.warn("Could not extract user ID from resource path: " + adminEvent.getResourcePath());
            return;
        }

        OperationType operation = adminEvent.getOperationType();
        logger.infof("Admin event: %s on user %s", operation, userId);

        try {
            switch (operation) {
                case CREATE:
                    handleUserCreate(userId);
                    break;
                case UPDATE:
                    handleUserUpdate(userId);
                    break;
                case DELETE:
                    handleUserDelete(userId);
                    break;
                default:
                    logger.debugf("Ignoring operation: %s", operation);
            }
        } catch (Exception e) {
            logger.error("Error processing admin event for user " + userId, e);
        }
    }

    private void handleUserCreate(String userId) {
        logger.infof("Handling user creation for: %s", userId);
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
        if (user != null) {
            Map<String, Object> userData = buildUserData(user);
            sendWebhook("POST", webhookUrl + "/api/webhooks/users", userData);
        }
    }

    private void handleUserUpdate(String userId) {
        logger.infof("Handling user update for: %s", userId);
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
        if (user != null) {
            Map<String, Object> userData = buildUserData(user);
            sendWebhook("PUT", webhookUrl + "/api/webhooks/users/" + userId, userData);
        }
    }

    private void handleUserDelete(String userId) {
        logger.infof("Handling user deletion for: %s", userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", userId);
        sendWebhook("DELETE", webhookUrl + "/api/webhooks/users/" + userId, data);
    }

    private Map<String, Object> buildUserData(UserModel user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("enabled", user.isEnabled());
        userData.put("emailVerified", user.isEmailVerified());
        userData.put("createdTimestamp", user.getCreatedTimestamp());
        return userData;
    }

    private void sendWebhook(String method, String url, Map<String, Object> data) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String jsonPayload = objectMapper.writeValueAsString(data);
            logger.infof("Sending %s request to %s with payload: %s", method, url, jsonPayload);

            switch (method) {
                case "POST":
                    HttpPost postRequest = new HttpPost(url);
                    postRequest.setEntity(new StringEntity(jsonPayload));
                    postRequest.setHeader("Content-Type", "application/json");
                    httpClient.execute(postRequest, response -> {
                        logger.infof("POST response: %d", response.getCode());
                        return null;
                    });
                    break;

                case "PUT":
                    HttpPut putRequest = new HttpPut(url);
                    putRequest.setEntity(new StringEntity(jsonPayload));
                    putRequest.setHeader("Content-Type", "application/json");
                    httpClient.execute(putRequest, response -> {
                        logger.infof("PUT response: %d", response.getCode());
                        return null;
                    });
                    break;

                case "DELETE":
                    HttpDelete deleteRequest = new HttpDelete(url);
                    httpClient.execute(deleteRequest, response -> {
                        logger.infof("DELETE response: %d", response.getCode());
                        return null;
                    });
                    break;
            }
        } catch (Exception e) {
            logger.error("Error sending webhook to " + url, e);
        }
    }

    private String extractUserId(String resourcePath) {
        // Resource path format: "users/{userId}" or "users/{userId}/..."
        if (resourcePath == null || !resourcePath.startsWith("users/")) {
            return null;
        }

        String[] parts = resourcePath.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }

        return null;
    }

    @Override
    public void close() {
        // Cleanup if needed
    }
}
