# 🚀 Stripe Test Quick Reference

## Start Testing
```bash
cd /home/etienne/Documents/IWAPROJECT/stripe
./test-stripe.sh
```

Or manually:
```bash
npm run dev
```

## URLs
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:4242
- **Stripe Dashboard**: https://dashboard.stripe.com/test/dashboard

## Test Cards 💳

| Purpose | Card Number | Details |
|---------|-------------|---------|
| ✅ Success | `4242 4242 4242 4242` | Standard success |
| 🔒 3D Secure | `4000 0025 0000 3155` | Requires authentication |
| ❌ Declined | `4000 0000 0000 9995` | Payment declined |

**For all cards**: Any future date for expiry, any 3 digits for CVC, any ZIP code

## Quick Test Flow

### 1. Create Account
```
Home Page → "Create Account" → Creates Stripe Connect account
```

### 2. Complete Onboarding
```
Click "Start Onboarding" → Fill form → Complete verification
```

### 3. Add Products
```
Products Tab → "Create Product" → Enter details → Save
```

### 4. Test Checkout
```
Storefront → Add to Cart → Checkout → Use test card → Complete
```

## API Endpoints (Backend on :4242)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/create-account` | Create connected account |
| POST | `/api/account-session` | Get account session |
| POST | `/api/create-product` | Create product |
| POST | `/api/list-products` | List products |
| POST | `/api/create-checkout-session` | Start checkout |
| POST | `/api/subscribe-to-platform` | Platform subscription |

## Environment Variables

Located in `.env`:
```
STRIPE_SECRET_KEY=sk_test_...          # Backend
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_... # Frontend
DOMAIN=http://localhost:3000
PLATFORM_PRICE_ID=                      # Optional
```

## Troubleshooting

### Port in use?
```bash
lsof -ti:3000 | xargs kill -9
lsof -ti:4242 | xargs kill -9
```

### Maven compile errors?
```bash
mvn clean compile
```

### Dependencies missing?
```bash
npm install
```

## What to Test

- [ ] Create a connected account
- [ ] Complete onboarding flow
- [ ] Create 2-3 test products
- [ ] View storefront
- [ ] Add products to cart
- [ ] Complete checkout with test card
- [ ] Test 3D Secure card
- [ ] Test declined card
- [ ] Check Stripe dashboard for events

## Integration Plan

After testing works:

1. ✅ **Test Stripe standalone** (current step)
2. 🔄 **Connect to service-catalog** - Link products
3. 🔄 **Add authentication** - Integrate Keycloak
4. 🔄 **Store orders** - Database persistence
5. 🔄 **Webhooks** - Handle Stripe events
6. 🔄 **Production deploy** - Use live keys

## Useful Commands

```bash
# View backend logs
mvn compile exec:java -Dexec.mainClass=com.stripe.sample.Server

# Build only
npm run build

# Install/update dependencies
npm install

# Clean Maven build
mvn clean
```
