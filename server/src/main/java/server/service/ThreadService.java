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

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository     userRepository;
    private final CommunityRepository communityRepository;

    public ThreadService(ThreadRepository threadRepository,
                         UserRepository userRepository,
                         CommunityRepository communityRepository) {
        this.threadRepository = threadRepository;
        this.userRepository      = userRepository;
        this.communityRepository = communityRepository;
    }

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

        Thread saved = threadRepository.save(hilo);
        return toDto(saved);
    }

    public ThreadDTO getHilo(Integer id) {
        Thread hilo = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Thread no encontrado: " + id));
        return toDto(hilo);
    }

    public List<ThreadSummaryDTO> getAllSummaries() {
        return threadRepository.findAll()
                .stream()
                .map(h -> new ThreadSummaryDTO(h.getId(), h.getTitle(), h.getDescription(), h.getOwner().getNickname()))
                .toList();
    }
    public List<Thread> getThreadsWithPrompt(String query) {
        return threadRepository.findByTitleContainingIgnoreCase(query);
    }

    public List<Thread> getThreadsFromUser(String email) {
        return threadRepository.findByOwnerEmail(email);
    }


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