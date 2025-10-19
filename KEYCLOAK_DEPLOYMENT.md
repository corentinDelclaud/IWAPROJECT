# Keycloak Configuration Management & Deployment Guide

## Understanding Keycloak Data Persistence

### What Gets Stored Where?

1. **Docker Image** (quay.io/keycloak/keycloak:23.0)
   - Base Keycloak application
   - Default settings only
   - ❌ Does NOT contain your custom configurations

2. **Docker Volumes** (Local to your machine)
   - `postgres_data` → Database with all your configs
   - `keycloak_data` → Keycloak runtime data
   - ✅ Contains all your configurations
   - ❌ NOT portable between machines

3. **Realm Export Files** (JSON files you commit to Git)
   - Portable configuration as code
   - ✅ Can be version controlled
   - ✅ Can be deployed anywhere
   - **This is what you should use!**

---

## Solution: Export/Import Workflow

### Architecture Overview

```
Local Development          Git Repository          Production Server
┌──────────────┐          ┌──────────────┐        ┌──────────────┐
│ Configure    │          │              │        │              │
│ Keycloak UI  │──Export─▶│ realm.json   │───────▶│ Auto-import  │
│              │          │ (version     │        │ on startup   │
│              │          │  controlled) │        │              │
└──────────────┘          └──────────────┘        └──────────────┘
```

---

## Method 1: Manual Export/Import (Simple)

### Step 1: Configure Keycloak

1. Start Keycloak and configure your realm through the admin console
2. Create clients, roles, users, etc.

### Step 2: Export Configuration

```bash
# Export the entire realm configuration
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users realm_file

# Copy the file to your project
sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/iwa-project-realm.json
```

**Important Options:**

- `--users realm_file` - Includes users in the export
- `--users skip` - Excludes users (recommended for production)
- `--users different_files` - Users in separate files

### Step 3: Version Control

```bash
# Add to git (THIS is what you push to your repository)
git add keycloak-config/iwa-project-realm.json
git commit -m "Add Keycloak realm configuration"
git push origin keycloak
```

### Step 4: Deploy to Another Environment

```bash
# On the new server/machine:

# 1. Clone your repository
git clone <your-repo>

# 2. Start Keycloak (will auto-import from keycloak-config/)
docker-compose up -d

# The realm is automatically imported! ✅
```

---

## Method 2: Automatic Import on Startup (Recommended - Already Set Up!)

I've already configured your `docker-compose.yml` for this:

```yaml
keycloak:
  command: start-dev --import-realm  # ← Auto-imports on startup
  volumes:
    - ./keycloak-config:/opt/keycloak/data/import  # ← Reads from here
```

### How It Works

1. **Place realm files** in `./keycloak-config/` directory
2. **Keycloak automatically imports** them on startup
3. **Files are version controlled** with your project
4. **Works on any machine** that clones your repo

### Workflow

```bash
# 1. Configure Keycloak (once)
# Use admin console to create realm, clients, roles...

# 2. Export configuration
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users skip

sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/

# 3. Commit to git
git add keycloak-config/iwa-project-realm.json
git commit -m "Update Keycloak configuration"
git push

# 4. On any other machine/server
git pull
docker-compose down
docker-compose up -d
# ✅ Configuration automatically applied!
```

---

## Method 3: Database Backup (For Complete State)

If you need to preserve **everything** including user passwords, sessions, etc.:

### Backup PostgreSQL Database

```bash
# Create backup
sudo docker exec iwa-postgres pg_dump -U keycloak keycloak > keycloak-backup.sql

# Restore on another machine
cat keycloak-backup.sql | sudo docker exec -i iwa-postgres psql -U keycloak keycloak
```

⚠️ **Warning:** This includes sensitive data (passwords, tokens). Only use for:

- Migration between environments
- Disaster recovery
- ❌ Don't commit to Git!

---

## Recommended Production Workflow

### Development Environment (Your Machine)

```bash
# 1. Start fresh Keycloak
docker-compose up -d

# 2. Configure through admin console
# - Create realm: iwa-project
# - Create clients: api-gateway, mobile-app
# - Create roles: user, admin, seller, buyer
# - Create test users

# 3. Export configuration (exclude test users for production)
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users skip

# 4. Copy to project
sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/

# 5. Commit
git add keycloak-config/
git commit -m "Add Keycloak realm configuration"
git push
```

