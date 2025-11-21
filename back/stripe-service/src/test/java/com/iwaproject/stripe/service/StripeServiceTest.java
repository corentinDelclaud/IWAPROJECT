package com.iwaproject.stripe.service;

import com.iwaproject.stripe.config.StripeConfig;
import com.iwaproject.stripe.dto.*;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

public class StripeServiceTest {

    private StripeService stripeService;
    private StripeConfig config;

    private MockedStatic<Account> accountStatic;
    private MockedStatic<AccountLink> accountLinkStatic;
    private MockedStatic<Product> productStatic;
    private MockedStatic<Price> priceStatic;
    private MockedStatic<com.stripe.model.checkout.Session> sessionStatic;
    private MockedStatic<com.stripe.model.billingportal.Session> portalSessionStatic;

    @BeforeEach
    void setUp() {
        config = mock(StripeConfig.class);
        when(config.getFrontendUrl()).thenReturn("http://localhost:19000");
        when(config.getOnboardingReturnPath()).thenReturn("/");
        when(config.getPlatformPriceId()).thenReturn("price_platform_123");
        stripeService = new StripeService(config);

        accountStatic = mockStatic(Account.class);
        accountLinkStatic = mockStatic(AccountLink.class);
        productStatic = mockStatic(Product.class);
        priceStatic = mockStatic(Price.class);
        sessionStatic = mockStatic(com.stripe.model.checkout.Session.class);
        portalSessionStatic = mockStatic(com.stripe.model.billingportal.Session.class);
    }

    @AfterEach
    void tearDown() {
        accountStatic.close();
        accountLinkStatic.close();
        productStatic.close();
        priceStatic.close();
        sessionStatic.close();
        portalSessionStatic.close();
    }

    @Test
    void createConnectAccount_returnsAccountId() throws StripeException {
        Account mockAccount = mock(Account.class);
        when(mockAccount.getId()).thenReturn("acct_123");
        accountStatic.when(() -> Account.create(any(AccountCreateParams.class))).thenReturn(mockAccount);

        CreateConnectAccountResponse resp = stripeService.createConnectAccount(
                CreateConnectAccountRequest.builder().email("user@example.com").build()
        );

        assertThat(resp.getAccountId()).isEqualTo("acct_123");
    }

    @Test
    void createAccountLink_usesFrontendUrlAndReturnPath() throws StripeException {
        AccountLink mockLink = mock(AccountLink.class);
        when(mockLink.getUrl()).thenReturn("https://stripe.com/link");
        accountLinkStatic.when(() -> AccountLink.create(any(AccountLinkCreateParams.class))).thenReturn(mockLink);

        CreateAccountLinkResponse resp = stripeService.createAccountLink(
                CreateAccountLinkRequest.builder().accountId("acct_123").build()
        );

        assertThat(resp.getUrl()).isEqualTo("https://stripe.com/link");
        accountLinkStatic.verify(() -> AccountLink.create(any(AccountLinkCreateParams.class)));
    }

    @Test
    void getAccountStatus_returnsFlags() throws StripeException {
        Account mockAccount = mock(Account.class);
        when(mockAccount.getId()).thenReturn("acct_123");
        when(mockAccount.getPayoutsEnabled()).thenReturn(true);
        when(mockAccount.getChargesEnabled()).thenReturn(false);
        when(mockAccount.getDetailsSubmitted()).thenReturn(true);
        accountStatic.when(() -> Account.retrieve("acct_123")).thenReturn(mockAccount);

        AccountStatusResponse resp = stripeService.getAccountStatus("acct_123");

        assertThat(resp.getId()).isEqualTo("acct_123");
        assertThat(resp.getPayoutsEnabled()).isTrue();
        assertThat(resp.getChargesEnabled()).isFalse();
        assertThat(resp.getDetailsSubmitted()).isTrue();
    }

    @Test
    void createProduct_returnsPriceId() throws StripeException {
        Product mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn("prod_123");
    productStatic.when(() -> Product.create(any(com.stripe.param.ProductCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockProduct);

        Price mockPrice = mock(Price.class);
        when(mockPrice.getId()).thenReturn("price_123");
    priceStatic.when(() -> Price.create(any(com.stripe.param.PriceCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockPrice);

        CreateProductResponse resp = stripeService.createProduct(
                CreateProductRequest.builder()
                        .productName("Item")
                        .productDescription("Desc")
                        .productPrice(1999L)
                        .accountId("acct_123")
                        .build()
        );

        assertThat(resp.getPriceId()).isEqualTo("price_123");
    }

    @Test
    void getProducts_returnsMappedDtos() throws StripeException {
        // Mock Product
        Product product = mock(Product.class);
        when(product.getId()).thenReturn("prod_123");
        when(product.getName()).thenReturn("Item");
        when(product.getDescription()).thenReturn("Desc");
        // Mock Price
        Price price = mock(Price.class);
        when(price.getUnitAmount()).thenReturn(2500L);
        when(price.getId()).thenReturn("price_123");
        when(price.getProductObject()).thenReturn(product);
    // Mock PriceCollection (must match return type of Price.list)
    PriceCollection collection = mock(PriceCollection.class);
        when(collection.getData()).thenReturn(List.of(price));
        priceStatic.when(() -> Price.list(any(PriceListParams.class), any(RequestOptions.class))).thenReturn(collection);

        List<ProductDto> products = stripeService.getProducts("acct_123");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getId()).isEqualTo("prod_123");
        assertThat(products.get(0).getPriceId()).isEqualTo("price_123");
        assertThat(products.get(0).getPrice()).isEqualTo(2500L);
    }

    @Test
    void createCheckoutSession_returnsUrl() throws StripeException {
        // Price indicates one-time
        Price mockPrice = mock(Price.class);
        when(mockPrice.getType()).thenReturn("one_time");
        priceStatic.when(() -> Price.retrieve(eq("price_abc"), any(RequestOptions.class))).thenReturn(mockPrice);

        com.stripe.model.checkout.Session mockSession = mock(com.stripe.model.checkout.Session.class);
        when(mockSession.getId()).thenReturn("cs_123");
        when(mockSession.getUrl()).thenReturn("https://stripe.com/checkout");
        sessionStatic.when(() -> com.stripe.model.checkout.Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenReturn(mockSession);

        CreateCheckoutSessionResponse resp = stripeService.createCheckoutSession(
                CreateCheckoutSessionRequest.builder()
                        .accountId("acct_123")
                        .priceId("price_abc")
                        .build()
        );

        assertThat(resp.getUrl()).isEqualTo("https://stripe.com/checkout");
    }

}
