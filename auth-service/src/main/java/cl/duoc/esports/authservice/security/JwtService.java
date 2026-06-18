package cl.duoc.esports.authservice.security;

import cl.duoc.esports.authservice.models.CuentaAcceso;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generarToken(CuentaAcceso cuenta) {
        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + expiration);

        return Jwts.builder()
                .subject(cuenta.getEmail())
                .claim("rol", cuenta.getRol().name())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String obtenerEmailDesdeToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerRolDesdeToken(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    public boolean validarToken(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}