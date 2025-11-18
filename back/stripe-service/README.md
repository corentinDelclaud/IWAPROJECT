# Stripe Service

A clean, production-ready Stripe payment integration microservice built with Spring Boot for the IWA Project.

## Features

✅ **Stripe Connect** - Create and manage connected accounts  
✅ **Product Management** - Create products with prices on connected accounts  
✅ **Checkout Sessions** - Complete payment flow with both one-time and subscription payments  
✅ **Platform Subscriptions** - Manage platform-level subscriptions  
✅ **Account Onboarding** - Merchant verification flow  
✅ **Webhook Handling** - Process Stripe events securely  
✅ **CORS Enabled** - Ready for frontend integration  

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.11**
- **Stripe Java SDK 29.6.0-beta.1**
- **Lombok** for reduced boilerplate
- **Maven** for dependency management

## Project Structure

```
stripe-service/
├── src/main/java/com/iwaproject/stripe/
│   ├── StripeServiceApplication.java     # Main application class
│   ├── config/
│   │   ├── StripeConfig.java            # Stripe configuration
│   │   └── CorsConfig.java              # CORS configuration
│   ├── controller/
│   │   ├── StripeController.java        # REST API endpoints
│   │   └── WebhookController.java       # Webhook endpoint
│   ├── service/
│   │   ├── StripeService.java          # Business logic
│   │   └── WebhookService.java         # Webhook processing
│   └── dto/                            # Data Transfer Objects
├── src/main/resources/
│   └── application.yml                  # Application configuration
├── Dockerfile                           # Docker configuration
└── pom.xml                             # Maven dependencies
```

## Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure:

```env
STRIPE_SECRET_KEY=sk_test_your_secret_key_here
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
STRIPE_PLATFORM_PRICE_ID=price_your_platform_price_id_here
APP_DOMAIN=http://localhost:3000
FRONTEND_URL=http://localhost:3000
```

### Application Properties

The service runs on port **8090** by default. You can change this in `application.yml`.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Stripe account with API keys

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will start on `http://localhost:8090`.

### Using Docker

```bash
# Build Docker image
docker build -t stripe-service .

# Run container
docker run -p 8090:8090 \
  -e STRIPE_SECRET_KEY=your_key \
  -e STRIPE_PUBLISHABLE_KEY=your_key \
  stripe-service
```

## API Endpoints

### Connect Accounts

#### Create Connect Account
```http
POST /api/stripe/connect-account
Content-Type: application/json

{
  "email": "merchant@example.com"
}
```

#### Create Account Link (Onboarding)
```http
POST /api/stripe/account-link
Content-Type: application/json

{
  "accountId": "acct_xxx"
}
```

#### Get Account Status
```http
GET /api/stripe/account-status/{accountId}
```

### Products

#### Create Product
```http
POST /api/stripe/product
Content-Type: application/json

{
  "productName": "Premium Widget",
  "productDescription": "A high-quality widget",
  "productPrice": 2999,
  "accountId": "acct_xxx"
}
```

#### Get Products
```http
GET /api/stripe/products/{accountId}
```

### Checkout

#### Create Checkout Session
```http
POST /api/stripe/checkout-session
Content-Type: application/json

{
  "accountId": "acct_xxx",
  "priceId": "price_xxx"
}
```

#### Subscribe to Platform
```http
POST /api/stripe/subscribe-platform
Content-Type: application/json

{
  "accountId": "acct_xxx"
}
```

#### Create Portal Session
```http
POST /api/stripe/portal-session
Content-Type: application/json

{
  "sessionId": "cs_xxx"
}
```

### Webhooks

#### Webhook Endpoint
```http
POST /api/stripe/webhook
Stripe-Signature: signature_from_stripe

[Stripe webhook payload]
```

## Webhook Events Handled

- `customer.subscription.trial_will_end`
- `customer.subscription.deleted`
- `customer.subscription.created`
- `customer.subscription.updated`
- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `payment_intent.succeeded`
- `payment_intent.payment_failed`

## Integration with Your Project

### 1. Add to Parent POM

Update `/back/pom.xml` to include the stripe-service module:

```xml
<modules>
    <module>api-gateway</module>
    <module>auth-service</module>
    <module>user-microservice</module>
    <module>stripe-service</module>  <!-- Add this line -->
</modules>
```

### 2. Configure API Gateway

Add routes in your API Gateway to proxy requests to the stripe-service:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: stripe-service
          uri: http://localhost:8090
          predicates:
            - Path=/api/stripe/**
```

### 3. Environment Setup

Add the Stripe environment variables to your deployment configuration.

## Testing

Use the Stripe CLI to test webhooks locally:

```bash
# Install Stripe CLI
# https://stripe.com/docs/stripe-cli

# Forward webhooks to local service
stripe listen --forward-to localhost:8085/api/stripe/webhook

# Test with sample events
stripe trigger checkout.session.completed
```

## Security Considerations

- ✅ Never expose your Stripe secret key in client-side code
- ✅ Always verify webhook signatures in production
- ✅ Use HTTPS in production
- ✅ Implement proper authentication/authorization before production use
- ✅ Store sensitive configuration in secure environment variables

## Next Steps

1. **Add Authentication**: Integrate with your Keycloak auth-service
2. **Database Integration**: Store transactions and orders
3. **Connect to Catalog**: Link with service-catalog for product management
4. **Event Publishing**: Publish events to message queue for other services
5. **Monitoring**: Add metrics and logging for production

## Resources

- [Stripe Documentation](https://stripe.com/docs)
- [Stripe Connect](https://stripe.com/docs/connect)
- [Stripe Java SDK](https://github.com/stripe/stripe-java)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## License

Part of the IWA Project.
