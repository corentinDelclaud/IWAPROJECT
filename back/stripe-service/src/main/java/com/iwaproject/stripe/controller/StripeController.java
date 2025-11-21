package com.iwaproject.stripe.controller;

import com.iwaproject.stripe.dto.*;
import com.iwaproject.stripe.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;

    /**
     * Create a Connected Account
     * POST /api/stripe/connect-account
     */
    @PostMapping("/connect-account")
    public ResponseEntity<?> createConnectAccount(@RequestBody CreateConnectAccountRequest request) {
        try {
            CreateConnectAccountResponse response = stripeService.createConnectAccount(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating connect account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Create Account Link for onboarding
     * POST /api/stripe/account-link
     */
    @PostMapping("/account-link")
    public ResponseEntity<?> createAccountLink(@RequestBody CreateAccountLinkRequest request) {
        try {
            CreateAccountLinkResponse response = stripeService.createAccountLink(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating account link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Get Connected Account Status
     * GET /api/stripe/account-status/{accountId}
     */
    @GetMapping("/account-status/{accountId}")
    public ResponseEntity<?> getAccountStatus(@PathVariable String accountId) {
        try {
            AccountStatusResponse response = stripeService.getAccountStatus(accountId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error getting account status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Create a product and price
     * POST /api/stripe/product
     */
    @PostMapping("/product")
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRequest request) {
        try {
            CreateProductResponse response = stripeService.createProduct(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Fetch products for a specific account
     * GET /api/stripe/products/{accountId}
     */
    @GetMapping("/products/{accountId}")
    public ResponseEntity<?> getProducts(@PathVariable String accountId) {
        try {
            List<ProductDto> products = stripeService.getProducts(accountId);
            return ResponseEntity.ok(products);
        } catch (StripeException e) {
            log.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Create a checkout session
     * POST /api/stripe/checkout-session
     */
    @PostMapping("/checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest request) {
        try {
            CreateCheckoutSessionResponse response = stripeService.createCheckoutSession(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating checkout session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Subscribe to platform
     * POST /api/stripe/subscribe-platform
     */
    @PostMapping("/subscribe-platform")
    public ResponseEntity<?> subscribeToPlatform(@RequestBody SubscribeToPlatformRequest request) {
        try {
            SubscribeToPlatformResponse response = stripeService.subscribeToPlatform(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating platform subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        } catch (IllegalStateException e) {
            log.error("Platform price ID not configured", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }

    /**
     * Create a portal session
     * POST /api/stripe/portal-session
     */
    @PostMapping("/portal-session")
    public ResponseEntity<?> createPortalSession(@RequestBody CreatePortalSessionRequest request) {
        try {
            String url = stripeService.createPortalSession(request);
            // Redirect to the portal URL
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header("Location", url)
                    .build();
        } catch (StripeException e) {
            log.error("Error creating portal session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().error(e.getMessage()).build());
        }
    }
}
