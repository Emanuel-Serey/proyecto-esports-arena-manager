package cl.duoc.esports.tournamentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tournamentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tournament Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de torneos del sistema eSports Arena Manager"));
    }
}