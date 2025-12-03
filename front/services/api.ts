import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_CONFIG, KEYCLOAK_CONFIG } from '@/config/keycloak';

interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  // optional stripe connected account id returned by backend
  stripeAccountId?: string | null;
  createdAt: string;
  updatedAt: string;
}

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  id_token: string;
  expires_in: number;
}

class ApiService {
  private baseUrl: string;
  private isRefreshing = false;
  private refreshSubscribers: Array<(token: string) => void> = [];

  constructor() {
    this.baseUrl = API_CONFIG.baseUrl;
  }

  private async getAccessToken(): Promise<string | null> {
    try {
      // Keep in sync with STORAGE_KEYS.ACCESS_TOKEN in AuthContext
      const token = await AsyncStorage.getItem('@auth/access_token');
      if (token) {
        console.log('[API] Access token retrieved from storage');
      } else {
        console.warn('[API] No access token found in storage');
      }
      return token;
    } catch (error) {
      console.error('Error getting access token:', error);
      return null;
    }
  }

  private async getRefreshToken(): Promise<string | null> {
    try {
      return await AsyncStorage.getItem('@auth/refresh_token');
    } catch (error) {
      console.error('Error getting refresh token:', error);
      return null;
    }
  }

  private parseJwt(token: string): any {
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
      return null;
    }
  }

  private async refreshAccessToken(): Promise<string> {
    const refreshToken = await this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    console.log('[API] Refreshing access token...');

    const tokenEndpoint = `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/token`;
    const response = await fetch(tokenEndpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'refresh_token',
        client_id: KEYCLOAK_CONFIG.clientId,
        refresh_token: refreshToken,
      }).toString(),
    });

    if (!response.ok) {
      console.error('[API] Token refresh failed');
      throw new Error('Failed to refresh token');
    }

    const data: TokenResponse = await response.json();
    const userInfo = this.parseJwt(data.access_token);

    // Store new tokens
    await Promise.all([
      AsyncStorage.setItem('@auth/access_token', data.access_token),
      AsyncStorage.setItem('@auth/refresh_token', data.refresh_token),
      AsyncStorage.setItem('@auth/user_info', JSON.stringify(userInfo)),
    ]);

    console.log('[API] Access token refreshed successfully');
    return data.access_token;
  }

  private async makeRequest<T>(
    endpoint: string,
    options: RequestInit = {},
    retry = true
  ): Promise<T> {
    const accessToken = await this.getAccessToken();

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`;
      console.log('[API] Making authenticated request to:', endpoint);
      console.log('[API] Token present:', accessToken.substring(0, 20) + '...');
    } else {
      console.warn('[API] No access token available for:', endpoint);
    }

    const url = `${this.baseUrl}${endpoint}`;
    console.log('[API] Full URL:', url);
    
    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (!response.ok) {
        if (response.status === 401 && retry) {
          // Token expired - try to refresh
          console.log('[API] Got 401, attempting to refresh token...');
          try {
            await this.refreshAccessToken();
            // Retry the request with the new token
            return this.makeRequest<T>(endpoint, options, false);
          } catch (refreshError) {
            console.error('[API] Token refresh failed:', refreshError);
            throw new Error('UNAUTHORIZED');
          }
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }

      return {} as T;
    } catch (error) {
      console.error(`API request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  async getUserProfile(): Promise<UserProfile> {
    return this.makeRequest<UserProfile>(API_CONFIG.endpoints.profile);
  }

  async getStripeAccountStatus(accountId: string): Promise<any> {
    if (!accountId) throw new Error('AccountId is required');
    const endpoint = `/api/stripe/account-status/${encodeURIComponent(accountId)}`;
    return this.makeRequest<any>(endpoint);
  }

  async createStripeAccountLink(accountId: string, redirectUrl?: string): Promise<{ url: string }> {
    if (!accountId) throw new Error('AccountId is required');
    const body: { accountId: string; redirectUrl?: string } = { accountId };
    if (redirectUrl) {
      body.redirectUrl = redirectUrl;
    }
    return this.makeRequest<{ url: string }>(`/api/stripe/account-link`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  async createConnectAccount(email: string): Promise<{ accountId: string }> {
    if (!email) throw new Error('Email is required');
    return this.makeRequest<{ accountId: string }>(`/api/stripe/connect-account`, {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  }

  async getPublicProfile(userId: string): Promise<UserProfile> {
    return this.makeRequest<UserProfile>(
      API_CONFIG.endpoints.publicProfile(userId)
    );
  }

  async updateProfile(data: Partial<UserProfile>): Promise<UserProfile> {
    return this.makeRequest<UserProfile>(API_CONFIG.endpoints.profile, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async deleteProfile(): Promise<void> {
    return this.makeRequest<void>(API_CONFIG.endpoints.profile, {
      method: 'DELETE',
    });
  }
}

export const apiService = new ApiService();
export type { UserProfile };
