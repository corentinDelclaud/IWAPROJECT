package com.iwaproject.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for creating WebhookEventListenerProvider instances
 */
public class WebhookEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOG = Logger.getLogger(WebhookEventListenerProviderFactory.class);
    private static final String PROVIDER_ID = "webhook-event-listener";
    
    private String webhookUrl;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new WebhookEventListenerProvider(session, webhookUrl);
    }

    @Override
    public void init(Config.Scope config) {
        // Get webhook URL from configuration
        // Use host.docker.internal to reach host machine from Docker container
        webhookUrl = config.get("webhookUrl", "http://host.docker.internal:8081/api/webhooks/keycloak/user-registered");
        
        LOG.infof("🚀 Webhook Event Listener initialized");
        LOG.infof("   Webhook URL: %s", webhookUrl);
        LOG.infof("   This extension will call the webhook when users register");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Not needed
    }

    @Override
    public void close() {
        // Cleanup if needed
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
