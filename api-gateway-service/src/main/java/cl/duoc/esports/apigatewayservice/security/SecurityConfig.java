package cl.duoc.esports.apigatewayservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String rol =
                    jwt.getClaimAsString("rol");

            if (rol == null || rol.isBlank()) {
                return List.of();
            }

            Collection<GrantedAuthority> authorities =
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + rol
                            )
                    );

            return authorities;
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "https://proyecto-esports-frontend-react-bice.vercel.app"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {

        return http

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .csrf(
                        AbstractHttpConfigurer::disable
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()


                        // AUTENTICACION
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro"
                        )
                        .permitAll()


                        // SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        )
                        .permitAll()


                        // CUENTAS
                        .requestMatchers(
                                "/api/auth/cuentas/**"
                        )
                        .hasRole("ADMIN")


                        // USUARIOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios/me"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/usuarios/me"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios/rol/JUGADOR"
                        )
                        .authenticated()

                        .requestMatchers(
                                "/api/usuarios/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v2/usuarios/**"
                        )
                        .hasRole("ADMIN")


                        // JUEGOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/juegos",
                                "/api/juegos/",
                                "/api/juegos/**",
                                "/api/v2/juegos",
                                "/api/v2/juegos/",
                                "/api/v2/juegos/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/juegos/**",
                                "/api/v2/juegos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/juegos/**",
                                "/api/v2/juegos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/juegos/**",
                                "/api/v2/juegos/**"
                        )
                        .hasRole("ADMIN")


                        // TORNEOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/torneos",
                                "/api/torneos/",
                                "/api/torneos/**",
                                "/api/v2/torneos",
                                "/api/v2/torneos/",
                                "/api/v2/torneos/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/torneos",
                                "/api/torneos/",
                                "/api/torneos/**",
                                "/api/v2/torneos",
                                "/api/v2/torneos/",
                                "/api/v2/torneos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/torneos/**",
                                "/api/v2/torneos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/torneos/**",
                                "/api/v2/torneos/**"
                        )
                        .hasRole("ADMIN")


                        // EQUIPOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/equipos/*"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/equipos/**",
                                "/api/v2/equipos/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/equipos/**",
                                "/api/v2/equipos/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/equipos/**",
                                "/api/v2/equipos/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/equipos/**",
                                "/api/v2/equipos/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )


                        // INSCRIPCIONES
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones/torneo/**",
                                "/api/v2/inscripciones/torneo/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones",
                                "/api/v2/inscripciones"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/inscripciones/*/estado/*",
                                "/api/v2/inscripciones/*/estado/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inscripciones",
                                "/api/v2/inscripciones"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/inscripciones/*/cancelar",
                                "/api/v2/inscripciones/*/cancelar"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inscripciones/*",
                                "/api/inscripciones/jugador/**",
                                "/api/inscripciones/equipo/**",
                                "/api/v2/inscripciones/*",
                                "/api/v2/inscripciones/jugador/**",
                                "/api/v2/inscripciones/equipo/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR",
                                "JUGADOR"
                        )


                        // SANCIONES
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/sanciones/me"
                        )
                        .authenticated()

                        .requestMatchers(
                                "/api/sanciones/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                "/api/v2/sanciones/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )


                        // PARTIDAS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/partidas/**",
                                "/api/v2/partidas/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/partidas/**",
                                "/api/v2/partidas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/partidas/**",
                                "/api/v2/partidas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/partidas/**",
                                "/api/v2/partidas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )


                        // RESULTADOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resultados/**",
                                "/api/v2/resultados/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resultados/**",
                                "/api/v2/resultados/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resultados/**",
                                "/api/v2/resultados/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resultados/**",
                                "/api/v2/resultados/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )


                        // RANKINGS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rankings/**",
                                "/api/v2/rankings/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rankings/**",
                                "/api/v2/rankings/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rankings/**",
                                "/api/v2/rankings/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "ORGANIZADOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/rankings/**",
                                "/api/v2/rankings/**"
                        )
                        .hasRole("ADMIN")


                        // PREMIOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/premios/**",
                                "/api/v2/premios/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/premios/**",
                                "/api/v2/premios/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/premios/**",
                                "/api/v2/premios/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/premios/**",
                                "/api/v2/premios/**"
                        )
                        .hasRole("ADMIN")


                        // CUALQUIER OTRO ENDPOINT
                        .anyRequest()
                        .authenticated()
                )


                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )

                .build();
    }
}