package cl.duoc.esports.matchservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI matchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Match Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de partidas del sistema eSports Arena Manager"));
    }
}