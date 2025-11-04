import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_CONFIG } from '@/config/keycloak';

interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
  updatedAt: string;
}

class ApiService {
  private baseUrl: string;

  constructor() {
    this.baseUrl = API_CONFIG.baseUrl;
  }

  private async getAccessToken(): Promise<string | null> {
    try {
      // Keep in sync with STORAGE_KEYS.ACCESS_TOKEN in AuthContext
      return await AsyncStorage.getItem('@auth/access_token');
    } catch (error) {
      console.error('Error getting access token:', error);
      return null;
    }
  }

  private async makeRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const accessToken = await this.getAccessToken();

      const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const url = `${this.baseUrl}${endpoint}`;
    
    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Token expired or invalid - should trigger re-authentication
          throw new Error('UNAUTHORIZED');
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
