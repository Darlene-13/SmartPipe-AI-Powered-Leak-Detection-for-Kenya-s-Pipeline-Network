package io.github.darlene.leakdetectionapplication.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger UI configuration.
 * Accessible at http://localhost:8080/swagger-ui/index.html
 * Configures JWT bearer token authentication for API testing.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Pipeline Leak Detection API")
                        .description(
                                "Intelligent Leak Detection System for Copper " +
                                        "Tailings Pipelines — REST API Documentation. " +
                                        "JKUAT Final Year Project 2026.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Darlene Wendy & Franklin Oginga")
                                .email("darlenewendie@gmail.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Enter JWT token obtained from " +
                                                        "POST /api/auth/login")));
    }
}