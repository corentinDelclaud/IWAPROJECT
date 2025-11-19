package iwaproject.keycloak.webhook;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for creating WebhookEventListenerProvider instances.
 */
public class WebhookEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(WebhookEventListenerProviderFactory.class);
    private static final String PROVIDER_ID = "user-webhook-sync";
    
    private String webhookUrl;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new WebhookEventListenerProvider(session, webhookUrl);
    }

    @Override
    public void init(Config.Scope config) {
        // Read webhook URL from configuration
        // Default to user-microservice container name in Docker
        webhookUrl = config.get("webhookUrl", "http://user-microservice:8081");
        logger.infof("WebhookEventListenerProviderFactory initialized with webhookUrl: %s", webhookUrl);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Post-initialization logic if needed
    }

    @Override
    public void close() {
        // Cleanup logic if needed
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
