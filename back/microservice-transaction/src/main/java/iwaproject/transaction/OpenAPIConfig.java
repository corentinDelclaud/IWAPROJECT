package iwaproject.transaction;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Transaction API",
        version = "1.0",
        description = "API pour la gestion des transactions"
    )
)
public class OpenAPIConfig {
}