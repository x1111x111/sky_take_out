package com.example.skyserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("苍穹外卖接口文档")
                        .version("1.0")
                        .description("苍穹外卖后端 API 接口文档"))

                .components(new Components()
                        .addSecuritySchemes(
                                "token",
                                new SecurityScheme()
                                        .name("token")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                        )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("token")
                );
    }
}