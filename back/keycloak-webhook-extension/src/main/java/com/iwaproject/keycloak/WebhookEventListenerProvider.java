package com.iwaproject.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Keycloak Event Listener that calls a webhook when users register
 * This automatically syncs new users to the microservice database
 */
public class WebhookEventListenerProvider implements EventListenerProvider {

    private static final Logger LOG = Logger.getLogger(WebhookEventListenerProvider.class);
    
    private final KeycloakSession session;
    private final String webhookUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookEventListenerProvider(KeycloakSession session, String webhookUrl) {
        this.session = session;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void onEvent(Event event) {
        // Only handle user registration events
        if (event.getType() == EventType.REGISTER) {
            LOG.infof("User registration event detected: userId=%s", event.getUserId());
            
            try {
                UserModel user = session.users().getUserById(
                    session.getContext().getRealm(), 
                    event.getUserId()
                );
                
                if (user != null) {
                    sendWebhook(user);
                } else {
                    LOG.warnf("User not found for registration event: userId=%s", event.getUserId());
                }
            } catch (Exception e) {
                LOG.errorf(e, "Failed to process registration event for userId=%s", event.getUserId());
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // We don't need to handle admin events for this use case
    }

    @Override
    public void close() {
        // Cleanup if needed
    }

    /**
     * Send user data to the microservice webhook
     */
    private void sendWebhook(UserModel user) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(webhookUrl);
            
            // Prepare user data
            Map<String, String> userData = new HashMap<>();
            userData.put("userId", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail() != null ? user.getEmail() : "");
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            
            // Convert to JSON
            String json = objectMapper.writeValueAsString(userData);
            
            LOG.infof("Sending webhook for user: %s to %s", user.getUsername(), webhookUrl);
            LOG.debugf("Webhook payload: %s", json);
            
            // Set request body
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            request.setHeader("Content-Type", "application/json");
            
            // Execute request
            httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    LOG.infof("✅ Successfully sent webhook for user: %s (HTTP %d)", 
                        user.getUsername(), statusCode);
                } else {
                    LOG.errorf("❌ Webhook failed for user: %s (HTTP %d)", 
                        user.getUsername(), statusCode);
                }
                return null;
            });
            
        } catch (Exception e) {
            LOG.errorf(e, "Error sending webhook for user: %s", user.getUsername());
        }
    }
}
