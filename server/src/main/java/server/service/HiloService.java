package server.service;

import org.springframework.stereotype.Service;
import server.dao.HiloRepository;
import server.entity.Hilo;
import java.util.List;

/**
 * Servicio de gestión de hilos.
 * Centraliza la lógica de negocio relacionada con los hilos,
 * delegando el acceso a datos en HiloRepository.
 */
@Service
public class HiloService {

    private final HiloRepository hiloRepository;

    public HiloService(HiloRepository hiloRepository) {
        this.hiloRepository = hiloRepository;
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

}