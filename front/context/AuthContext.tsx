import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { KEYCLOAK_CONFIG } from '@/config/keycloak';

// Enable web browser warming for better UX
WebBrowser.maybeCompleteAuthSession();

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  userInfo: UserInfo | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;
}

interface UserInfo {
  sub: string;
  email?: string;
  name?: string;
  preferred_username?: string;
  given_name?: string;
  family_name?: string;
}

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  id_token: string;
  expires_in: number;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEYS = {
  ACCESS_TOKEN: '@auth/access_token',
  REFRESH_TOKEN: '@auth/refresh_token',
  USER_INFO: '@auth/user_info',
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);

  const discovery = {
    authorizationEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/auth`,
    tokenEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/token`,
    revocationEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/logout`,
  };

  // Load stored tokens on mount
  useEffect(() => {
    loadStoredAuth();
  }, []);

  const loadStoredAuth = async () => {
    try {
      const [storedAccessToken, storedRefreshToken, storedUserInfo] = await Promise.all([
        AsyncStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN),
        AsyncStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN),
        AsyncStorage.getItem(STORAGE_KEYS.USER_INFO),
      ]);

      if (storedAccessToken && storedRefreshToken) {
        setAccessToken(storedAccessToken);
        setRefreshToken(storedRefreshToken);
        setUserInfo(storedUserInfo ? JSON.parse(storedUserInfo) : null);
        setIsAuthenticated(true);
      }
    } catch (error) {
      console.error('Failed to load stored auth:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const storeTokens = async (access: string, refresh: string, info: UserInfo) => {
    try {
      await Promise.all([
        AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, access),
        AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refresh),
        AsyncStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info)),
      ]);
    } catch (error) {
      console.error('Failed to store tokens:', error);
    }
  };

  const clearTokens = async () => {
    try {
      await Promise.all([
        AsyncStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.USER_INFO),
      ]);
    } catch (error) {
      console.error('Failed to clear tokens:', error);
    }
  };

  const parseJwt = (token: string): UserInfo => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Failed to parse JWT:', error);
      throw error;
    }
  };

  const login = async () => {
    try {
      setIsLoading(true);

      // Create authorization request
      const authRequest = new AuthSession.AuthRequest({
        clientId: KEYCLOAK_CONFIG.clientId,
        redirectUri: KEYCLOAK_CONFIG.redirectUri,
        scopes: KEYCLOAK_CONFIG.scopes,
        responseType: AuthSession.ResponseType.Code,
        usePKCE: true,
      });

      // Prompt user to authenticate
      const result = await authRequest.promptAsync(discovery);

      if (result.type === 'success' && result.params.code) {
        // Exchange authorization code for tokens
        const tokenResult = await AuthSession.exchangeCodeAsync(
          {
            clientId: KEYCLOAK_CONFIG.clientId,
            code: result.params.code,
            redirectUri: KEYCLOAK_CONFIG.redirectUri,
            extraParams: {
              code_verifier: authRequest.codeVerifier || '',
            },
          },
          discovery
        );

        if (tokenResult.accessToken) {
          const info = parseJwt(tokenResult.accessToken);
          
          setAccessToken(tokenResult.accessToken);
          setRefreshToken(tokenResult.refreshToken || '');
          setUserInfo(info);
          setIsAuthenticated(true);

          await storeTokens(
            tokenResult.accessToken,
            tokenResult.refreshToken || '',
            info
          );
        }
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const refreshAccessToken = async () => {
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await fetch(discovery.tokenEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: KEYCLOAK_CONFIG.clientId,
          refresh_token: refreshToken,
        }).toString(),
      });

      if (!response.ok) {
        throw new Error('Failed to refresh token');
      }

      const data: TokenResponse = await response.json();
      const info = parseJwt(data.access_token);

      setAccessToken(data.access_token);
      setRefreshToken(data.refresh_token);
      setUserInfo(info);

      await storeTokens(data.access_token, data.refresh_token, info);
    } catch (error) {
      console.error('Token refresh failed:', error);
      await logout();
      throw error;
    }
  };

  const logout = async () => {
    try {
      setIsLoading(true);
      
      // Revoke tokens with Keycloak
      if (refreshToken) {
        try {
          await fetch(discovery.revocationEndpoint, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
              client_id: KEYCLOAK_CONFIG.clientId,
              refresh_token: refreshToken,
            }).toString(),
          });
        } catch (error) {
          console.error('Failed to revoke token:', error);
        }
      }

      // Clear local state
      setAccessToken(null);
      setRefreshToken(null);
      setUserInfo(null);
      setIsAuthenticated(false);
      await clearTokens();
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        accessToken,
        refreshToken,
        userInfo,
        login,
        logout,
        refreshAccessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
