import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { AppState } from 'react-native';
import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { KEYCLOAK_CONFIG } from '@/config/keycloak';

// Enable web browser warming
WebBrowser.maybeCompleteAuthSession();

// Storage keys
const STORAGE_KEYS = {
  ACCESS_TOKEN: '@auth/access_token',
  REFRESH_TOKEN: '@auth/refresh_token',
  USER_INFO: '@auth/user_info',
};

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  userInfo: UserInfo | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  // accept an optional refresh token to allow refreshing before state is settled
  refreshAccessToken: (refreshTokenArg?: string) => Promise<void>;
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);

  const discovery = {
    authorizationEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/auth`,
    tokenEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/token`,
    revocationEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/revoke`,
    endSessionEndpoint: `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/logout`,
  };

  // Load stored tokens on mount
  useEffect(() => {
    loadStoredAuth();
  }, []);

  // Refresh tokens when the app becomes active again (e.g. user switches back after a long time)
  useEffect(() => {
    const subscription = AppState.addEventListener('change', async (nextAppState) => {
      if (nextAppState === 'active') {
        try {
          // If we have an access token, check expiry and refresh if it's close to expiring
          if (accessToken) {
            try {
              const payload: any = parseJwt(accessToken);
              const willExpireSoon = payload?.exp && payload.exp * 1000 <= Date.now() + 60_000;
              if (willExpireSoon && refreshToken) {
                console.log('[Auth] App resumed: access token will expire soon, refreshing...');
                await refreshAccessToken(refreshToken);
                setIsAuthenticated(true);
              }
            } catch (err) {
              // parsing failed -> try refreshing if we have a refresh token
              if (refreshToken) {
                console.log('[Auth] App resumed: access token parse failed, attempting refresh');
                await refreshAccessToken(refreshToken);
                setIsAuthenticated(true);
              }
            }
          } else if (refreshToken) {
            // No access token but we have a refresh token: try to refresh to restore session
            console.log('[Auth] App resumed: no access token but have refresh token, attempting refresh');
            await refreshAccessToken(refreshToken);
            setIsAuthenticated(true);
          }
        } catch (error) {
          console.warn('[Auth] Token refresh on resume failed, logging out', error);
          await logout();
        }
      }
    });

    return () => subscription.remove();
  }, [accessToken, refreshToken]);

  const loadStoredAuth = async () => {
    try {
      const [storedAccessToken, storedRefreshToken, storedUserInfo] = await Promise.all([
        AsyncStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN),
        AsyncStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN),
        AsyncStorage.getItem(STORAGE_KEYS.USER_INFO),
      ]);

      // Ensure refresh token is stored in state for possible refresh attempts
      if (storedRefreshToken) {
        setRefreshToken(storedRefreshToken);
      }

      // If we have an access token, validate its expiry first
      if (storedAccessToken) {
        try {
          const payload: any = parseJwt(storedAccessToken);
          const isValid = payload?.exp && payload.exp * 1000 > Date.now() + 60_000;

          if (isValid) {
            setAccessToken(storedAccessToken);
            setUserInfo(storedUserInfo ? JSON.parse(storedUserInfo) : (payload || null));
            setIsAuthenticated(true);
            return;
          }

          // Access token expired or will expire soon -> try to refresh using refresh token
          if (storedRefreshToken) {
            try {
              await refreshAccessToken(storedRefreshToken);
              setIsAuthenticated(true);
              return;
            } catch (err) {
              console.warn('[Auth] Refresh during load failed:', err);
              await logout();
              return;
            }
          }
        } catch (err) {
          // Parsing failed - try to recover using refresh token
          if (storedRefreshToken) {
            try {
              await refreshAccessToken(storedRefreshToken);
              setIsAuthenticated(true);
              return;
            } catch (e) {
              console.warn('[Auth] Refresh after parse failure failed:', e);
              await logout();
              return;
            }
          }
        }
      } else if (storedRefreshToken) {
        // No access token but we have a refresh token -> try to refresh
        try {
          await refreshAccessToken(storedRefreshToken);
          setIsAuthenticated(true);
          return;
        } catch (err) {
          console.warn('[Auth] Refresh with stored refresh token failed:', err);
          await logout();
          return;
        }
      }
    } catch (error) {
      console.error('Failed to load stored auth:', error);
    } finally {
      setIsLoading(false);
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

      const redirectUri = AuthSession.makeRedirectUri({
        scheme: 'exp',
        path: 'auth'
      });
      
      console.log('[Auth] Redirect URI:', redirectUri);
      console.log('[Auth] Authorization Endpoint:', discovery.authorizationEndpoint);

      const authRequest = new AuthSession.AuthRequest({
        clientId: KEYCLOAK_CONFIG.clientId,
        redirectUri,
        scopes: KEYCLOAK_CONFIG.scopes,
        responseType: AuthSession.ResponseType.Code,
        usePKCE: true,
      });

      const result = await authRequest.promptAsync(discovery);

      if (result.type === 'success' && result.params.code) {
        const tokenResult = await AuthSession.exchangeCodeAsync(
          {
            clientId: KEYCLOAK_CONFIG.clientId,
            code: result.params.code,
            redirectUri,
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

          await Promise.all([
            AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, tokenResult.accessToken),
            AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, tokenResult.refreshToken || ''),
            AsyncStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info)),
          ]);
        }
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const refreshAccessToken = async (refreshTokenArg?: string) => {
    const tokenToUse = refreshTokenArg ?? refreshToken;
    if (!tokenToUse) throw new Error('No refresh token available');

    try {
      const response = await fetch(discovery.tokenEndpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: KEYCLOAK_CONFIG.clientId,
          refresh_token: tokenToUse,
        }).toString(),
      });

      if (!response.ok) throw new Error('Failed to refresh token');

      const data: TokenResponse = await response.json();
      const info = parseJwt(data.access_token);

      setAccessToken(data.access_token);
      setRefreshToken(data.refresh_token);
      setUserInfo(info);

      await Promise.all([
        AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.access_token),
        AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refresh_token),
        AsyncStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info)),
      ]);
    } catch (error) {
      console.error('Token refresh failed:', error);
      await logout();
      throw error;
    }
  };

  const logout = async () => {
    try {
      console.log('[Auth] Starting logout...');
      setIsLoading(true);
      
      if (refreshToken) {
        try {
          console.log('[Auth] Revoking refresh token with Keycloak revoke endpoint...');
          // Use token revocation endpoint to revoke the refresh token
          await fetch(discovery.revocationEndpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
              token: refreshToken,
              token_type_hint: 'refresh_token',
              client_id: KEYCLOAK_CONFIG.clientId,
            }).toString(),
          });
        } catch (err) {
          console.warn('[Auth] Refresh token revocation failed, attempting end-session logout as fallback', err);
          // Fallback: call end-session endpoint to try to remove server-side session
          try {
            await fetch(discovery.endSessionEndpoint, {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: new URLSearchParams({
                client_id: KEYCLOAK_CONFIG.clientId,
                refresh_token: refreshToken,
              }).toString(),
            });
          } catch (e) {
            console.error('[Auth] End-session fallback failed:', e);
          }
        }
      }

      console.log('[Auth] Clearing local state...');
      setAccessToken(null);
      setRefreshToken(null);
      setUserInfo(null);
      setIsAuthenticated(false);
      
      console.log('[Auth] Removing stored tokens...');
      await Promise.all([
        AsyncStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.USER_INFO),
      ]);
      
      console.log('[Auth] Logout complete! isAuthenticated is now false');
    } catch (error) {
      console.error('[Auth] Logout failed:', error);
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
