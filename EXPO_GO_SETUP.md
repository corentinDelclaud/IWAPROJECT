# Fixing Expo Go Authentication

## ✅ CORS Issue Fixed!

The backend now accepts requests from:
- `http://localhost:*` (web development)
- `http://192.168.*.*:*` (local network devices)
- `exp://*` (Expo Go app)

The backend has been restarted with CORS enabled.

---

## 🔧 Fixing Expo Go App Authentication

### Problem
Expo Go app authentication fails because Keycloak doesn't recognize the redirect URI.

### Solution: Configure Keycloak Redirect URIs

#### Step 1: Find Your Redirect URI

1. **Start your Expo app:**
   ```bash
   cd /home/etienne/Documents/IWAPROJECT/front
   npm start
   ```

2. **Open Expo Go on your device** and scan the QR code

3. **Click "Sign In with Keycloak"**

4. **Check the Expo console/terminal** for a log like:
   ```
   [Auth] Redirect URI: exp://192.168.1.100:19000/--/auth
   ```

#### Step 2: Add Redirect URI to Keycloak

1. **Open Keycloak Admin Console:**
   - Go to: http://localhost:8080/admin
   - Login with `admin` / `admin`

2. **Navigate to Client Settings:**
   - Realms → **IWA_NextLevel**
   - Clients → **user-microservice**

3. **Add Valid Redirect URIs:**

   Click "Add" and add these patterns:

   ```
   exp://*
   test://*
   http://localhost:19000/*
   http://192.168.*.*:19000/*
   ```

   **Important:** Replace `192.168.*.*` with your actual IP from the console log if needed.

4. **Add Web Origins:**
   ```
   *
   ```

5. **Verify these settings:**
   - Client Protocol: `openid-connect`
   - Access Type: `public`
   - Standard Flow Enabled: `ON`
   - Direct Access Grants Enabled: `ON`

6. **Click "Save"**

#### Step 3: Test Again

1. **Restart your Expo app** (press `r` in terminal or shake device)
2. **Click "Sign In with Keycloak"**
3. **Should now redirect to Keycloak login**
4. **Enter credentials** (create user if needed)
5. **Should redirect back to app**

---

## 👤 Create Test User (if needed)

1. **In Keycloak Admin Console:**
   - Users → **Add User**
   - Username: `testuser`
   - Email: `test@example.com`
   - Email Verified: `ON`
   - Click **Save**

2. **Set Password:**
   - Go to **Credentials** tab
   - Click **Set Password**
   - Password: `password123`
   - Temporary: `OFF`
   - Click **Save**

3. **Test login with:**
   - Username: `testuser`
   - Password: `password123`

---

## 🔍 Troubleshooting

### "Invalid redirect URI" error
- Check Expo console for actual redirect URI
- Ensure it's added to Keycloak client settings
- Try adding broader patterns: `exp://*` and `test://*`

### "Cannot reach Keycloak" on device
- Ensure device and computer are on same WiFi
- Check firewall isn't blocking port 8080
- Try accessing `http://<YOUR_IP>:8080` from device browser
- If using VPN, disable it temporarily

### Profile page still has CORS error
- Backend should be restarted (already done)
- Clear browser cache (Ctrl+Shift+R)
- Try in incognito/private window

### Expo Go vs Web behavior differences
- **Web**: Uses `http://localhost:19000` as origin
- **Expo Go**: Uses `exp://` custom scheme
- Both need separate redirect URI patterns in Keycloak

---

## 📱 Expected Redirect URIs by Platform

| Platform | Redirect URI Pattern | Example |
|----------|---------------------|---------|
| Expo Go (iOS/Android) | `exp://<IP>:19000/--/auth` | `exp://192.168.1.100:19000/--/auth` |
| Expo Web | `http://localhost:19000/*` | `http://localhost:19000/auth` |
| Custom Scheme | `test://*` | `test://auth` |

---

## ✅ Verification Checklist

- [ ] Backend is running on port 8081
- [ ] Keycloak is running on port 8080
- [ ] Expo dev server is running on port 19000
- [ ] Valid Redirect URIs added to Keycloak (with wildcards)
- [ ] Web Origins set to `*` in Keycloak
- [ ] Test user created in Keycloak
- [ ] Device can ping your computer's IP
- [ ] CORS error is fixed in web console

---

## 🎯 Quick Fix Commands

```bash
# Terminal 1: Keycloak (if not running)
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
docker-compose -f docker-compose.keycloak.yml up

# Terminal 2: Backend (already running with CORS)
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
./mvnw spring-boot:run

# Terminal 3: Frontend
cd /home/etienne/Documents/IWAPROJECT/front
npm start
```

---

Good luck! The CORS issue is now fixed. Just need to configure the redirect URIs in Keycloak for Expo Go to work! 🚀
