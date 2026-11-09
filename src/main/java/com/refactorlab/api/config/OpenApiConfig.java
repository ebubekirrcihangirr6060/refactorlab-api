package com.refactorlab.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger UI'da "Authorize" butonu ile HTTP Basic giriş yapılabilsin
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI refactorLabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RefactorLab API")
                        .version("1.0.0")
                        .description("Legacy kod ve SQL sorgularında kod kokusu tespiti, refactoring önerisi ve modernizasyon analizi"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
