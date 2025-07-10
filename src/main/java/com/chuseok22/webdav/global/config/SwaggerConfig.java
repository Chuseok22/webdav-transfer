package com.chuseok22.webdav.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "📁 WebDAV Cloud Transfer",
        description = """
                        ### "WebDAV Cloud Transfer"
                        #### [Github](https://github.com/Chuseok22/webdav-transfer)""",
        version = "1.0v"
    ),
    servers = {
        @Server(url = "https://webdav.chuseok22.com", description = "메인 서버"),
        @Server(url = "https://webdav-test.chuseok22.com", description = "테스트 서버"),
        @Server(url = "http://localhost:8080", description = "로컬 서버")
    }
)
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme apiKey = new SecurityScheme()
        .type(Type.HTTP)
        .in(In.HEADER)
        .name("Authorization")
        .scheme("bearer")
        .bearerFormat("JWT");

    SecurityRequirement securityRequirement = new SecurityRequirement()
        .addList("Bearer Token");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
        .addSecurityItem(securityRequirement)
        .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:8080")
                    .description("로컬 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://webdav-test.chuseok22.com")
                    .description("테스트 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://webdav.chuseok22.com")
                    .description("메인 서버")
            )
        );
  }
}