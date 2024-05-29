package com.SunuBtrust360_Enrol.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "GAINDE 2000",
                        email = "abc@gainde2000.sn",
                        url = "https://www.gainde2000.com/"
                ),
                description = "L'API d'enrôlement de SunuBtrust360 permet aux utilisateurs de fpournir leurs informations et de recevoir un certificat  basé sur la PKI (Infrastructure à Clé Publique). Cette API offre des endpoints pour l'inscription des utilisateurs, la génération de certificats et d'autres fonctionnalités liées à la sécurité.",
                title = "API d'enrolement ",
                version = "1.0"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)

public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.SunuBtrust360_Enrol.controller"))
                .paths(PathSelectors.any())
                .build()
                .tags(
                        new Tag("Signataire", "Opérations relatives aux utilisateurs")
                );
    }

}