package com.refactorlab.api.config;

import com.refactorlab.api.rest.RefactorLabErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
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

    // Ortak JSON hata modelini (RefactorLabErrorResponse) dokümana ekler ve
    // her /api işlemine 400 / 401 / 403 / 404 cevaplarını bu modelle belgeler
    @Bean
    public OpenApiCustomizer errorResponsesCustomizer() {
        return theOpenApi -> {
            ModelConverters.getInstance().read(RefactorLabErrorResponse.class)
                    .forEach(theOpenApi.getComponents()::addSchemas);
            Schema<?> theErrorSchema = new Schema<>().$ref("#/components/schemas/RefactorLabErrorResponse");

            theOpenApi.getPaths().forEach((thePath, thePathItem) -> {
                if (!thePath.startsWith("/api/")) {
                    return;
                }
                for (Operation theOperation : thePathItem.readOperations()) {
                    addError(theOperation, "400", "Geçersiz istek (doğrulama, bozuk JSON, yanlış id formatı)", theErrorSchema);
                    addError(theOperation, "401", "Kimlik bilgisi yok veya yanlış", theErrorSchema);
                    addError(theOperation, "403", "Rol yetkisi yetersiz", theErrorSchema);
                    if (thePath.contains("{")) {
                        addError(theOperation, "404", "Kayıt bulunamadı", theErrorSchema);
                    }
                }
            });
        };
    }

    private void addError(Operation theOperation, String theCode, String theDescription, Schema<?> theSchema) {
        if (!theOperation.getResponses().containsKey(theCode)) {
            theOperation.getResponses().addApiResponse(theCode, new ApiResponse()
                    .description(theDescription)
                    .content(new Content().addMediaType("application/json", new MediaType().schema(theSchema))));
        }
    }
}
