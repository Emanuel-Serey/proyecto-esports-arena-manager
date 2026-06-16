package cl.duoc.esports.resultservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI resultServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Result Service API")
                        .version("1.0")
                        .description("Documentación de endpoints del microservicio de resultados del sistema eSports Arena Manager"));
    }
}
