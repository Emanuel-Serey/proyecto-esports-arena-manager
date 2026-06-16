package cl.duoc.esports.rankingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rankingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ranking Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de rankings del sistema eSports Arena Manager"));
    }
}