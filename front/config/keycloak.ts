// Keycloak configuration for React Native app
export const KEYCLOAK_CONFIG = {
  // Keycloak server URL
  issuer: 'http://localhost:8080/realms/IWA_NextLevel',
  
  // Client ID configured in Keycloak
  clientId: 'user-microservice',
  
  // Redirect URI after successful authentication
  // For Expo Go: use expo scheme
  // For production: use your custom scheme
  redirectUri: 'exp://localhost:8081',  // Adjust for your Expo setup
  
  // Scopes requested during authentication
  scopes: ['openid', 'profile', 'email'],
  
  // Discovery document URL (Keycloak auto-discovery)
  discoveryUrl: 'http://localhost:8080/realms/IWA_NextLevel/.well-known/openid-configuration',
};

// Backend API configuration
export const API_CONFIG = {
  baseUrl: 'http://localhost:8081',
  endpoints: {
    profile: '/api/users/profile',
    publicProfile: (userId: string) => `/api/users/${userId}`,
  },
};
