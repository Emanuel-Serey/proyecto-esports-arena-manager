package cl.duoc.esports.teamservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI teamServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Team Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de equipos del sistema eSports Arena Manager"));
    }
}