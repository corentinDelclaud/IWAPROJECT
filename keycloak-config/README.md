# Keycloak Realm Configuration

This directory contains the exported Keycloak realm configuration that will be automatically imported when Keycloak starts.

## What Goes Here

Place your exported realm JSON files in this directory:
- `iwa-project-realm.json` - Main application realm configuration

## How to Export

After configuring your realm in the Keycloak admin console:

```bash
# Export the realm configuration (without test users for production)
sudo docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project \
  --users skip

# Copy to this directory
sudo docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json \
  ./keycloak-config/
```

## Auto-Import

When you run `docker-compose up`, Keycloak will automatically import any `.json` files from this directory on startup.

## What's Included in Exports

✅ Realm settings
✅ Clients (api-gateway, mobile-app)
✅ Roles and role mappings
✅ Authentication flows
✅ Client scopes
✅ Identity providers (if configured)

## What's NOT Included

❌ Users (recommended to create separately per environment)
❌ User passwords
❌ Sessions and tokens
❌ Runtime state

## Version Control

**Commit these files to Git!** This allows:
- Configuration as code
- Easy deployment to multiple environments
- Team collaboration
- Version history of configuration changes

See `KEYCLOAK_DEPLOYMENT.md` for complete deployment workflow.
