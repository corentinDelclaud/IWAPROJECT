package iwaproject.user_microservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple HTTP client to call the stripe-service endpoints.
 * Uses RestTemplate to POST /api/stripe/connect-account and returns accountId.
 */
@Component
@Slf4j
public class StripeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stripe.service.url:http://localhost:8090}")
    private String stripeServiceUrl;

    public String createConnectAccount(String email) {
        try {
            String url = stripeServiceUrl + "/api/stripe/connect-account";
            Map<String, String> body = new HashMap<>();
            body.put("email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            log.info("Calling Stripe service to create connect account for email {} at {}", email, url);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, request, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object accountId = resp.getBody().get("accountId");
                if (accountId != null) {
                    return accountId.toString();
                }
            }
            log.warn("Unexpected response from Stripe service when creating account for {}: status={}, body={}",
                    email, resp.getStatusCode(), resp.getBody());
        } catch (Exception e) {
            log.error("Error calling stripe-service to create connect account: {}", e.getMessage(), e);
        }
        return null;
    }
}
