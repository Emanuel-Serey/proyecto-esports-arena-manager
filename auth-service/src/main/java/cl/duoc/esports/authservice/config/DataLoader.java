package cl.duoc.esports.authservice.config;

import cl.duoc.esports.authservice.models.CuentaAcceso;
import cl.duoc.esports.authservice.models.EstadoCuenta;
import cl.duoc.esports.authservice.models.Rol;
import cl.duoc.esports.authservice.repositories.CuentaAccesoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initCuentas(
            CuentaAccesoRepository cuentaAccesoRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            cargarCuenta(
                    cuentaAccesoRepository,
                    passwordEncoder,
                    "admin@gmail.com",
                    "123456",
                    Rol.ADMIN
            );

            cargarCuenta(
                    cuentaAccesoRepository,
                    passwordEncoder,
                    "organizador@gmail.com",
                    "123456",
                    Rol.ORGANIZADOR
            );

            cargarCuenta(
                    cuentaAccesoRepository,
                    passwordEncoder,
                    "jugador@gmail.com",
                    "123456",
                    Rol.JUGADOR
            );

            System.out.println("Cuentas iniciales cargadas en auth-service");
        };
    }

    private void cargarCuenta(
            CuentaAccesoRepository cuentaAccesoRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String password,
            Rol rol
    ) {
        Optional<CuentaAcceso> cuentaExistente = cuentaAccesoRepository.findByEmail(email);

        if (cuentaExistente.isPresent()) {
            CuentaAcceso cuenta = cuentaExistente.get();

            cuenta.setPasswordHash(passwordEncoder.encode(password));
            cuenta.setRol(rol);
            cuenta.setEstado(EstadoCuenta.ACTIVO);

            cuentaAccesoRepository.save(cuenta);

            System.out.println("Cuenta inicial actualizada: " + email);
        } else {
            CuentaAcceso cuenta = CuentaAcceso.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .rol(rol)
                    .estado(EstadoCuenta.ACTIVO)
                    .fechaCreacion(LocalDate.now())
                    .build();

            cuentaAccesoRepository.save(cuenta);

            System.out.println("Cuenta inicial creada: " + email);
        }
    }
}