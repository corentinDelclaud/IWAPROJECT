### GET /websites/keycloak/clients/{id}/client-secret

Source: https://www.keycloak.org/docs/-api/26.4.0/javadocs/org/keycloak/services/resources/admin/ClientResource

Retrieve the client secret for a specific client. Use this to get the current secret associated with the client.

```APIDOC
## GET /clients/{id}/client-secret

### Description
Get the client secret.

### Method
GET

### Endpoint
/clients/{id}/client-secret

### Parameters
#### Path Parameters
- **id** (string) - Required - The unique identifier of the client.

### Response
#### Success Response (200)
- **CredentialRepresentation** (object) - The client's credential representation, including the secret.

#### Response Example
```json
{
  "type": "secret",
  "value": "your_current_client_secret"
}
```
```

--------------------------------

### Get Client Secret

Source: https://www.keycloak.org/docs/-api/latest/javadocs/org/keycloak/services/resources/admin/ClientResource

Retrieves the current client secret.

```APIDOC
## GET /websites/keycloak/clients/{id}/client-secret

### Description
Retrieves the current client secret.

### Method
GET

### Endpoint
`/websites/keycloak/clients/{id}/client-secret`

### Parameters
#### Path Parameters
- **id** (string) - Required - The ID of the client whose secret to retrieve.

### Response
#### Success Response (200)
- **CredentialRepresentation** (object) - The current client secret details.

#### Response Example
```json
{
  "value": "current-client-secret",
  "type": "secret"
}
```

#### Error Response (404)
- **404**: Client not found.
```

