package cl.duoc.esports.sanctionservice.clients;

import cl.duoc.esports.sanctionservice.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8087/api/usuarios")
public interface UsuarioClient {

    @GetMapping("/{id}")
    UsuarioDTO buscarUsuarioPorId(@PathVariable Long id);
}