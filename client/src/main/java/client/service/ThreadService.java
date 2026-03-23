package client.service;

import client.config.AppConfig;
import lib.dto.CreateHiloDto;
import lib.dto.HiloDto;
import lib.dto.HiloSummaryDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class ThreadService {

    private final RestTemplate restTemplate;
    private final String       serverApiUrl;

    public ThreadService(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.serverApiUrl = appConfig.getServerApiUrl();
    }

    public HiloDto createHilo(CreateHiloDto dto) {
        try {
            ResponseEntity<HiloDto> response = restTemplate.postForEntity(
                    serverApiUrl + "/api/threads/create",
                    dto,
                    HiloDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public HiloDto getHilo(Integer id) {
        try {
            ResponseEntity<HiloDto> response = restTemplate.getForEntity(
                    serverApiUrl + "/api/threads/get/" + id,
                    HiloDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public List<HiloSummaryDto> getAllSummaries() {
        try {
            ResponseEntity<List<HiloSummaryDto>> response = restTemplate.exchange(
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