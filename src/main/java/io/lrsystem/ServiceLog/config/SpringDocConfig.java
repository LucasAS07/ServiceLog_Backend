package io.lrsystem.ServiceLog.config;

import io.lrsystem.ServiceLog.dto.CustomError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringDocConfig {


    @Bean
    public OpenAPI openAPI() {

        final String schemeName = "bearerAuth";

        Components components = new Components()
                .addSecuritySchemes(
                        schemeName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                )
                .schemas(gerarSchemas());

        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .info(new Info()
                        .title("ServiceLog API")
                        .version("V1")
                        .description("REST API do ServiceLog")
                );
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            openApi.getPaths()
                    .values()
                    .forEach(pathItem -> pathItem.readOperationsMap()
                            .forEach((httpMethod, operation) -> {
                                ApiResponses responses = operation.getResponses();
                                switch (httpMethod) {
                                    case GET:
                                        responses.addApiResponse("404", new ApiResponse()
                                                .description("Recurso não encontrado"));
                                        responses.addApiResponse("406", new ApiResponse()
                                                .description("Recurso não possui representação aceitavel"));
                                        responses.addApiResponse("500", new ApiResponse()
                                                .description("Erro interno do servidor"));
                                        break;
                                    case POST:
                                        responses.addApiResponse("400", new ApiResponse()
                                                .description("Requisição invalida"));
                                        responses.addApiResponse("500", new ApiResponse()
                                                .description("Erro interno do servidor"));
                                        break;
                                    case PUT:
                                        responses.addApiResponse("404", new ApiResponse()
                                                .description("Recurso não encontrado"));
                                        responses.addApiResponse("400", new ApiResponse()
                                                .description("Requisição invalida"));
                                        responses.addApiResponse("500", new ApiResponse()
                                                .description("Erro interno do servidor"));
                                        break;
                                    case DELETE:
                                        responses.addApiResponse("404", new ApiResponse()
                                                .description("Recurso não encontrado"));
                                        responses.addApiResponse("500", new ApiResponse()
                                                .description("Erro interno do servidor"));
                                        break;
                                    default:
                                        responses.addApiResponse("500", new ApiResponse()
                                                .description("Erro interno do servidor"));
                                }
                            }));
        };
    }

    private Map<String, Schema> gerarSchemas() {
        final Map<String,Schema> schemaMap = new HashMap<>();

        Map<String,Schema> problemSchema = ModelConverters.getInstance().read(CustomError.class);

        schemaMap.putAll(problemSchema);

        return schemaMap;
    }

}
