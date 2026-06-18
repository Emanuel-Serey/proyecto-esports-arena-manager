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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos de autenticación
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cuentas").permitAll()

                        // Swagger y documentación
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Gestión de cuentas de acceso: solo ADMIN
                        .requestMatchers("/api/auth/cuentas/**").hasRole("ADMIN")

                        // Usuarios: solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/v2/usuarios/**").hasRole("ADMIN")

                        // Juegos
                        .requestMatchers(HttpMethod.GET, "/api/juegos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v2/juegos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.POST, "/api/juegos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/juegos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/juegos/**").hasRole("ADMIN")

                        // Torneos
                        .requestMatchers(HttpMethod.GET, "/api/torneos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v2/torneos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.POST, "/api/torneos/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/torneos/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/torneos/**").hasRole("ADMIN")

                        // Equipos
                        .requestMatchers(HttpMethod.GET, "/api/equipos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.POST, "/api/equipos/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/equipos/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipos/**").hasAnyRole("ADMIN", "ORGANIZADOR")

                        // Inscripciones
                        .requestMatchers("/api/inscripciones/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers("/api/v2/inscripciones/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")

                        // Sanciones: solo ADMIN
                        .requestMatchers("/api/sanciones/**").hasRole("ADMIN")
                        .requestMatchers("/api/v2/sanciones/**").hasRole("ADMIN")

                        // Partidas y resultados
                        .requestMatchers("/api/partidas/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers("/api/v2/partidas/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers("/api/resultados/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers("/api/v2/resultados/**").hasAnyRole("ADMIN", "ORGANIZADOR")

                        // Rankings
                        .requestMatchers(HttpMethod.GET, "/api/rankings/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v2/rankings/**").hasAnyRole("ADMIN", "ORGANIZADOR", "JUGADOR")
                        .requestMatchers(HttpMethod.POST, "/api/rankings/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/rankings/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/rankings/**").hasRole("ADMIN")

                        // Premios
                        .requestMatchers("/api/premios/**").hasAnyRole("ADMIN", "ORGANIZADOR")
                        .requestMatchers("/api/v2/premios/**").hasAnyRole("ADMIN", "ORGANIZADOR")

                        // Todo lo demás requiere token
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