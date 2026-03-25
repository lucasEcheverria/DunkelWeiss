package server.service;

import lib.dto.CreateHiloDto;
import lib.dto.HiloDto;
import lib.dto.HiloSummaryDto;
import org.springframework.stereotype.Service;
import server.dao.ComunidadRepository;
import server.dao.HiloRepository;
import server.dao.UserRepository;
import server.entity.Comunidad;
import server.entity.Hilo;
import server.entity.User;

import java.util.List;

/**
 * Servicio de gestión de hilos.
 *
 * Centraliza la lógica de negocio relacionada con los hilos,
 * delegando el acceso a datos en los repositorios correspondientes.
 */
@Service
public class HiloService {

    private final HiloRepository      hiloRepository;
    private final UserRepository      userRepository;
    private final ComunidadRepository comunidadRepository;

    public HiloService(HiloRepository hiloRepository,
                       UserRepository userRepository,
                       ComunidadRepository comunidadRepository) {
        this.hiloRepository      = hiloRepository;
        this.userRepository      = userRepository;
        this.comunidadRepository = comunidadRepository;
    }

    /**
     * Busca hilos cuyo título o descripción contengan el texto indicado.
     * La búsqueda es insensible a mayúsculas/minúsculas.
     *
     * @param query texto a buscar
     * @return lista de hilos que coinciden con la búsqueda
     */
    public List<Hilo> buscarHilos(String query) {
        return hiloRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    /**
     * Crea un nuevo hilo asociado a un usuario y una comunidad existentes.
     *
     * @param dto datos del hilo a crear
     * @return el hilo creado como DTO
     * @throws IllegalArgumentException si el usuario o la comunidad no existen
     */
    public HiloDto createHilo(CreateHiloDto dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + dto.getOwnerId()));

        Comunidad comunidad = comunidadRepository.findById(dto.getComunidadId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comunidad no encontrada: " + dto.getComunidadId()));

        Hilo hilo = new Hilo();
        hilo.setTitle(dto.getTitle());
        hilo.setDescription(dto.getDescription());
        hilo.setOwner(owner);
        hilo.setComunidad(comunidad);

        return toDto(hiloRepository.save(hilo));
    }

    /**
     * Obtiene el detalle de un hilo por su identificador.
     *
     * @param id identificador del hilo
     * @return el hilo encontrado como DTO
     * @throws IllegalArgumentException si el hilo no existe
     */
    public HiloDto getHilo(Integer id) {
        Hilo hilo = hiloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hilo no encontrado: " + id));
        return toDto(hilo);
    }

    /**
     * Obtiene un listado resumido de todos los hilos existentes.
     *
     * @return lista de resúmenes de hilos
     */
    public List<HiloSummaryDto> getAllSummaries() {
        return hiloRepository.findAll()
                .stream()
                .map(h -> new HiloSummaryDto(h.getId(), h.getTitle()))
                .toList();
    }

    /**
     * Convierte una entidad Hilo en su representación DTO.
     *
     * @param hilo entidad a convertir
     * @return DTO con los datos del hilo
     */
    private HiloDto toDto(Hilo hilo) {
        return new HiloDto(
                hilo.getId(),
                hilo.getTitle(),
                hilo.getDescription(),
                hilo.getOwner().getNickname(),
                hilo.getComunidad().getNombre()
        );
    }
}