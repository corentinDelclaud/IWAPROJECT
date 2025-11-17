package com.stripe.sample;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import com.stripe.model.Account;
import com.stripe.param.AccountCreateParams;
import com.stripe.model.PriceSearchResult;
import com.stripe.param.PriceSearchParams;
import com.stripe.model.StripeCollection;
import com.stripe.model.AccountLink;
import com.stripe.model.Product;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import com.stripe.model.Subscription;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.model.Price;
import com.stripe.param.PriceListParams;

import static spark.Spark.*;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();
        // Set port to 4242
        port(4242);

        Stripe.apiKey = dotenv.get("STRIPE_SECRET_KEY");

        // Set CORS headers for API endpoints
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            }

            response.header("Access-Control-Allow-Origin", "*");
            response.status(200);
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
            response.type("application/json");
        });

        // Create a sample product and return a price for it
        post("/api/create-product", (request, response) -> {
            String productName = parseRequestBody(request, "productName");
            String productDescription = parseRequestBody(request, "productDescription");
            Long productPrice = Long.parseLong(parseRequestBody(request, "productPrice"));
            String accountId = parseRequestBody(request, "accountId");

            try {
                Product product;
                Price price;

                // Set the request to be made on the connected account
                RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();

                // Create the product on the connected account
                ProductCreateParams productParams = ProductCreateParams.builder()
                    .setName(productName)
                    .setDescription(productDescription)
                    .build();
                product = Product.create(productParams, requestOptions);

                // Create a price for the product on the connected account
                PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(product.getId())
                    .setUnitAmount(productPrice)
                    .setCurrency("eur")
                    .build();
                price = Price.create(priceParams, requestOptions);

                return new Gson().toJson(Map.of(
                    "productName", productName,
                    "productDescription", productDescription,
                    "productPrice", productPrice,
                    "priceId", price.getId()
                ));
            } catch (StripeException e) {
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });

        // Create a Connected Account
        post("/api/create-connect-account", (request, response) -> {
            String email = parseRequestBody(request, "email");

            try {
                // Create a Connect account using v1 API (stable and works with localhost)
                AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("FR")
                    .setEmail(email)
                    .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                            .setCardPayments(
                                AccountCreateParams.Capabilities.CardPayments.builder()
                                    .setRequested(true)
                                    .build()
                            )
                            .setTransfers(
                                AccountCreateParams.Capabilities.Transfers.builder()
                                    .setRequested(true)
                                    .build()
                            )
                            .build()
                    )
                    .build();

                Account account = Account.create(params);
                return new Gson().toJson(Map.of("accountId", account.getId()));
            } catch (StripeException e) {
                e.printStackTrace(); // Log the error for debugging
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });

        // Create Account Link for onboarding
        post("/api/create-account-link", (request, response) -> {
            String accountId = parseRequestBody(request, "accountId");

            try {
                // Use v1 API which works with localhost
                AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(dotenv.get("DOMAIN") + "?refresh=true")
                    .setReturnUrl(dotenv.get("DOMAIN") + "?accountId=" + accountId)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

                AccountLink accountLink = AccountLink.create(params);
                return new Gson().toJson(Map.of("url", accountLink.getUrl()));
            } catch (StripeException e) {
                e.printStackTrace(); // Log the error for debugging
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });

        // Get Connected Account Status
        get("/api/account-status/:accountId", (request, response) -> {
            String accountId = request.params(":accountId");

            try {
                // Use v1 API to retrieve account
                Account account = Account.retrieve(accountId);

                boolean payoutsEnabled = account.getPayoutsEnabled() != null && account.getPayoutsEnabled();
                boolean chargesEnabled = account.getChargesEnabled() != null && account.getChargesEnabled();
                boolean detailsSubmitted = account.getDetailsSubmitted() != null && account.getDetailsSubmitted();

                return new Gson().toJson(Map.of(
                    "id", account.getId(),
                    "payoutsEnabled", payoutsEnabled,
                    "chargesEnabled", chargesEnabled,
                    "detailsSubmitted", detailsSubmitted
                ));
            } catch (StripeException e) {
                e.printStackTrace(); // Log the error for debugging
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });

        // Fetch products for a specific account
        get("/api/products/:accountId", (request, response) -> {
            String accountId = request.params(":accountId");

            try {
                List<Map<String, Object>> productsList = new ArrayList<>();
                PriceListParams params = PriceListParams.builder()
                    .setActive(true)
                    .setLimit(100L)
                    .addExpand("data.product")
                    .build();

                RequestOptions requestOptions = null;
                if (!accountId.equals("platform")) {
                    requestOptions = RequestOptions.builder()
                        .setStripeAccount(accountId)
                        .build();
                }

                StripeCollection<Price> prices = requestOptions != null
                    ? Price.list(params, requestOptions)
                    : Price.list(params);

                for (Price price : prices.getData()) {
                    Product product = (Product) price.getProductObject();
                    productsList.add(Map.of(
                        "id", product.getId(),
                        "name", product.getName(),
                        "description", product.getDescription(),
                        "price", price.getUnitAmount(),
                        "priceId", price.getId(),
                        "image", "https://i.imgur.com/6Mvijcm.png"
                    ));
                }

                return new Gson().toJson(productsList);
            } catch (StripeException e) {
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });
        // Create a subscription from the connected account to the platform
        post("/api/subscribe-to-platform", (request, response) -> {
            String accountId = parseRequestBody(request, "accountId");
            String priceId = dotenv.get("PLATFORM_PRICE_ID"); // Price ID created on the platform account

            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build()
                )
                // Pass the V2 Account ID
                .setCustomerAccount(accountId)
                // Defines where Stripe will redirect a customer after successful payment
                .setSuccessUrl(dotenv.get("DOMAIN") + "?session_id={CHECKOUT_SESSION_ID}&success=true")
                // Defines where Stripe will redirect if a customer cancels payment
                .setCancelUrl(dotenv.get("DOMAIN") + "?canceled=true")
                .build();

            Session session = Session.create(params);
            return new Gson().toJson(Map.of("url", session.getUrl()));
        });

        post("/api/create-checkout-session", (request, response) -> {
            String accountId = parseRequestBody(request, "accountId");
            String priceId = parseRequestBody(request, "priceId");

            try {
                // Get the price's type from Stripe
                Price price;
                RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();
                price = Price.retrieve(priceId, requestOptions);
                String priceType = price.getType();
                SessionCreateParams.Mode mode = priceType.equals("recurring") ?
                  SessionCreateParams.Mode.SUBSCRIPTION :
                  SessionCreateParams.Mode.PAYMENT;
                SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build()
                    )
                .setMode(mode)
                // Defines where Stripe will redirect a customer after successful payment
                .setSuccessUrl(dotenv.get("DOMAIN") + "/done?session_id={CHECKOUT_SESSION_ID}")
                // Defines where Stripe will redirect if a customer cancels payment
                .setCancelUrl(dotenv.get("DOMAIN"))
                ;

            // Add Connect-specific parameters based on payment mode
            if (mode == SessionCreateParams.Mode.SUBSCRIPTION) {
                SessionCreateParams.SubscriptionData.Builder subscriptionDataBuilder =
                    SessionCreateParams.SubscriptionData.builder();
                if (paramsBuilder.build().getSubscriptionData() != null) {
                    subscriptionDataBuilder.setTrialPeriodDays(
                        paramsBuilder.build().getSubscriptionData().getTrialPeriodDays()
                    );
                }
                // Application fee for subscriptions (removed deprecated method)
                paramsBuilder.setSubscriptionData(subscriptionDataBuilder.build());
            } else {
                paramsBuilder.setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .setApplicationFeeAmount(123L)
                        .build()
                );
            }

            RequestOptions checkoutRequestOptions = RequestOptions.builder()
                .setStripeAccount(accountId)
                .build();

            Session session = Session.create(paramsBuilder.build(), checkoutRequestOptions);

            // Return JSON instead of redirect for AJAX requests
            return new Gson().toJson(Map.of("url", session.getUrl()));
            } catch (StripeException e) {
                e.printStackTrace(); // Log the error for debugging
                response.status(500);
                return new Gson().toJson(Map.of("error", e.getMessage()));
            }
        });



        post("/api/create-portal-session", (request, response) -> {
            // Get the Stripe customer we previously created
            // Normally you'd fetch this from your database based on the authenticated user
            String sessionId = parseRequestBody(request, "session_id");
            Session checkoutSession = Session.retrieve(sessionId);
            com.stripe.param.billingportal.SessionCreateParams portalParams =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                // Set the customer_account to the V2 Account's ID
                .setCustomerAccount(checkoutSession.getCustomerAccount())
                .setReturnUrl(dotenv.get("DOMAIN") + "/?session_id=" + sessionId)
                .build();

            com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(portalParams);

            response.redirect(portalSession.getUrl(), 303);
            return "";
        });

        post("/api/webhook", (request, response) -> {
            String payload = request.body();
            String sigHeader = request.headers("Stripe-Signature");
            Event event = null;

            // Replace this endpoint secret with your endpoint's unique secret
            // If you are testing with the CLI, find the secret by running 'stripe listen'
            // If you are using an endpoint defined with the API or dashboard, look in your webhook settings
            // at https://dashboard.stripe.com/webhooks
            String endpointSecret = "";

            // Only verify the event if you have an endpoint secret defined.
            // Otherwise use the basic event deserialized with GSON.fromJson
            if (endpointSecret != null && !endpointSecret.isEmpty()) {
                try {
                    event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
                } catch (Exception e) {
                    response.status(400);
                    return "";
                }
            } else {
                // For testing without a real signature
                try {
                    event = Event.GSON.fromJson(payload, Event.class);
                } catch (Exception e) {
                    response.status(400);
                    return "";
                }
            }

            // Handle the event
            StripeObject stripeObject;
            String status;
            EventDataObjectDeserializer dataObjectDeserializer;

            switch (event.getType()) {
                case "customer.subscription.trial_will_end":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    if (dataObjectDeserializer.getObject().isPresent()) {
                        stripeObject = dataObjectDeserializer.getObject().get();
                        Subscription subscription = (Subscription) stripeObject;
                        status = subscription.getStatus();
                        System.out.println("Subscription status is " + status);
                        // Then define and call a method to handle the subscription trial ending.
                        // handleSubscriptionTrialEnding(subscription);
                    }
                    break;
                case "customer.subscription.deleted":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    if (dataObjectDeserializer.getObject().isPresent()) {
                        stripeObject = dataObjectDeserializer.getObject().get();
                        Subscription subscription = (Subscription) stripeObject;
                        status = subscription.getStatus();
                        System.out.println("Subscription status is " + status);
                        // Then define and call a method to handle the subscription deleted.
                        // handleSubscriptionDeleted(subscription);
                    }
                    break;
                case "checkout.session.completed":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    if (dataObjectDeserializer.getObject().isPresent()) {
                        stripeObject = dataObjectDeserializer.getObject().get();
                        Session session = (Session) stripeObject;
                        status = session.getStatus();
                        System.out.println("Checkout Session status is " + status);
                        // Then define and call a method to handle the checkout session completed.
                        // handleCheckoutSessionCompleted(session);
                    }
                    break;
                case "checkout.session.async_payment_failed":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    if (dataObjectDeserializer.getObject().isPresent()) {
                        stripeObject = dataObjectDeserializer.getObject().get();
                        Session session = (Session) stripeObject;
                        status = session.getStatus();
                        System.out.println("Checkout Session status is " + status);
                        // Then define and call a method to handle the checkout session failed.
                        // handleCheckoutSessionFailed(session);
                    }
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }

            response.status(200);
            return "";
        });
    }

    // Helper method to parse request body (JSON or form data)
    private static String parseRequestBody(spark.Request request, String paramName) {
        // First try to get the parameter from form data
        String paramValue = request.queryParams(paramName);

        // If not found in form data, try to parse from JSON body
        if (paramValue == null || paramValue.isEmpty()) {
            com.google.gson.JsonObject jsonObject = new Gson().fromJson(request.body(), com.google.gson.JsonObject.class);
            paramValue = jsonObject.get(paramName).getAsString();
        }

        return paramValue;
    }
}

