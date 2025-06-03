package com.tedu.seniorproject.ecomap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:4444");
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setName("EcoMap Team");
        contact.setEmail("contact@ecomap.com");
        contact.setUrl("https://www.ecomap.com");

        License license = new License().name("MIT License").url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("EcoMap API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for EcoMap application.")
                .license(license);

        // Define the security scheme (Bearer Token)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Define a security requirement to use the security scheme globally
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .addSecurityItem(securityRequirement)
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }
} 