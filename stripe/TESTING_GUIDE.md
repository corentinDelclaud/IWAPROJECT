# Stripe Integration Testing Guide

This guide will help you test the Stripe integration before connecting it to your products.

## Prerequisites

1. **Node.js** (v16 or higher)
2. **Java 11** or higher
3. **Maven** installed
4. **Stripe Account** (test mode keys)

## Setup Steps

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment Variables

Your `.env` file is already configured with test API keys. The keys are:
- `STRIPE_SECRET_KEY`: Server-side key (sk_test_...)
- `VITE_STRIPE_PUBLISHABLE_KEY`: Client-side key (pk_test_...)
- `DOMAIN`: Your local development URL (http://localhost:3000)

### 3. Start the Application

Run both the frontend and backend simultaneously:

```bash
npm run dev
```

This command will:
- Start the Vite dev server on port 3000 (frontend)
- Start the Java Spark server on port 4242 (backend)
- Automatically open your browser to http://localhost:3000

## What Can You Test?

### 1. **Create a Connected Account**
- Click the "Create Account" button on the home page
- This simulates creating a merchant/seller account using Stripe Connect

### 2. **Onboarding Flow**
- Complete the account onboarding process
- This uses Stripe's Account Link API to handle merchant verification

### 3. **Create Products**
- Once your account is active, you can create test products
- Add product name, description, and price
- Products are created on the connected account

### 4. **Test Checkout**
- View the storefront for your connected account
- Add products to cart
- Complete a test purchase using Stripe test cards

### 5. **Subscribe to Platform**
- Test subscription functionality
- Use the platform subscription feature

## Stripe Test Cards

Use these test card numbers for payments:

| Card Number | Description |
|-------------|-------------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0025 0000 3155` | Requires authentication (3D Secure) |
| `4000 0000 0000 9995` | Declined card |

**Additional details for test cards:**
- **Expiry**: Any future date (e.g., 12/25)
- **CVC**: Any 3 digits (e.g., 123)
- **ZIP**: Any 5 digits (e.g., 12345)

## API Endpoints

The backend server (port 4242) provides these endpoints:

- `POST /api/create-account` - Create a connected account
- `POST /api/account-session` - Create an account session
- `POST /api/create-product` - Create a product on connected account
- `POST /api/list-products` - List products for an account
- `POST /api/subscribe-to-platform` - Create platform subscription
- `POST /api/create-checkout-session` - Create checkout session
- `POST /webhook` - Stripe webhook handler

## Checking Stripe Dashboard

1. Go to https://dashboard.stripe.com/test/dashboard
2. Check **Connect > Accounts** to see created accounts
3. Check **Products** to see created products
4. Check **Payments** to see test transactions

## Architecture

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   React     │────────▶│   Java      │────────▶│   Stripe    │
│  Frontend   │  API    │   Server    │   API   │     API     │
│  (Port 3000)│ Calls   │ (Port 4242) │ Calls   │             │
└─────────────┘         └─────────────┘         └─────────────┘
```

## Common Issues

### Port Already in Use
If you get a port error:
```bash
# Kill processes on ports 3000 and 4242
lsof -ti:3000 | xargs kill -9
lsof -ti:4242 | xargs kill -9
```

### Maven Compilation Errors
Make sure Java 11+ is installed:
```bash
java -version
```

### API Key Issues
- Ensure your `.env` file has valid test mode keys (starting with `sk_test_` and `pk_test_`)
- Never commit real API keys to version control

## Next Steps

Once testing is complete:
1. **Integration with Products**: Connect this to your product catalog
2. **Database Integration**: Store product and order information
3. **User Authentication**: Link with your Keycloak authentication
4. **Webhook Handling**: Implement proper webhook endpoints for production
5. **Error Handling**: Add comprehensive error handling and logging

## Useful Resources

- [Stripe Connect Docs](https://stripe.com/docs/connect)
- [Stripe Testing Docs](https://stripe.com/docs/testing)
- [Stripe Checkout Docs](https://stripe.com/docs/checkout)
