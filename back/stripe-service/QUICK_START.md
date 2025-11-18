# Stripe Service - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Configure Your API Keys

1. Copy the example environment file:
   ```bash
   cd /home/etienne/Documents/IWAPROJECT/back/stripe-service
   cp .env.example .env
   ```

2. Get your Stripe API keys:
   - Go to https://dashboard.stripe.com/test/apikeys
   - Copy your **Secret key** (starts with `sk_test_`)
   - Copy your **Publishable key** (starts with `pk_test_`)

3. Edit `.env` and add your keys:
   ```env
   STRIPE_SECRET_KEY=sk_test_YOUR_ACTUAL_KEY_HERE
   STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_ACTUAL_KEY_HERE
   ```

### Step 2: Build and Run

From the `back/stripe-service` directory:

```bash
# Build the service
mvn clean install

# Run the service
mvn spring-boot:run
```

The service will start on **http://localhost:8090**

### Step 3: Test the API

You can use the included `test-stripe-api.http` file with VS Code REST Client extension, or use curl:

```bash
# Health check
curl http://localhost:8090/actuator/health

# Create a Connect account
curl -X POST http://localhost:8090/api/stripe/connect-account \
  -H "Content-Type: application/json" \
  -d '{"email":"merchant@example.com"}'
```

## 📋 Common Tasks

### Create a Connected Merchant Account

```bash
curl -X POST http://localhost:8090/api/stripe/connect-account \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}' | jq
```

Save the `accountId` from the response!

### Create an Onboarding Link

```bash
curl -X POST http://localhost:8090/api/stripe/account-link \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acct_YOUR_ACCOUNT_ID"}' | jq
```

Open the returned URL in a browser to complete onboarding.

### Create a Product

```bash
curl -X POST http://localhost:8090/api/stripe/product \
  -H "Content-Type: application/json" \
  -d '{
    "productName":"Premium Widget",
    "productDescription":"A high-quality product",
    "productPrice":2999,
    "accountId":"acct_YOUR_ACCOUNT_ID"
  }' | jq
```

### Get Products

```bash
curl http://localhost:8090/api/stripe/products/acct_YOUR_ACCOUNT_ID | jq
```

### Create Checkout Session

```bash
curl -X POST http://localhost:8090/api/stripe/checkout-session \
  -H "Content-Type: application/json" \
  -d '{
    "accountId":"acct_YOUR_ACCOUNT_ID",
    "priceId":"price_YOUR_PRICE_ID"
  }' | jq
```

Open the returned URL to complete the checkout.

## 🐳 Run with Docker

```bash
# Build
docker build -t stripe-service .

# Run
docker run -p 8090:8090 \
  -e STRIPE_SECRET_KEY=sk_test_your_key \
  -e STRIPE_PUBLISHABLE_KEY=pk_test_your_key \
  stripe-service
```

## 🧪 Testing with Stripe Test Cards

When testing checkout, use these test card numbers:

- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **3D Secure**: `4000 0027 6000 3184`

Any future expiry date and any 3-digit CVC will work.

## 🔗 Integration with API Gateway

To route requests through your API Gateway:

1. Make sure the stripe-service is running on port 8090
2. Add routes in your `api-gateway` configuration
3. Access via: `http://localhost:8080/api/stripe/*` (adjust port as needed)

## 🔍 Troubleshooting

### Service won't start
- Check that port 8090 is available
- Verify Java 21 is installed: `java -version`
- Check logs for specific errors

### API returns 500 errors
- Verify your Stripe API keys are correct
- Check the logs: `mvn spring-boot:run` shows detailed errors
- Ensure you're using test mode keys (starting with `sk_test_`)

### CORS errors from frontend
- The service has CORS enabled for all origins
- If you still have issues, check browser console for specific errors

## 📚 Next Steps

1. **Integrate with Frontend**: Use the API endpoints in your React/React Native app
2. **Add Authentication**: Secure endpoints with JWT tokens from your auth-service
3. **Setup Webhooks**: Use Stripe CLI for local webhook testing
4. **Production Deployment**: Switch to live API keys and configure webhook endpoints

## 🔐 Security Notes

- ⚠️ Never commit your `.env` file (it's in `.gitignore`)
- ⚠️ Never expose secret keys in frontend code
- ⚠️ Always use HTTPS in production
- ⚠️ Verify webhook signatures in production

## 📖 Further Reading

- Full API documentation: See `README.md`
- Stripe Connect guide: https://stripe.com/docs/connect
- Testing guide: https://stripe.com/docs/testing
