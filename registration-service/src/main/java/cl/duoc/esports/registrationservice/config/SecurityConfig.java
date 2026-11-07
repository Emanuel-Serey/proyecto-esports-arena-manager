package cl.duoc.esports.registrationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("rol");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Inscripciones visibles de un torneo
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones/torneo/**",
                                "/api/v2/inscripciones/torneo/**"
                        ).permitAll()

                        // Listado completo: solo gestión
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones",
                                "/api/v2/inscripciones"
                        ).hasAnyRole("ADMIN", "ORGANIZADOR")

                        // Aceptar / rechazar / cambiar estado
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/inscripciones/*/estado/*",
                                "/api/v2/inscripciones/*/estado/*"
                        ).hasAnyRole("ADMIN", "ORGANIZADOR")

                        // Crear inscripción
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inscripciones",
                                "/api/v2/inscripciones"
                        ).hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        // Cancelar: después validaremos propiedad
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/inscripciones/*/cancelar",
                                "/api/v2/inscripciones/*/cancelar"
                        ).hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        // Consultas individuales
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones/*",
                                "/api/inscripciones/jugador/**",
                                "/api/inscripciones/equipo/**",
                                "/api/v2/inscripciones/*",
                                "/api/v2/inscripciones/jugador/**",
                                "/api/v2/inscripciones/equipo/**"
                        ).hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
}