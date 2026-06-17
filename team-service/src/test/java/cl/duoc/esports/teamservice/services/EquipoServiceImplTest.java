package cl.duoc.esports.teamservice.services;

import cl.duoc.esports.teamservice.clients.JuegoClient;
import cl.duoc.esports.teamservice.clients.UsuarioClient;
import cl.duoc.esports.teamservice.dto.EquipoDTO;
import cl.duoc.esports.teamservice.dto.JuegoDTO;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;
import cl.duoc.esports.teamservice.dto.UsuarioDTO;
import cl.duoc.esports.teamservice.exceptions.EquipoException;
import cl.duoc.esports.teamservice.models.Equipo;
import cl.duoc.esports.teamservice.models.MiembroEquipo;
import cl.duoc.esports.teamservice.repositories.EquipoRepository;
import cl.duoc.esports.teamservice.repositories.MiembroEquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServiceImplTest {

    @Mock
    private MiembroEquipoRepository miembroEquipoRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private JuegoClient juegoClient;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private EquipoServiceImpl equipoService;

    @Test
    void crearEquipo_deberiaCrearEquipoCorrectamente() {
        // Given
        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha",
                1L,
                1L,
                null,
                List.of(
                        crearMiembroDTO(null, 1L, "CAPITAN"),
                        crearMiembroDTO(null, 2L, "JUGADOR")
                )
        );

        when(equipoRepository.existsByNombre("Team Alpha")).thenReturn(false);
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(crearJuegoDTO(true));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));
        when(usuarioClient.buscarUsuarioPorId(2L)).thenReturn(crearUsuarioDTO(2L, "ACTIVO"));
        when(miembroEquipoRepository.existsByUsuarioIdAndEquipo_Estado(anyLong(), eq("ACTIVO"))).thenReturn(false);

        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> {
            Equipo equipo = invocation.getArgument(0);
            equipo.setId(1L);

            long idMiembro = 1L;
            for (MiembroEquipo miembro : equipo.getMiembros()) {
                miembro.setId(idMiembro++);
                miembro.setEquipo(equipo);
            }

            return equipo;
        });

        // When
        EquipoDTO resultado = equipoService.crearEquipo(equipoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Team Alpha", resultado.getNombre());
        assertEquals(1L, resultado.getCapitanId());
        assertEquals(1L, resultado.getJuegoPrincipalId());
        assertEquals("ACTIVO", resultado.getEstado());
        assertEquals(2, resultado.getMiembros().size());

        verify(equipoRepository).existsByNombre("Team Alpha");
        verify(juegoClient).buscarJuegoPorId(1L);
        verify(usuarioClient, times(3)).buscarUsuarioPorId(anyLong());
        verify(equipoRepository).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_deberiaLanzarErrorSiNombreYaExiste() {
        // Given
        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha",
                1L,
                1L,
                null,
                List.of(crearMiembroDTO(null, 1L, "CAPITAN"))
        );

        when(equipoRepository.existsByNombre("Team Alpha")).thenReturn(true);

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.crearEquipo(equipoDTO)
        );

        // Then
        assertEquals("Ya existe un equipo con ese nombre", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(equipoRepository).existsByNombre("Team Alpha");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_deberiaLanzarErrorSiNoTieneMiembros() {
        // Given
        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha",
                1L,
                1L,
                null,
                List.of()
        );

        when(equipoRepository.existsByNombre("Team Alpha")).thenReturn(false);
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(crearJuegoDTO(true));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.crearEquipo(equipoDTO)
        );

        // Then
        assertEquals("El equipo debe tener al menos un miembro", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_deberiaLanzarErrorSiCapitanNoEstaIncluido() {
        // Given
        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha",
                99L,
                1L,
                null,
                List.of(crearMiembroDTO(null, 1L, "JUGADOR"))
        );

        when(equipoRepository.existsByNombre("Team Alpha")).thenReturn(false);
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(crearJuegoDTO(true));
        when(usuarioClient.buscarUsuarioPorId(99L)).thenReturn(crearUsuarioDTO(99L, "ACTIVO"));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.crearEquipo(equipoDTO)
        );

        // Then
        assertEquals("El capitán debe estar incluido como miembro del equipo", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_deberiaLanzarErrorSiHayMiembrosDuplicados() {
        // Given
        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha",
                1L,
                1L,
                null,
                List.of(
                        crearMiembroDTO(null, 1L, "CAPITAN"),
                        crearMiembroDTO(null, 1L, "JUGADOR")
                )
        );

        when(equipoRepository.existsByNombre("Team Alpha")).thenReturn(false);
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(crearJuegoDTO(true));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.crearEquipo(equipoDTO)
        );

        // Then
        assertEquals("No se puede repetir un usuario dentro del mismo equipo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void listarEquipos_deberiaRetornarListaDeEquipos() {
        // Given
        Equipo equipo1 = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        Equipo equipo2 = crearEquipo(2L, "Team Beta", 2L, 1L, "ACTIVO");

        when(equipoRepository.findAll()).thenReturn(List.of(equipo1, equipo2));

        // When
        List<EquipoDTO> resultado = equipoService.listarEquipos();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("Team Alpha", resultado.get(0).getNombre());
        assertEquals("Team Beta", resultado.get(1).getNombre());

        verify(equipoRepository).findAll();
    }

    @Test
    void buscarEquipoPorId_deberiaRetornarEquipoSiExiste() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        // When
        EquipoDTO resultado = equipoService.buscarEquipoPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Team Alpha", resultado.getNombre());
        assertEquals("ACTIVO", resultado.getEstado());

        verify(equipoRepository).findById(1L);
    }

    @Test
    void buscarEquipoPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.buscarEquipoPorId(99L)
        );

        // Then
        assertEquals("Equipo no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(equipoRepository).findById(99L);
    }

    @Test
    void actualizarEquipo_deberiaActualizarEquipoCorrectamente() {
        // Given
        Equipo equipoExistente = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipoExistente.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipoExistente));

        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Alpha Updated",
                1L,
                1L,
                null,
                null
        );

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipoExistente));
        when(equipoRepository.existsByNombre("Team Alpha Updated")).thenReturn(false);
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(crearJuegoDTO(true));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EquipoDTO resultado = equipoService.actualizarEquipo(1L, equipoDTO);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals("Team Alpha Updated", resultado.getNombre());
        assertEquals(1L, resultado.getCapitanId());
        assertEquals(1L, resultado.getJuegoPrincipalId());

        verify(equipoRepository).findById(1L);
        verify(equipoRepository).existsByNombre("Team Alpha Updated");
        verify(equipoRepository).save(any(Equipo.class));
    }

    @Test
    void actualizarEquipo_deberiaLanzarErrorSiNombreYaExiste() {
        // Given
        Equipo equipoExistente = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        EquipoDTO equipoDTO = crearEquipoDTO(
                null,
                "Team Beta",
                1L,
                1L,
                null,
                null
        );

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipoExistente));
        when(equipoRepository.existsByNombre("Team Beta")).thenReturn(true);

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.actualizarEquipo(1L, equipoDTO)
        );

        // Then
        assertEquals("Ya existe un equipo con ese nombre", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(equipoRepository).findById(1L);
        verify(equipoRepository).existsByNombre("Team Beta");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void desactivarEquipo_deberiaCambiarEstadoAInactivo() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        equipoService.desactivarEquipo(1L);

        // Then
        assertEquals("INACTIVO", equipo.getEstado());

        verify(equipoRepository).findById(1L);
        verify(equipoRepository).save(equipo);
    }

    @Test
    void agregarMiembro_deberiaAgregarMiembroCorrectamente() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipo.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipo));

        MiembroEquipoDTO miembroDTO = crearMiembroDTO(null, 2L, "JUGADOR");

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(usuarioClient.buscarUsuarioPorId(2L)).thenReturn(crearUsuarioDTO(2L, "ACTIVO"));
        when(miembroEquipoRepository.existsByUsuarioIdAndEquipo_Estado(2L, "ACTIVO")).thenReturn(false);

        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> {
            Equipo equipoGuardado = invocation.getArgument(0);
            for (MiembroEquipo miembro : equipoGuardado.getMiembros()) {
                if (miembro.getId() == null) {
                    miembro.setId(2L);
                    miembro.setEquipo(equipoGuardado);
                }
            }
            return equipoGuardado;
        });

        // When
        MiembroEquipoDTO resultado = equipoService.agregarMiembro(1L, miembroDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getUsuarioId());
        assertEquals("JUGADOR", resultado.getRolDentroEquipo());
        assertEquals(2, equipo.getMiembros().size());

        verify(equipoRepository).findById(1L);
        verify(usuarioClient).buscarUsuarioPorId(2L);
        verify(equipoRepository).save(equipo);
    }

    @Test
    void agregarMiembro_deberiaLanzarErrorSiUsuarioYaPerteneceAlEquipo() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipo.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipo));

        MiembroEquipoDTO miembroDTO = crearMiembroDTO(null, 1L, "JUGADOR");

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.agregarMiembro(1L, miembroDTO)
        );

        // Then
        assertEquals("El usuario ya pertenece al equipo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void listarMiembros_deberiaRetornarMiembrosDelEquipo() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipo.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipo));
        equipo.getMiembros().add(crearMiembro(2L, 2L, "JUGADOR", equipo));

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        // When
        List<MiembroEquipoDTO> resultado = equipoService.listarMiembros(1L);

        // Then
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuarioId());
        assertEquals(2L, resultado.get(1).getUsuarioId());

        verify(equipoRepository).findById(1L);
    }

    @Test
    void eliminarMiembro_deberiaEliminarMiembroCorrectamente() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        MiembroEquipo capitan = crearMiembro(1L, 1L, "CAPITAN", equipo);
        MiembroEquipo jugador = crearMiembro(2L, 2L, "JUGADOR", equipo);

        equipo.getMiembros().add(capitan);
        equipo.getMiembros().add(jugador);

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        equipoService.eliminarMiembro(1L, 2L);

        // Then
        assertEquals(1, equipo.getMiembros().size());
        assertEquals(1L, equipo.getMiembros().get(0).getUsuarioId());

        verify(equipoRepository).findById(1L);
        verify(equipoRepository).save(equipo);
    }

    @Test
    void eliminarMiembro_deberiaLanzarErrorSiMiembroNoExiste() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipo.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipo));

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.eliminarMiembro(1L, 99L)
        );

        // Then
        assertEquals("Miembro no encontrado en el equipo", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void eliminarMiembro_deberiaLanzarErrorSiEsCapitan() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");
        equipo.getMiembros().add(crearMiembro(1L, 1L, "CAPITAN", equipo));

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        // When
        EquipoException exception = assertThrows(
                EquipoException.class,
                () -> equipoService.eliminarMiembro(1L, 1L)
        );

        // Then
        assertEquals("No se puede eliminar al capitán del equipo", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void listarPorEstado_deberiaRetornarEquiposPorEstado() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        when(equipoRepository.findByEstado("ACTIVO")).thenReturn(List.of(equipo));

        // When
        List<EquipoDTO> resultado = equipoService.listarPorEstado("activo");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.get(0).getEstado());

        verify(equipoRepository).findByEstado("ACTIVO");
    }

    @Test
    void listarPorJuegoPrincipal_deberiaRetornarEquiposPorJuego() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        when(equipoRepository.findByJuegoPrincipalId(1L)).thenReturn(List.of(equipo));

        // When
        List<EquipoDTO> resultado = equipoService.listarPorJuegoPrincipal(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getJuegoPrincipalId());

        verify(equipoRepository).findByJuegoPrincipalId(1L);
    }

    @Test
    void listarPorCapitan_deberiaRetornarEquiposPorCapitan() {
        // Given
        Equipo equipo = crearEquipo(1L, "Team Alpha", 1L, 1L, "ACTIVO");

        when(equipoRepository.findByCapitanId(1L)).thenReturn(List.of(equipo));

        // When
        List<EquipoDTO> resultado = equipoService.listarPorCapitan(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getCapitanId());

        verify(equipoRepository).findByCapitanId(1L);
    }

    private Equipo crearEquipo(Long id, String nombre, Long capitanId, Long juegoPrincipalId, String estado) {
        Equipo equipo = new Equipo();
        equipo.setId(id);
        equipo.setNombre(nombre);
        equipo.setCapitanId(capitanId);
        equipo.setJuegoPrincipalId(juegoPrincipalId);
        equipo.setEstado(estado);
        equipo.setMiembros(new ArrayList<>());
        return equipo;
    }

    private EquipoDTO crearEquipoDTO(
            Long id,
            String nombre,
            Long capitanId,
            Long juegoPrincipalId,
            String estado,
            List<MiembroEquipoDTO> miembros) {

        EquipoDTO equipoDTO = new EquipoDTO();
        equipoDTO.setId(id);
        equipoDTO.setNombre(nombre);
        equipoDTO.setCapitanId(capitanId);
        equipoDTO.setJuegoPrincipalId(juegoPrincipalId);
        equipoDTO.setEstado(estado);
        equipoDTO.setMiembros(miembros);
        return equipoDTO;
    }

    private MiembroEquipo crearMiembro(Long id, Long usuarioId, String rolDentroEquipo, Equipo equipo) {
        MiembroEquipo miembro = new MiembroEquipo();
        miembro.setId(id);
        miembro.setUsuarioId(usuarioId);
        miembro.setRolDentroEquipo(rolDentroEquipo);
        miembro.setEquipo(equipo);
        return miembro;
    }

    private MiembroEquipoDTO crearMiembroDTO(Long id, Long usuarioId, String rolDentroEquipo) {
        MiembroEquipoDTO miembroDTO = new MiembroEquipoDTO();
        miembroDTO.setId(id);
        miembroDTO.setUsuarioId(usuarioId);
        miembroDTO.setRolDentroEquipo(rolDentroEquipo);
        return miembroDTO;
    }

    private JuegoDTO crearJuegoDTO(Boolean estado) {
        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setId(1L);
        juegoDTO.setEstado(estado);
        return juegoDTO;
    }

    private UsuarioDTO crearUsuarioDTO(Long id, String estado) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(id);
        usuarioDTO.setEstado(estado);
        return usuarioDTO;
    }
}