### Production Environment

```bash
# 1. Clone repository
git clone <your-repo>
cd IWAPROJECT

# 2. Update .env for production
vim .env
# - Change admin password
# - Change database password
# - Update hostname

# 3. Start Keycloak (will auto-import configuration)
docker-compose up -d

# 4. Create production users through admin console
# (Test users from dev are not included)

# 5. Done! ✅
```

---

## What Should Be in Git?

### ✅ Commit These

```
keycloak-config/
  └── iwa-project-realm.json    # Realm configuration
docker-compose.yml               # Service definitions
.env.example                     # Template for .env
README.md                        # Documentation
```

### ❌ Don't Commit These

```
.env                             # Contains secrets
keycloak-backup.sql              # Contains sensitive data
postgres_data/                   # Database volume
keycloak_data/                   # Runtime data
```

---

## Realm Export Best Practices

### Include in Exports

- ✅ Realm settings
- ✅ Clients (api-gateway, mobile-app)
- ✅ Roles (user, admin, seller, buyer)
- ✅ Authentication flows
- ✅ Theme configurations

### Exclude from Exports

- ❌ Test users (create manually per environment)
- ❌ Real user passwords
- ❌ Session data
- ❌ Tokens

**Why?** Different environments need different users:

- **Dev:** Test users with simple passwords
- **Staging:** Test users + some real data
- **Production:** Only real users

---

## Updating Configuration

### If you make changes to Keycloak

```bash
# 1. Make changes in admin console

# 2. Export updated configuration
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users skip

# 3. Copy and commit
sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/
git add keycloak-config/iwa-project-realm.json
git commit -m "Update Keycloak: added new client scope"
git push

# 4. Team members pull and restart
git pull
docker-compose restart keycloak
```

---

## Environment-Specific Configurations

### .env.development

```env
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KC_HOSTNAME=localhost
```

### .env.production

```env
KEYCLOAK_ADMIN=secure_admin_username
KEYCLOAK_ADMIN_PASSWORD=very_secure_password_here
KC_HOSTNAME=auth.yourcompany.com
```

### Usage

```bash
# Development
cp .env.development .env
docker-compose up -d

# Production
cp .env.production .env
docker-compose up -d
```

---

## Troubleshooting Import Issues

### Import Didn't Work?

Check if realm file is in the right place:

```bash
ls -la keycloak-config/
# Should show: iwa-project-realm.json
```

### Manual Import

```bash
sudo docker cp ./keycloak-config/iwa-project-realm.json \
  iwa-keycloak:/opt/keycloak/data/import/

sudo docker restart iwa-keycloak
```

### View Import Logs

```bash
sudo docker logs iwa-keycloak | grep -i import
```

---

## Summary: Complete Deployment Workflow

### One-Time Setup (You Do This Once)

1. Configure Keycloak through admin console
2. Export realm configuration
3. Commit `keycloak-config/iwa-project-realm.json` to Git
4. Push to repository

### Deploying to New Server (Anyone Can Do This)

1. Clone repository
2. Copy `.env.example` to `.env` and update values
3. Run `docker-compose up -d`
4. ✅ Keycloak is configured automatically!
5. Create environment-specific users if needed

### Updating Configuration

1. Make changes in admin console
2. Export updated configuration
3. Commit and push to Git
4. Others pull and restart: `docker-compose restart keycloak`

---

## Quick Reference Commands

```bash
# Export realm
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users skip

# Copy to project
sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/

# Backup database
sudo docker exec iwa-postgres pg_dump -U keycloak keycloak > backup.sql

# Restore database
cat backup.sql | sudo docker exec -i iwa-postgres psql -U keycloak keycloak
```

---

## Next Steps

1. ✅ Configure your Keycloak realm (follow KEYCLOAK_SETUP.md)
2. ✅ Export the configuration
3. ✅ Commit to Git
4. ✅ Ready to deploy anywhere!

**Your configuration is now portable and version-controlled!** 🚀
