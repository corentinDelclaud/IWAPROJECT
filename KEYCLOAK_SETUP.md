# Keycloak Setup Guide for IWA Project

## Overview

This guide will help you set up Keycloak as the Identity and Access Management (IAM) solution for the IWA Project.

## Prerequisites

- Docker and Docker Compose installed
- Ports 8180 (Keycloak) and 5432 (PostgreSQL) available

## Quick Start

### 1. Start Keycloak

```bash
# From the project root directory
docker-compose up -d
```

This will start:
- **PostgreSQL** on port 5432 (database for Keycloak)
- **Keycloak** on port 8180

### 2. Check Container Status

```bash
docker-compose ps
```

Wait until both containers show as "healthy". This may take 1-2 minutes.

### 3. Access Keycloak Admin Console

Open your browser and navigate to:
```
http://localhost:8180
```

Click on **"Administration Console"**

**Default Credentials:**
- Username: `admin`
- Password: `admin`

⚠️ **Important:** Change these credentials in production by updating the `.env` file.

---

## Initial Keycloak Configuration

### Step 1: Create a New Realm

A realm is a space where you manage objects like users, applications, roles, and groups.

1. Hover over **"master"** in the top-left corner
2. Click **"Create Realm"**
3. Enter the following:
   - **Realm name:** `iwa-project`
   - Click **"Create"**

### Step 2: Configure Realm Settings

1. Go to **Realm Settings** (left sidebar)
2. Configure the following tabs:

#### General Tab

- **Display name:** IWA Project
- **Enabled:** ON
- **User registration:** ON (if you want users to self-register)
- **Forgot password:** ON
- **Remember me:** ON

#### Login Tab

- **User registration:** ON/OFF (based on your needs)
- **Forgot password:** ON
- **Remember me:** ON
- **Email as username:** OFF (use username)

#### Email Tab (Optional for now, required for password reset)

Configure your SMTP settings for email notifications:
- **Host:** smtp.gmail.com (example)
- **Port:** 587
- **From:** noreply@iwaproject.com
- **Enable SSL:** ON
- **Enable Authentication:** ON
- **Username:** your-email@gmail.com
- **Password:** your-app-password

### Step 3: Create Clients

Clients are applications that can request authentication.

#### 3.1 Create Backend Client (API Gateway)

1. Go to **Clients** → **Create client**
2. **Client type:** OpenID Connect
3. **Client ID:** `api-gateway`
4. Click **"Next"**
5. Configure:
   - **Client authentication:** ON
   - **Authorization:** OFF
   - **Standard flow:** ON
   - **Direct access grants:** ON
6. Click **"Next"**
7. Configure URLs:
   - **Root URL:** `http://localhost:8080`
   - **Valid redirect URIs:** `http://localhost:8080/*`
   - **Valid post logout redirect URIs:** `http://localhost:8080/*`
   - **Web origins:** `http://localhost:8080`
8. Click **"Save"**

**Important:** Go to **Credentials** tab and copy the **Client Secret** - you'll need this for backend configuration.

#### 3.2 Create Frontend Client (Mobile App)

1. Go to **Clients** → **Create client**
2. **Client type:** OpenID Connect
3. **Client ID:** `mobile-app`
4. Click **"Next"**
5. Configure:
   - **Client authentication:** OFF (public client)
   - **Authorization:** OFF
   - **Standard flow:** ON
   - **Direct access grants:** ON
   - **Implicit flow:** OFF
   - **OAuth 2.0 Device Authorization Grant:** OFF
6. Click **"Next"**
7. Configure URLs:
   - **Root URL:** `exp://localhost:8081`
   - **Valid redirect URIs:**
     ```
     exp://localhost:8081
     http://localhost:8081
     myapp://*
     exp://*
     ```
   - **Valid post logout redirect URIs:** `exp://localhost:8081/*`
   - **Web origins:** `*` (for development only)
8. Click **"Save"**
9. Go to **Advanced** tab:
   - **Proof Key for Code Exchange (PKCE) Code Challenge Method:** S256 (required for mobile)

### Step 4: Create Roles

Roles define what users can do in your application.

1. Go to **Realm roles** → **Create role**
2. Create the following roles:

   - **Role name:** `user`
     - **Description:** Standard user with basic access
   
   - **Role name:** `admin`
     - **Description:** Administrator with full access
   
   - **Role name:** `seller`
     - **Description:** Can list and manage products
   
   - **Role name:** `buyer`
     - **Description:** Can browse and purchase products

### Step 5: Create Test Users

