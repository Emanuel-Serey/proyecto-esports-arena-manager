package cl.duoc.esports.gameservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gameServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Game Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de juegos del sistema eSports Arena Manager"));
    }
}