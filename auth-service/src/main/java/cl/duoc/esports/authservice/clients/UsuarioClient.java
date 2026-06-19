package cl.duoc.esports.authservice.clients;

import cl.duoc.esports.authservice.dto.UsuarioAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/email/{email}")
    UsuarioAuthResponse buscarUsuarioPorEmail(@PathVariable String email);
}