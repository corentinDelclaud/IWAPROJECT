package iwaproject.transaction.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    public String getUserIdFromToken() {
        log.info("#debuglog Extracting user ID from JWT token");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                log.error("#debuglog No valid JWT token found in security context");
                throw new IllegalStateException("No valid JWT token found");
            }
            
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getClaimAsString("sub");
            
            if (userId == null || userId.isEmpty()) {
                log.error("#debuglog JWT token does not contain 'sub' claim");
                throw new IllegalStateException("JWT token does not contain user ID");
            }
            
            log.info("#debuglog User ID extracted successfully: {}", userId);
            return userId;
            
        } catch (Exception e) {
            log.error("#debuglog Failed to extract user ID from JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract user ID from token", e);
        }
    }
}