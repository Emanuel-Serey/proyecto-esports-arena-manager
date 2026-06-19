package cl.duoc.esports.authservice.services;

import cl.duoc.esports.authservice.clients.UsuarioClient;
import cl.duoc.esports.authservice.dto.*;
import cl.duoc.esports.authservice.exceptions.AuthException;
import cl.duoc.esports.authservice.models.CuentaAcceso;
import cl.duoc.esports.authservice.models.EstadoCuenta;
import cl.duoc.esports.authservice.models.Rol;
import cl.duoc.esports.authservice.repositories.CuentaAccesoRepository;
import cl.duoc.esports.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuentaAccesoRepository cuentaAccesoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioClient usuarioClient;

    public CuentaAccesoResponse crearCuenta(CrearCuentaRequest request) {

        if (cuentaAccesoRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Ya existe una cuenta con ese email", HttpStatus.CONFLICT);
        }

        UsuarioAuthResponse usuario;

        try {
            usuario = usuarioClient.buscarUsuarioPorEmail(request.getEmail());
        } catch (Exception ex) {
            throw new AuthException(
                    "No existe un usuario asociado a ese email en user-service",
                    HttpStatus.NOT_FOUND
            );
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new AuthException(
                    "El usuario asociado no se encuentra activo",
                    HttpStatus.FORBIDDEN
            );
        }

        if (!usuario.getRol().equalsIgnoreCase(request.getRol().name())) {
            throw new AuthException(
                    "El rol de la cuenta no coincide con el rol registrado en user-service",
                    HttpStatus.CONFLICT
            );
        }

        CuentaAcceso cuenta = CuentaAcceso.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .estado(EstadoCuenta.ACTIVO)
                .build();

        CuentaAcceso cuentaGuardada = cuentaAccesoRepository.save(cuenta);

        return convertirAResponse(cuentaGuardada);
    }

    public AuthResponse login(LoginRequest request) {

        CuentaAcceso cuenta = cuentaAccesoRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVO) {
            throw new AuthException("La cuenta se encuentra inactiva", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), cuenta.getPasswordHash())) {
            throw new AuthException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        UsuarioAuthResponse usuario;

        try {
            usuario = usuarioClient.buscarUsuarioPorEmail(cuenta.getEmail());
        } catch (Exception ex) {
            throw new AuthException("No existe un usuario asociado a ese email en user-service", HttpStatus.NOT_FOUND);
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new AuthException("El usuario asociado no se encuentra activo en user-service", HttpStatus.FORBIDDEN);
        }

        if (!usuario.getRol().equalsIgnoreCase(cuenta.getRol().name())) {
            throw new AuthException("El rol de la cuenta no coincide con el rol registrado en user-service", HttpStatus.CONFLICT);
        }

        String token = jwtService.generarToken(cuenta);

        return new AuthResponse(
                token,
                "Bearer",
                cuenta.getEmail(),
                cuenta.getRol()
        );
    }

    public List<CuentaAccesoResponse> listarCuentas() {
        return cuentaAccesoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public CuentaAccesoResponse buscarPorId(Long id) {
        CuentaAcceso cuenta = obtenerCuentaPorId(id);
        return convertirAResponse(cuenta);
    }

    public CuentaAccesoResponse buscarPorEmail(String email) {
        CuentaAcceso cuenta = cuentaAccesoRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Cuenta no encontrada", HttpStatus.NOT_FOUND));

        return convertirAResponse(cuenta);
    }

    public List<CuentaAccesoResponse> listarPorRol(Rol rol) {
        return cuentaAccesoRepository.findByRol(rol)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<CuentaAccesoResponse> listarPorEstado(EstadoCuenta estado) {
        return cuentaAccesoRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public CuentaAccesoResponse actualizarPassword(Long id, ActualizarPasswordRequest request) {
        CuentaAcceso cuenta = obtenerCuentaPorId(id);

        cuenta.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));

        return convertirAResponse(cuentaAccesoRepository.save(cuenta));
    }

    public CuentaAccesoResponse actualizarRol(Long id, ActualizarRolRequest request) {
        CuentaAcceso cuenta = obtenerCuentaPorId(id);

        cuenta.setRol(request.getRol());

        return convertirAResponse(cuentaAccesoRepository.save(cuenta));
    }

    public CuentaAccesoResponse actualizarEstado(Long id, ActualizarEstadoRequest request) {
        CuentaAcceso cuenta = obtenerCuentaPorId(id);

        cuenta.setEstado(request.getEstado());

        return convertirAResponse(cuentaAccesoRepository.save(cuenta));
    }

    public CuentaAccesoResponse desactivarCuenta(Long id) {
        CuentaAcceso cuenta = obtenerCuentaPorId(id);

        cuenta.setEstado(EstadoCuenta.INACTIVO);

        return convertirAResponse(cuentaAccesoRepository.save(cuenta));
    }

    private CuentaAcceso obtenerCuentaPorId(Long id) {
        return cuentaAccesoRepository.findById(id)
                .orElseThrow(() -> new AuthException("Cuenta no encontrada", HttpStatus.NOT_FOUND));
    }

    private CuentaAccesoResponse convertirAResponse(CuentaAcceso cuenta) {
        return CuentaAccesoResponse.builder()
                .id(cuenta.getId())
                .email(cuenta.getEmail())
                .rol(cuenta.getRol())
                .estado(cuenta.getEstado())
                .fechaCreacion(cuenta.getFechaCreacion())
                .build();
    }
}