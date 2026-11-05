package cl.duoc.esports.authservice.clients;

import cl.duoc.esports.authservice.dto.UsuarioAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import cl.duoc.esports.authservice.dto.RegistroUsuarioRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UsuarioClient {

    @PostMapping("/api/usuarios/registro")
    UsuarioAuthResponse registrarUsuario(
            @RequestBody RegistroUsuarioRequest request
    );

    @GetMapping("/api/usuarios/email/{email}")
    UsuarioAuthResponse buscarUsuarioPorEmail(@PathVariable String email);
}