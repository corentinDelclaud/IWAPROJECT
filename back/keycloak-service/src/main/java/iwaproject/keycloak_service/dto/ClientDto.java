package iwaproject.keycloak_service.dto;

public class ClientDto {
    private String clientId;
    private String name;
    private String description;
    private boolean enabled;

    public ClientDto() {
    }

    public ClientDto(String clientId, String name, String description, boolean enabled) {
        this.clientId = clientId;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}