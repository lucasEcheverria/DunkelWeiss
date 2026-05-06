package client.service;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ThreadServiceProxy {

    private final RestTemplate restTemplate;
    private final String serverApiUrl;
    private final AuthServiceProxy authService;

    public ThreadServiceProxy(RestTemplate restTemplate,
                              AuthServiceProxy authService,
                              @Value("${server.api.url}") String serverApiUrl) {
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.serverApiUrl = serverApiUrl;
    }

    public ThreadDTO createThread(CreateThreadDTO dto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authService.getToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateThreadDTO> request = new HttpEntity<>(dto, headers);

            ResponseEntity<ThreadDTO> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/create",
                    HttpMethod.POST,
                    request,
                    ThreadDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public ThreadDTO getThread(Integer id) {
        try {
            ResponseEntity<ThreadDTO> response = restTemplate.getForEntity(
                    serverApiUrl + "/api/threads/get/" + id,
                    ThreadDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public List<ThreadSummaryDTO> getAllSummaries() {
        try {
            ResponseEntity<List<ThreadSummaryDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/getAll",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    public List<ThreadDTO> getThreadsWithPrompt(String query) {
        try {
            ResponseEntity<List<ThreadDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/search?query=" + query,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            System.out.println(response.getBody());
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<ThreadDTO> getThreadsFromUser(String email) {
        try {
            ResponseEntity<List<ThreadDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/user?email=" + email,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<ThreadSummaryDTO> getConversationThreads(String email) {
        try {
            ResponseEntity<List<ThreadSummaryDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/user/conversations?email=" + email,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<ThreadSummaryDTO> getInitialFeed() {
        try {
            ResponseEntity<List<ThreadSummaryDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/thread_feed",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Obtener hilos favoritos del usuario autenticado
    public List<ThreadDTO> getFavoriteThreads() {
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = authService.getToken();
            if (token == null) return Collections.emptyList();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<List<ThreadDTO>> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/favorites",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Añadir a favoritos
    public boolean addFavorite(Integer threadId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = authService.getToken();
            if (token == null) return false;
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/favorites/" + threadId,
                    HttpMethod.POST,
                    request,
                    Void.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    // Quitar de favoritos
    public boolean removeFavorite(Integer threadId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = authService.getToken();
            if (token == null) return false;
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    serverApiUrl + "/api/threads/favorites/" + threadId,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}