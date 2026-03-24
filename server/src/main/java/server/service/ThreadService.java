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

@Service
public class ThreadService {

    private final HiloRepository     hiloRepository;
    private final UserRepository     userRepository;
    private final ComunidadRepository comunidadRepository;

    public ThreadService(HiloRepository hiloRepository,
                         UserRepository userRepository,
                         ComunidadRepository comunidadRepository) {
        this.hiloRepository      = hiloRepository;
        this.userRepository      = userRepository;
        this.comunidadRepository = comunidadRepository;
    }

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

        Hilo saved = hiloRepository.save(hilo);
        return toDto(saved);
    }

    public HiloDto getHilo(Integer id) {
        Hilo hilo = hiloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hilo no encontrado: " + id));
        return toDto(hilo);
    }

    public List<HiloSummaryDto> getAllSummaries() {
        return hiloRepository.findAll()
                .stream()
                .map(h -> new HiloSummaryDto(h.getId(), h.getTitle()))
                .toList();
    }

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