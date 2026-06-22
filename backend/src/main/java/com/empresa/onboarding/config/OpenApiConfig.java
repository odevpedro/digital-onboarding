package com.empresa.onboarding.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Onboarding API")
                        .description("API do monolito de onboarding digital PF/PJ\n\n" +
                                "Fluxo completo: dados pessoais -> documentos -> compliance -> risco -> " +
                                "consentimento Open Finance -> integracao NucleoValidacao -> criacao de conta -> ativacao")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banco Digital")
                                .email("dev@bancodigital.com.br"))
                        .license(new License()
                                .name("Proprietaria")
                                .url("https://bancodigital.com.br")));
    }
}
