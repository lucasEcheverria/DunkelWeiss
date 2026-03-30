package server.service;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.stereotype.Service;
import server.dao.CommunityRepository;
import server.dao.ThreadRepository;
import server.dao.UserRepository;
import server.entity.Community;
import server.entity.Thread;
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

    private final ThreadRepository threadRepository;
    private final UserRepository      userRepository;
    private final CommunityRepository communityRepository;

    public HiloService(ThreadRepository threadRepository,
                       UserRepository userRepository,
                       CommunityRepository communityRepository) {
        this.threadRepository = threadRepository;
        this.userRepository      = userRepository;
        this.communityRepository = communityRepository;
    }

    /**
     * Busca hilos cuyo título o descripción contengan el texto indicado.
     * La búsqueda es insensible a mayúsculas/minúsculas.
     *
     * @param query texto a buscar
     * @return lista de hilos que coinciden con la búsqueda
     */
    public List<Thread> buscarHilos(String query) {
        return threadRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    /**
     * Crea un nuevo hilo asociado a un usuario y una comunidad existentes.
     *
     * @param dto datos del hilo a crear
     * @return el hilo creado como DTO
     * @throws IllegalArgumentException si el usuario o la comunidad no existen
     */
    public ThreadDTO createHilo(CreateThreadDTO dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + dto.getOwnerId()));

        Community community = communityRepository.findById(dto.getComunidadId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Community no encontrada: " + dto.getComunidadId()));

        Thread hilo = new Thread();
        hilo.setTitle(dto.getTitle());
        hilo.setDescription(dto.getDescription());
        hilo.setOwner(owner);
        hilo.setCommunity(community);

        return toDto(threadRepository.save(hilo));
    }

    /**
     * Obtiene el detalle de un hilo por su identificador.
     *
     * @param id identificador del hilo
     * @return el hilo encontrado como DTO
     * @throws IllegalArgumentException si el hilo no existe
     */
    public ThreadDTO getHilo(Integer id) {
        Thread hilo = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Thread no encontrado: " + id));
        return toDto(hilo);
    }

    /**
     * Obtiene un listado resumido de todos los hilos existentes.
     *
     * @return lista de resúmenes de hilos
     */
    public List<ThreadSummaryDTO> getAllSummaries() {
        return threadRepository.findAll()
                .stream()
                .map(h -> new ThreadSummaryDTO(h.getId(), h.getTitle()))
                .toList();
    }

    /**
     * Convierte una entidad Thread en su representación DTO.
     *
     * @param hilo entidad a convertir
     * @return DTO con los datos del hilo
     */
    private ThreadDTO toDto(Thread hilo) {
        return new ThreadDTO(
                hilo.getId(),
                hilo.getTitle(),
                hilo.getDescription(),
                hilo.getOwner().getNickname(),
                hilo.getCommunity().getName()
        );
    }
}