# Stripe Integration Test Environment

This is a complete Stripe Connect integration example for testing payment functionality before integrating with your main application.

## Quick Start

### 1. Install Dependencies
```bash
npm install
```

### 2. Start the Application
```bash
npm run dev
```

This will start both:
- **Frontend** (React + Vite) on http://localhost:3000
- **Backend** (Java Spark) on http://localhost:4242

### 3. Open Your Browser
Navigate to [http://localhost:3000/](http://localhost:3000/)

## What's Included

This example demonstrates:

✅ **Stripe Connect** - Create and manage connected accounts  
✅ **Product Management** - Create products with prices  
✅ **Checkout Sessions** - Complete payment flow  
✅ **Subscriptions** - Platform subscription management  
✅ **Account Onboarding** - Merchant verification flow  

## Configuration

Your API keys are configured in the `.env` file. The test keys are already set up:
- Frontend: Uses `VITE_STRIPE_PUBLISHABLE_KEY`
- Backend: Uses `STRIPE_SECRET_KEY`

## Testing

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for detailed testing instructions including:
- How to create connected accounts
- Test card numbers
- API endpoint documentation
- Common troubleshooting steps

## Tech Stack

- **Frontend**: React 18, Vite, React Router, Stripe.js
- **Backend**: Java 11, Spark Framework, Stripe Java SDK
- **Build Tool**: Maven for Java, npm for frontend

## Project Structure

```
stripe/
├── src/                    # React components
│   ├── App.jsx            # Main app component
│   ├── Home.jsx           # Home page with account creation
│   ├── Storefront.jsx     # Product storefront
│   ├── Products.jsx       # Product management
│   └── main/java/         # Java backend
│       └── com/stripe/sample/
│           └── Server.java # API server
├── .env                   # Environment variables (API keys)
├── package.json           # Node dependencies
├── pom.xml               # Maven dependencies
└── vite.config.js        # Vite configuration
```

## Next Steps

Once you've tested the Stripe integration:

1. **Connect to Your Product Catalog** - Link this with your service-catalog microservice
2. **Integrate Authentication** - Connect with your Keycloak auth
3. **Add Database Persistence** - Store orders and transactions
4. **Implement Webhooks** - Handle Stripe events properly
5. **Deploy to Production** - Use production API keys and secure configuration

## Documentation

- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Comprehensive testing guide
- [Stripe Connect Docs](https://stripe.com/docs/connect)
- [Stripe Checkout Docs](https://stripe.com/docs/checkout)