1. Go to **Users** → **Add user**
2. Create an admin user:
   - **Username:** `testadmin`
   - **Email:** `admin@test.com`
   - **First name:** Test
   - **Last name:** Admin
   - **Email verified:** ON
   - Click **"Create"**

3. Set password:
   - Go to **Credentials** tab
   - Click **"Set password"**
   - **Password:** `Test123!`
   - **Temporary:** OFF
   - Click **"Save"**

4. Assign roles:
   - Go to **Role mappings** tab
   - Click **"Assign role"**
   - Select: `admin`, `user`
   - Click **"Assign"**

5. Repeat for other test users:
   - **Username:** `testseller`
     - Email: `seller@test.com`
     - Password: `Test123!`
     - Roles: `seller`, `user`
   
   - **Username:** `testbuyer`
     - Email: `buyer@test.com`
     - Password: `Test123!`
     - Roles: `buyer`, `user`

### Step 6: Configure Token Settings

1. Go to **Realm settings** → **Tokens** tab
2. Configure token lifespans:
   - **Access Token Lifespan:** 5 minutes (default)
   - **Access Token Lifespan For Implicit Flow:** 15 minutes
   - **Client login timeout:** 1 minute
   - **Login timeout:** 30 minutes
   - **Login action timeout:** 5 minutes
   - **SSO Session Idle:** 30 minutes
   - **SSO Session Max:** 10 hours

---

## Verification

### Test Authentication Endpoint

You can test if Keycloak is working properly:

```bash
# Get the OpenID Connect configuration
curl http://localhost:8180/realms/iwa-project/.well-known/openid-configuration
```

### Test User Login (Direct Grant)

```bash
curl -X POST http://localhost:8180/realms/iwa-project/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=mobile-app" \
  -d "username=testadmin" \
  -d "password=Test123!" \
  -d "grant_type=password"
```

You should receive an access token in the response.

---

## Important URLs

- **Keycloak Admin Console:** http://localhost:8180
- **Realm Endpoint:** http://localhost:8180/realms/iwa-project
- **Token Endpoint:** http://localhost:8180/realms/iwa-project/protocol/openid-connect/token
- **Authorization Endpoint:** http://localhost:8180/realms/iwa-project/protocol/openid-connect/auth
- **UserInfo Endpoint:** http://localhost:8180/realms/iwa-project/protocol/openid-connect/userinfo
- **JWKS Endpoint:** http://localhost:8180/realms/iwa-project/protocol/openid-connect/certs

---

## Managing Keycloak

### Stop Keycloak

```bash
docker-compose down
```

### View Logs

```bash
docker-compose logs -f keycloak
```

### Restart Keycloak

```bash
docker-compose restart keycloak
```

### Reset Everything (⚠️ Deletes all data)

```bash
docker-compose down -v
docker-compose up -d
```

---

## Export Realm Configuration (Backup)

To export your realm configuration:

```bash
# Export realm to a file
docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project
```

Then copy the exported file from the container:
```bash
docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json ./keycloak-realm-export.json
```

---

## Security Best Practices

### For Production:

1. **Change default credentials** in `.env`:
   ```env
   KEYCLOAK_ADMIN=your-secure-admin-username
   KEYCLOAK_ADMIN_PASSWORD=your-very-secure-password
   ```

2. **Enable HTTPS:**
   - Set up SSL certificates
   - Configure `KC_PROXY=edge` in docker-compose.yml
   - Use a reverse proxy (nginx/traefik)

3. **Use strong database passwords**

4. **Enable email verification** for new users

5. **Configure rate limiting** to prevent brute force attacks

6. **Regular backups** of PostgreSQL database and realm configuration

7. **Update Keycloak** regularly for security patches

8. **Restrict network access** to Keycloak admin console

---

## Troubleshooting

### Keycloak won't start

- Check if ports 8180 and 5432 are available
- Check Docker logs: `docker-compose logs keycloak`
- Ensure PostgreSQL is healthy: `docker-compose ps`

### Can't access admin console

- Verify Keycloak is running: `docker-compose ps`
- Check if port 8180 is accessible: `curl http://localhost:8180`
- Try clearing browser cache

### Database connection errors

- Ensure PostgreSQL container is running and healthy
- Check database credentials in `.env` file
- Verify network connectivity: `docker network inspect iwaproject_iwa-network`

---

## Next Steps

After completing this setup, you can proceed to:
1. **Phase 2:** Configure Spring Cloud Gateway to use Keycloak
2. **Phase 3:** Integrate authentication in React Native frontend

---

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Server Administration](https://www.keycloak.org/docs/latest/server_admin/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
