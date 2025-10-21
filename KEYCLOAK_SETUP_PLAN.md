# Keycloak Authentication Setup Plan

## ✅ Completed - Cleanup Phase

The following items have been cleaned up:

1. **AuthContext cleaned** - Removed all Keycloak implementation, kept interface structure
2. **Login screen sanitized** - Removed Keycloak-specific text and references
3. **Config file reset** - `keycloak.ts` configuration cleared
4. **Expo port configured** - All scripts now use port 19000 explicitly

## 📋 Overview

This plan outlines the steps to set up Keycloak authentication for your IWA mobile application.

**Architecture:**
- **Frontend**: React Native (Expo) mobile app
- **Backend**: Spring Boot microservice (port 8081)
- **Keycloak**: Authentication server (port 8080)
- **Expo Dev Server**: Port 19000 (configured)

---

## 🔧 Phase 1: Keycloak Server Setup

### 1.1 Start Keycloak Server

**Location**: `/home/etienne/Documents/IWAPROJECT/back/user-microservice/`

```bash
cd back/user-microservice
docker-compose -f docker-compose.keycloak.yml up -d
```

**Verify:** Access http://localhost:8080
- Username: `admin`
- Password: `admin`

### 1.2 Configure Keycloak Realm

Your realm configuration already exists at `keycloak-config/realm-export.json`.

**Realm Details:**
- Realm Name: `IWA_NextLevel`
- Existing Client: `user-microservice`

**Required Configuration:**

1. **Access Keycloak Admin Console**: http://localhost:8080/admin
2. **Navigate to**: Realms → IWA_NextLevel → Clients → user-microservice
3. **Update Client Settings**:
   ```
   Client ID: user-microservice
   Client Protocol: openid-connect
   Access Type: public (for mobile apps)
   Valid Redirect URIs: 
     - exp://192.168.*.*/--/*
     - exp://localhost:19000/--/*
     - http://localhost:19000/*
     - myapp://* (if using custom scheme)
   Web Origins: *
   Standard Flow Enabled: Yes
   Direct Access Grants Enabled: Yes
   ```

4. **Create Test Users** (if not already exist):
   - Navigate to: Users → Add User
   - Set username, email
   - Go to Credentials tab → Set password → Disable temporary

---

## 🔧 Phase 2: Backend Configuration

### 2.1 Verify Backend Settings

**File**: `back/user-microservice/src/main/resources/application.properties`

Already configured ✅:
```properties
server.port=8081
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/IWA_NextLevel
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/certs
```

### 2.2 Start Backend Service

```bash
cd back/user-microservice
./mvnw spring-boot:run
```

**Verify:** Backend should be running on http://localhost:8081

---

## 🔧 Phase 3: Frontend Configuration

### 3.1 Update Keycloak Configuration

**File**: `front/config/keycloak.ts`

```typescript
import { buildUrl } from '@/utils/network';

// Keycloak configuration for React Native app
export const KEYCLOAK_CONFIG = {
  // Keycloak server URL
  get issuer() {
    return `${buildUrl(8080)}/realms/IWA_NextLevel`;
  },
  
  // Client ID configured in Keycloak
  clientId: 'user-microservice',
  
  // Scopes requested during authentication
  scopes: ['openid', 'profile', 'email'],
  
  // Discovery document URL
  get discoveryUrl() {
    return `${buildUrl(8080)}/realms/IWA_NextLevel/.well-known/openid-configuration`;
  },
};

// Backend API configuration (already correct)
export const API_CONFIG = {
  get baseUrl() {
    return buildUrl(8081);
  },
  endpoints: {
    profile: '/api/users/profile',
    publicProfile: (userId: string) => `/api/users/${userId}`,
  },
};
```

### 3.2 Implement Authentication in AuthContext

**File**: `front/context/AuthContext.tsx`

Add the following imports and implementation:

```typescript
import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
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

// ... (keep existing interfaces)

export function AuthProvider({ children }: { children: ReactNode }) {
  // ... (keep existing state)
  
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

  const refreshAccessToken = async () => {
    if (!refreshToken) throw new Error('No refresh token available');

    try {
      const response = await fetch(discovery.tokenEndpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: KEYCLOAK_CONFIG.clientId,
          refresh_token: refreshToken,
        }).toString(),
      });

      if (!response.ok) throw new Error('Failed to refresh token');

      const data = await response.json();
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
      setIsLoading(true);
      
      if (refreshToken) {
        await fetch(discovery.revocationEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({
            client_id: KEYCLOAK_CONFIG.clientId,
            refresh_token: refreshToken,
          }).toString(),
        }).catch(console.error);
      }

      setAccessToken(null);
      setRefreshToken(null);
      setUserInfo(null);
      setIsAuthenticated(false);
      
      await Promise.all([
        AsyncStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN),
        AsyncStorage.removeItem(STORAGE_KEYS.USER_INFO),
      ]);
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // ... rest of provider
}
```

### 3.3 Update app.json

**File**: `front/app.json`

Add the custom scheme for deep linking:

