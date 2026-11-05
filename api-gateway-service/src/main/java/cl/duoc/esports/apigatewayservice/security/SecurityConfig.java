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
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString("rol");

            if (rol == null || rol.isBlank()) {
                return List.of();
            }

            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
            );

            return authorities;
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://proyecto-esports-frontend-react-bice.vercel.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {

        return http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticación
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cuentas").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Cuentas de acceso: solo ADMIN
                        .requestMatchers("/api/auth/cuentas/**").hasRole("ADMIN")

                        // Usuarios: por ahora solo ADMIN.
                        // Más adelante crearemos acceso al perfil propio.
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/v2/usuarios/**").hasRole("ADMIN")


                        // JUEGOS
                        // Los visitantes pueden consultar los juegos disponibles.
                        .requestMatchers(HttpMethod.GET, "/api/juegos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/juegos/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/juegos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/juegos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/juegos/**").hasRole("ADMIN")


                        // TORNEOS
                        // Los torneos son información pública.
                        .requestMatchers(HttpMethod.GET, "/api/torneos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/torneos/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/torneos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/torneos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/torneos/**")
                        .hasRole("ADMIN")


                        // EQUIPOS
                        .requestMatchers(HttpMethod.GET, "/api/equipos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        .requestMatchers(HttpMethod.POST, "/api/equipos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/equipos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/equipos/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")


                        // INSCRIPCIONES
                        .requestMatchers("/api/inscripciones/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        .requestMatchers("/api/v2/inscripciones/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")


                        // SANCIONES
                        // EP3 permite que ORGANIZADOR aplique sanciones.
                        .requestMatchers("/api/sanciones/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers("/api/v2/sanciones/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")


                        // PARTIDAS
                        // Las llaves/calendarios pueden ser consultados por visitantes.
                        .requestMatchers(HttpMethod.GET, "/api/partidas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/partidas/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/partidas/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/partidas/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/partidas/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")


                        // RESULTADOS
                        // Los resultados pueden ser consultados públicamente.
                        .requestMatchers(HttpMethod.GET, "/api/resultados/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/resultados/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/resultados/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/resultados/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")


                        // RANKINGS
                        // El visitante puede consultar las tablas de posiciones.
                        .requestMatchers(HttpMethod.GET, "/api/rankings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/rankings/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/rankings/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/rankings/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/rankings/**")
                        .hasRole("ADMIN")


                        // PREMIOS
                        // Se pueden consultar públicamente.
                        .requestMatchers(HttpMethod.GET, "/api/premios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/premios/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/premios/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.PUT, "/api/premios/**")
                        .hasAnyRole("ADMIN", "ORGANIZADOR")


                        // Cualquier otro endpoint requiere sesión
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .build();
    }
}