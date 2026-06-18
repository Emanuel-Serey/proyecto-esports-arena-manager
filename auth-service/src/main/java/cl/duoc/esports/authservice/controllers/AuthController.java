package cl.duoc.esports.authservice.controllers;

import cl.duoc.esports.authservice.dto.*;
import cl.duoc.esports.authservice.models.EstadoCuenta;
import cl.duoc.esports.authservice.models.Rol;
import cl.duoc.esports.authservice.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Auth Service",
        description = "Endpoints de autenticación, login y administración de cuentas de acceso"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/cuentas")
    @Operation(
            summary = "Crear cuenta de acceso",
            description = "Crea una cuenta de acceso con email, contraseña y rol. La contraseña se almacena encriptada con BCrypt."
    )
    public ResponseEntity<CuentaAccesoResponse> crearCuenta(
            @Valid @RequestBody CrearCuentaRequest request
    ) {
        CuentaAccesoResponse response = authService.crearCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales de la cuenta y devuelve un token JWT con el rol del usuario autenticado."
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cuentas")
    @Operation(
            summary = "Listar cuentas de acceso",
            description = "Obtiene el listado completo de cuentas de acceso registradas en el sistema."
    )
    public ResponseEntity<List<CuentaAccesoResponse>> listarCuentas() {
        return ResponseEntity.ok(authService.listarCuentas());
    }

    @GetMapping("/cuentas/{id}")
    @Operation(
            summary = "Buscar cuenta por ID",
            description = "Busca una cuenta de acceso específica mediante su identificador."
    )
    public ResponseEntity<CuentaAccesoResponse> buscarPorId(
            @Parameter(description = "ID de la cuenta de acceso", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(authService.buscarPorId(id));
    }

    @GetMapping("/cuentas/buscar")
    @Operation(
            summary = "Buscar cuenta por email",
            description = "Busca una cuenta de acceso utilizando su correo electrónico."
    )
    public ResponseEntity<CuentaAccesoResponse> buscarPorEmail(
            @Parameter(description = "Correo electrónico de la cuenta", example = "admin@gmail.com")
            @RequestParam String email
    ) {
        return ResponseEntity.ok(authService.buscarPorEmail(email));
    }

    @GetMapping("/cuentas/rol/{rol}")
    @Operation(
            summary = "Listar cuentas por rol",
            description = "Lista las cuentas de acceso filtradas por rol. Valores permitidos: ADMIN, ORGANIZADOR, JUGADOR."
    )
    public ResponseEntity<List<CuentaAccesoResponse>> listarPorRol(
            @Parameter(description = "Rol de la cuenta", example = "ADMIN")
            @PathVariable Rol rol
    ) {
        return ResponseEntity.ok(authService.listarPorRol(rol));
    }

    @GetMapping("/cuentas/estado/{estado}")
    @Operation(
            summary = "Listar cuentas por estado",
            description = "Lista las cuentas de acceso filtradas por estado. Valores permitidos: ACTIVO, INACTIVO."
    )
    public ResponseEntity<List<CuentaAccesoResponse>> listarPorEstado(
            @Parameter(description = "Estado de la cuenta", example = "ACTIVO")
            @PathVariable EstadoCuenta estado
    ) {
        return ResponseEntity.ok(authService.listarPorEstado(estado));
    }

    @PutMapping("/cuentas/{id}/password")
    @Operation(
            summary = "Actualizar contraseña",
            description = "Actualiza la contraseña de una cuenta de acceso. La nueva contraseña se almacena encriptada con BCrypt."
    )
    public ResponseEntity<CuentaAccesoResponse> actualizarPassword(
            @Parameter(description = "ID de la cuenta de acceso", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.actualizarPassword(id, request));
    }

    @PutMapping("/cuentas/{id}/rol")
    @Operation(
            summary = "Actualizar rol",
            description = "Actualiza el rol asociado a una cuenta de acceso."
    )
    public ResponseEntity<CuentaAccesoResponse> actualizarRol(
            @Parameter(description = "ID de la cuenta de acceso", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request
    ) {
        return ResponseEntity.ok(authService.actualizarRol(id, request));
    }

    @PutMapping("/cuentas/{id}/estado")
    @Operation(
            summary = "Actualizar estado",
            description = "Actualiza el estado de una cuenta de acceso, permitiendo activarla o inactivarla."
    )
    public ResponseEntity<CuentaAccesoResponse> actualizarEstado(
            @Parameter(description = "ID de la cuenta de acceso", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequest request
    ) {
        return ResponseEntity.ok(authService.actualizarEstado(id, request));
    }

    @DeleteMapping("/cuentas/{id}")
    @Operation(
            summary = "Desactivar cuenta",
            description = "Desactiva una cuenta de acceso sin eliminar físicamente el registro de la base de datos."
    )
    public ResponseEntity<CuentaAccesoResponse> desactivarCuenta(
            @Parameter(description = "ID de la cuenta de acceso", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(authService.desactivarCuenta(id));
    }
}