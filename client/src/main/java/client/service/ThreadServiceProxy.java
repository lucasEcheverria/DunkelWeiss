package client.service;

import client.config.AppConfig;
import lib.dto.CreateThreadDTO;
import lib.dto.ThreadDTO;
import lib.dto.ThreadSummaryDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class ThreadServiceProxy {

    private final RestTemplate restTemplate;
    private final String       serverApiUrl;

    public ThreadServiceProxy(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
    }

    public ThreadDTO createHilo(CreateThreadDTO dto) {
        try {
            ResponseEntity<ThreadDTO> response = restTemplate.postForEntity(
                    serverApiUrl + "/api/threads/create",
                    dto,
                    ThreadDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public ThreadDTO getHilo(Integer id) {
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