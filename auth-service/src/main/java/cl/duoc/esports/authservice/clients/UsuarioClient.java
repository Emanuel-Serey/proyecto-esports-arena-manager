package cl.duoc.esports.authservice.clients;

import cl.duoc.esports.authservice.dto.RegistroUsuarioRequest;
import cl.duoc.esports.authservice.dto.UsuarioAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:http://localhost:8087}",
        path = "/api/usuarios"
)
public interface UsuarioClient {

    @PostMapping("/registro")
    UsuarioAuthResponse registrarUsuario(
            @RequestBody RegistroUsuarioRequest request
    );

    @GetMapping("/email/{email}")
    UsuarioAuthResponse buscarUsuarioPorEmail(@PathVariable String email);

    @PutMapping("/{id}")
    UsuarioAuthResponse actualizarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioAuthResponse usuario
    );
}