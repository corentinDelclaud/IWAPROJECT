package com.iwaproject.stripe.service;

import com.iwaproject.stripe.config.StripeConfig;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final StripeConfig stripeConfig;

    /**
     * Process webhook events from Stripe
     */
    public void processWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = null;
        String endpointSecret = stripeConfig.getWebhookSecret();

        // Verify the event if webhook secret is configured
        if (endpointSecret != null && !endpointSecret.isEmpty()) {
            try {
                event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            } catch (SignatureVerificationException e) {
                log.error("Webhook signature verification failed: {}", e.getMessage());
                throw e;
            }
        } else {
            // For testing without a real signature
            try {
                event = Event.GSON.fromJson(payload, Event.class);
            } catch (Exception e) {
                log.error("Failed to parse webhook event: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid webhook payload");
            }
        }

        // Handle the event
        handleEvent(event);
    }

    private void handleEvent(Event event) {
        log.info("Processing webhook event: {} (type: {})", event.getId(), event.getType());

        EventDataObjectDeserializer dataObjectDeserializer;
        StripeObject stripeObject;

        switch (event.getType()) {
            case "customer.subscription.trial_will_end":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Subscription subscription = (Subscription) stripeObject;
                    handleSubscriptionTrialWillEnd(subscription);
                }
                break;

            case "customer.subscription.deleted":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Subscription subscription = (Subscription) stripeObject;
                    handleSubscriptionDeleted(subscription);
                }
                break;

            case "customer.subscription.created":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Subscription subscription = (Subscription) stripeObject;
                    handleSubscriptionCreated(subscription);
                }
                break;

            case "customer.subscription.updated":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Subscription subscription = (Subscription) stripeObject;
                    handleSubscriptionUpdated(subscription);
                }
                break;

            case "checkout.session.completed":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Session session = (Session) stripeObject;
                    handleCheckoutSessionCompleted(session);
                }
                break;

            case "checkout.session.async_payment_succeeded":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Session session = (Session) stripeObject;
                    handleCheckoutSessionAsyncPaymentSucceeded(session);
                }
                break;

            case "checkout.session.async_payment_failed":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    Session session = (Session) stripeObject;
                    handleCheckoutSessionAsyncPaymentFailed(session);
                }
                break;

            case "payment_intent.succeeded":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    handlePaymentIntentSucceeded(paymentIntent);
                }
                break;

            case "payment_intent.payment_failed":
                dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    handlePaymentIntentFailed(paymentIntent);
                }
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleSubscriptionTrialWillEnd(Subscription subscription) {
        log.info("Subscription trial will end - ID: {}, Status: {}", 
                subscription.getId(), subscription.getStatus());
        // TODO: Implement business logic - send notification, update database, etc.
    }

    private void handleSubscriptionDeleted(Subscription subscription) {
        log.info("Subscription deleted - ID: {}, Status: {}", 
                subscription.getId(), subscription.getStatus());
        // TODO: Implement business logic - revoke access, update database, etc.
    }

    private void handleSubscriptionCreated(Subscription subscription) {
        log.info("Subscription created - ID: {}, Status: {}", 
                subscription.getId(), subscription.getStatus());
        // TODO: Implement business logic - grant access, update database, etc.
    }

    private void handleSubscriptionUpdated(Subscription subscription) {
        log.info("Subscription updated - ID: {}, Status: {}", 
                subscription.getId(), subscription.getStatus());
        // TODO: Implement business logic - adjust access level, update database, etc.
    }

    private void handleCheckoutSessionCompleted(Session session) {
        log.info("Checkout session completed - ID: {}, Status: {}, Mode: {}", 
                session.getId(), session.getStatus(), session.getMode());
        // TODO: Implement business logic - fulfill order, update database, send confirmation, etc.
    }

    private void handleCheckoutSessionAsyncPaymentSucceeded(Session session) {
        log.info("Checkout session async payment succeeded - ID: {}", session.getId());
        // TODO: Implement business logic - fulfill order, update database, etc.
    }

    private void handleCheckoutSessionAsyncPaymentFailed(Session session) {
        log.info("Checkout session async payment failed - ID: {}", session.getId());
        // TODO: Implement business logic - notify customer, update order status, etc.
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        log.info("Payment intent succeeded - ID: {}, Amount: {}", 
                paymentIntent.getId(), paymentIntent.getAmount());
        // TODO: Implement business logic - update order status, etc.
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        log.info("Payment intent failed - ID: {}, Amount: {}", 
                paymentIntent.getId(), paymentIntent.getAmount());
        // TODO: Implement business logic - notify customer, retry payment, etc.
    }
}
