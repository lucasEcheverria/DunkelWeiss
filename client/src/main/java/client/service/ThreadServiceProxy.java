package client.service;

import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
} 