```json
{
  "expo": {
    "scheme": "myapp",
    // ... rest of config
  }
}
```

### 3.4 Update Login Screen

**File**: `front/app/login.tsx`

Update the text to be more descriptive:

```typescript
<Text style={styles.subtitle}>
  Sign in with your Keycloak account to continue
</Text>

<Text style={styles.loginButtonText}>Sign In with Keycloak</Text>

<Text style={styles.helpText}>
  You will be redirected to Keycloak for secure authentication
</Text>
```

---

## 🧪 Phase 4: Testing

### 4.1 Test Sequence

1. **Start all services:**
   ```bash
   # Terminal 1 - Keycloak
   cd back/user-microservice
   docker-compose -f docker-compose.keycloak.yml up

   # Terminal 2 - Backend
   cd back/user-microservice
   ./mvnw spring-boot:run

   # Terminal 3 - Frontend (Expo)
   cd front
   npm start
   ```

2. **Test on Device/Emulator:**
   - Open Expo Go app
   - Scan QR code from terminal
   - App should show login screen
   - Click "Sign In with Keycloak"
   - Should redirect to Keycloak login page
   - Enter test user credentials
   - Should redirect back to app and show tabs

### 4.2 Troubleshooting

**Common Issues:**

1. **Redirect URI Mismatch:**
   - Check Expo logs for actual redirect URI
   - Add it to Keycloak client's Valid Redirect URIs

2. **Cannot reach Keycloak on device:**
   - Ensure device and computer are on same network
   - Check `utils/network.ts` is correctly detecting your LAN IP
   - Try accessing `http://<YOUR_IP>:8080` from device browser

3. **Invalid client error:**
   - Verify client ID matches in Keycloak and `keycloak.ts`
   - Check client is set to "public" access type

4. **CORS errors:**
   - Ensure Web Origins is set to `*` in Keycloak client

---

## 📱 Phase 5: Network Configuration

### 5.1 Understanding buildUrl()

Your `utils/network.ts` uses `buildUrl()` to dynamically determine the host:

- **On physical device**: Uses your LAN IP (e.g., 192.168.1.x)
- **On emulator**: Uses localhost
- **Configurable**: Can set via `EXPO_PUBLIC_API_HOST` env variable

### 5.2 Environment Variables (Optional)

Create `.env` file in `front/`:

```env
EXPO_PUBLIC_API_HOST=192.168.1.10
EXPO_PUBLIC_KEYCLOAK_PORT=8080
EXPO_PUBLIC_BACKEND_PORT=8081
```

---

## 📦 Phase 6: Dependencies Verification

All required dependencies are already installed:

- ✅ `expo-auth-session`
- ✅ `expo-web-browser`
- ✅ `expo-crypto`
- ✅ `@react-native-async-storage/async-storage`

---

## 🔐 Phase 7: Security Considerations

### 7.1 Production Checklist

- [ ] Change Keycloak admin password
- [ ] Use HTTPS for Keycloak (required for production)
- [ ] Update redirect URIs to production URLs
- [ ] Set proper CORS origins
- [ ] Enable email verification
- [ ] Configure proper token lifespans
- [ ] Set up password policies
- [ ] Enable brute force protection

### 7.2 Token Management

Current token lifespans (from realm-export.json):
- Access token: 300 seconds (5 minutes)
- Refresh token: Controlled by SSO session (30 minutes idle)

Consider implementing:
- Automatic token refresh before expiry
- Handle token expiration in API interceptors

---

## 📝 Quick Start Commands

```bash
# 1. Start Keycloak
cd back/user-microservice && docker-compose -f docker-compose.keycloak.yml up -d

# 2. Start Backend
cd back/user-microservice && ./mvnw spring-boot:run

# 3. Start Frontend (port 19000 is now configured)
cd front && npm start

# 4. Access Keycloak Admin
# Open browser: http://localhost:8080
# Username: admin, Password: admin
```

---

## 📚 Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/docs/latest/)
- [Expo AuthSession](https://docs.expo.dev/versions/latest/sdk/auth-session/)
- [OAuth 2.0 with PKCE](https://oauth.net/2/pkce/)
- [Spring Security OAuth2 Resource Server](https://spring.io/guides/tutorials/spring-boot-oauth2/)

---

## ✅ Checklist

- [x] Cleanup existing Keycloak code
- [x] Configure Expo port to 19000
- [ ] Start Keycloak server
- [ ] Configure Keycloak client redirect URIs
- [ ] Create test users in Keycloak
- [ ] Start backend service
- [ ] Update frontend configuration
- [ ] Implement AuthContext
- [ ] Test authentication flow
- [ ] Test token refresh
- [ ] Test logout
- [ ] Test on physical device

---

## 🆘 Support

If you encounter issues:

1. Check the Keycloak admin console for client configuration
2. Check Expo logs for redirect URI issues
3. Verify all services are running on correct ports
4. Ensure device and computer are on same network (for testing)
5. Check browser/device can access Keycloak directly

Good luck with your setup! 🚀
