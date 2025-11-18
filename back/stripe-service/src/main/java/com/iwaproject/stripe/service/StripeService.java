package com.iwaproject.stripe.service;

import com.iwaproject.stripe.config.StripeConfig;
import com.iwaproject.stripe.dto.*;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeConfig stripeConfig;

    /**
     * Create a Stripe Connect account
     */
    public CreateConnectAccountResponse createConnectAccount(CreateConnectAccountRequest request) throws StripeException {
        log.info("Creating Connect account for email: {}", request.getEmail());
        
        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry("FR")
                .setEmail(request.getEmail())
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
        log.info("Created Connect account with ID: {}", account.getId());
        
        return CreateConnectAccountResponse.builder()
                .accountId(account.getId())
                .build();
    }

    /**
     * Create an Account Link for onboarding
     */
    public CreateAccountLinkResponse createAccountLink(CreateAccountLinkRequest request) throws StripeException {
        log.info("Creating account link for account: {}", request.getAccountId());
        
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(request.getAccountId())
                .setRefreshUrl(stripeConfig.getDomain() + "?refresh=true")
                .setReturnUrl(stripeConfig.getDomain() + "?accountId=" + request.getAccountId())
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        log.info("Created account link: {}", accountLink.getUrl());
        
        return CreateAccountLinkResponse.builder()
                .url(accountLink.getUrl())
                .build();
    }

    /**
     * Get Connected Account Status
     */
    public AccountStatusResponse getAccountStatus(String accountId) throws StripeException {
        log.info("Getting account status for: {}", accountId);
        
        Account account = Account.retrieve(accountId);

        boolean payoutsEnabled = account.getPayoutsEnabled() != null && account.getPayoutsEnabled();
        boolean chargesEnabled = account.getChargesEnabled() != null && account.getChargesEnabled();
        boolean detailsSubmitted = account.getDetailsSubmitted() != null && account.getDetailsSubmitted();

        return AccountStatusResponse.builder()
                .id(account.getId())
                .payoutsEnabled(payoutsEnabled)
                .chargesEnabled(chargesEnabled)
                .detailsSubmitted(detailsSubmitted)
                .build();
    }

    /**
     * Create a product and price on a connected account
     */
    public CreateProductResponse createProduct(CreateProductRequest request) throws StripeException {
        log.info("Creating product '{}' on account: {}", request.getProductName(), request.getAccountId());
        
        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(request.getAccountId())
                .build();

        // Create the product
        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(request.getProductName())
                .setDescription(request.getProductDescription())
                .build();
        Product product = Product.create(productParams, requestOptions);

        // Create a price for the product
        PriceCreateParams priceParams = PriceCreateParams.builder()
                .setProduct(product.getId())
                .setUnitAmount(request.getProductPrice())
                .setCurrency("eur")
                .build();
        Price price = Price.create(priceParams, requestOptions);

        log.info("Created product {} with price {}", product.getId(), price.getId());

        return CreateProductResponse.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .productPrice(request.getProductPrice())
                .priceId(price.getId())
                .build();
    }

    /**
     * Fetch products for a specific account
     */
    public List<ProductDto> getProducts(String accountId) throws StripeException {
        log.info("Fetching products for account: {}", accountId);
        
        List<ProductDto> productsList = new ArrayList<>();
        
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
            productsList.add(ProductDto.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(price.getUnitAmount())
                    .priceId(price.getId())
                    .image("https://i.imgur.com/6Mvijcm.png") // Default image
                    .build());
        }

        log.info("Found {} products for account: {}", productsList.size(), accountId);
        return productsList;
    }

    /**
     * Create a checkout session
     */
    public CreateCheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) throws StripeException {
        log.info("Creating checkout session for account: {}, price: {}", request.getAccountId(), request.getPriceId());
        
        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(request.getAccountId())
                .build();

        // Get the price's type from Stripe
        Price price = Price.retrieve(request.getPriceId(), requestOptions);
        String priceType = price.getType();
        SessionCreateParams.Mode mode = priceType.equals("recurring") 
                ? SessionCreateParams.Mode.SUBSCRIPTION 
                : SessionCreateParams.Mode.PAYMENT;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(request.getPriceId())
                                .setQuantity(1L)
                                .build()
                )
                .setMode(mode)
                .setSuccessUrl(stripeConfig.getFrontendUrl() + "/done?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeConfig.getFrontendUrl());

        // Add Connect-specific parameters based on payment mode
        if (mode == SessionCreateParams.Mode.SUBSCRIPTION) {
            paramsBuilder.setSubscriptionData(
                    SessionCreateParams.SubscriptionData.builder().build()
            );
        } else {
            paramsBuilder.setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                            .setApplicationFeeAmount(123L)
                            .build()
            );
        }

        Session session = Session.create(paramsBuilder.build(), requestOptions);
        log.info("Created checkout session: {}", session.getId());

        return CreateCheckoutSessionResponse.builder()
                .url(session.getUrl())
                .build();
    }

    /**
     * Subscribe to platform
     */
    public SubscribeToPlatformResponse subscribeToPlatform(SubscribeToPlatformRequest request) throws StripeException {
        log.info("Creating platform subscription for account: {}", request.getAccountId());
        
        String priceId = stripeConfig.getPlatformPriceId();
        if (priceId == null || priceId.isEmpty()) {
            throw new IllegalStateException("Platform price ID not configured");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setCustomerAccount(request.getAccountId())
                .setSuccessUrl(stripeConfig.getFrontendUrl() + "?session_id={CHECKOUT_SESSION_ID}&success=true")
                .setCancelUrl(stripeConfig.getFrontendUrl() + "?canceled=true")
                .build();

        Session session = Session.create(params);
        log.info("Created platform subscription session: {}", session.getId());

        return SubscribeToPlatformResponse.builder()
                .url(session.getUrl())
                .build();
    }

    /**
     * Create a portal session for managing subscriptions
     */
    public String createPortalSession(CreatePortalSessionRequest request) throws StripeException {
        log.info("Creating portal session for checkout session: {}", request.getSessionId());
        
        Session checkoutSession = Session.retrieve(request.getSessionId());
        
        com.stripe.param.billingportal.SessionCreateParams portalParams =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomerAccount(checkoutSession.getCustomerAccount())
                        .setReturnUrl(stripeConfig.getFrontendUrl() + "/?session_id=" + request.getSessionId())
                        .build();

        com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(portalParams);

        log.info("Created portal session: {}", portalSession.getUrl());
        return portalSession.getUrl();
    }
}
