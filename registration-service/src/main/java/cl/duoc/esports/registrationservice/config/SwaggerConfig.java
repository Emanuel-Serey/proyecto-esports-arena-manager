package cl.duoc.esports.registrationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI registrationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Registration Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de inscripciones del sistema eSports Arena Manager"));
    }
}