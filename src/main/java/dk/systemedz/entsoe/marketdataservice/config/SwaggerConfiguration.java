package dk.systemedz.entsoe.marketdataservice.config;

import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties properties) {
        properties.setPath("/api-docs");
        properties.setDefaultModelsExpandDepth(-1);
        return properties;
    }
}
