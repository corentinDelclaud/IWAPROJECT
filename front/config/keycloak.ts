import { buildUrl } from '@/utils/network';

// Keycloak configuration for React Native app
export const KEYCLOAK_CONFIG = {
  // Keycloak server URL
  get issuer() {
    return `${buildUrl(8085)}/realms/IWA_NextLevel`;
  },
  
  // Client ID configured in Keycloak
  clientId: 'auth-service',
  
  // Scopes requested during authentication
  scopes: ['openid', 'profile', 'email'],
  
  // Discovery document URL
  get discoveryUrl() {
    return `${buildUrl(8085)}/realms/IWA_NextLevel/.well-known/openid-configuration`;
  },
};

// Backend API configuration
export const API_CONFIG = {
  get baseUrl() {
    return buildUrl(8081);
  },
  endpoints: {
    profile: '/api/users/profile',
    publicProfile: (userId: string) => `/api/users/${userId}`,
  },
};
