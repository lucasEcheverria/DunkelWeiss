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
import java.util.ArrayList;

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    public ThreadService(ThreadRepository threadRepository,
                         UserRepository userRepository,
                         CommunityRepository communityRepository) {
        this.threadRepository = threadRepository;
        this.userRepository      = userRepository;
        this.communityRepository = communityRepository;
    }

    public ThreadDTO createHilo(CreateThreadDTO dto, User owner) {
        Community community = communityRepository.findById(dto.communityId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Community no encontrada: " + dto.communityId()));

        Thread hilo = new Thread();
        hilo.setTitle(dto.title());
        hilo.setDescription(dto.description());
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
    
    // Añade un hilo a la lista de favoritos del usuario autenticado.
    public void addFavoriteThread(User user, Integer threadId) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User no encontrado: " + user.getId()));

        Thread hilo = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread no encontrado: " + threadId));
        
        List<Thread> favs = u.getFavoriteThreads();
        if (favs == null) favs = new ArrayList<>();

        // Evita duplicados
        if (favs.stream().noneMatch(t -> t.getId().equals(threadId))) {
            favs.add(hilo);
            u.setFavoriteThreads(favs);
            userRepository.save(u);
        }
    }

    // Quita un hilo de la lista de favoritos del usuario autenticado.
    public void removeFavoriteThread(User user, Integer threadId) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User no encontrado: " + user.getId()));

        @SuppressWarnings("unused")
		Thread hilo = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread no encontrado: " + threadId));

        List<Thread> favs = u.getFavoriteThreads();
        if (favs == null || favs.stream().noneMatch(t -> t.getId().equals(threadId))) {
            throw new IllegalArgumentException("El hilo no está en favoritos: " + threadId);
        }

        favs.removeIf(t -> t.getId().equals(threadId));
        u.setFavoriteThreads(favs);
        userRepository.save(u);
    }
    
    // Devuelve la lista de hilos marcados como favoritos por el usuario.
    public List<Thread> getFavoriteThreads(User user) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User no encontrado: " + user.getId()));
        List<Thread> favs = u.getFavoriteThreads();
        return favs == null ? List.of() : favs;
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

    public List<ThreadSummaryDTO> getInitialFeed() {
        return threadRepository.findTop10ByPostCount()
                .stream()
                .map(h -> new ThreadSummaryDTO(
                        h.getId(),
                        h.getTitle(),
                        h.getDescription(),
                        h.getOwner().getNickname()
                ))
                .toList();
    }
}